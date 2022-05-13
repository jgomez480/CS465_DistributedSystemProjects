package appserver.server;

import appserver.comm.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropertyHandler;

public class Server implements MessageTypes {
    
    static SatelliteManager satelliteManager;
    static LoadManager loadManager;
    static ServerSocket serverSocket;
    int port;
    
    
    
    public Server(String propertiesFilePath) 
    {
        // Initalize static variables
        satelliteManager = new SatelliteManager();
        System.out.println("[Server.Server] Satellite Manager Created");
        loadManager = new LoadManager();
        System.out.println("[Server.Server] Load Manager Created");
        
        
        
        try {
            
            // init static variables with properties read 
            Properties properties;
            properties = new PropertyHandler(propertiesFilePath);
            port = Integer.parseInt(properties.getProperty("PORT"));
            
            serverSocket = new ServerSocket(port);
            System.out.println("[Server.Server] Server Socket Created");

        } catch (Exception e) {
            System.err.println("Properties file " + propertiesFilePath + " not found, exiting ...");
            System.exit(1);
        }
    }
    
    public void run() 
    {
        try 
        {
            while (true)
            {
                // Accept connections to server and spawn thread to handle them
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            }
        } catch (IOException ex) { System.err.println(ex); }
    }
    
    public static void main(String[] args)
    {
        new Server(args[0]).run();
    }
    
    
    
    
    
    
    public class ServerThread extends Thread {
            
        Socket client;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;
        Message recvMessage;
        Message sendMessage;
        
        private ServerThread(Socket client) 
        {
            this.client = client;
        }
    
        public void run() 
        {
            try 
            {
                // Opening streams and reading first message
                inputStream = new ObjectInputStream(client.getInputStream());
                outputStream = new ObjectOutputStream(client.getOutputStream());
                recvMessage = (Message) inputStream.readObject();
                ConnectivityInfo satelliteInfo = null;
            
                switch(recvMessage.getType())
                {
                    case REGISTER_SATELLITE:
                        // Get connectivity info from satellite
                        System.out.println("\n[ServerThread.run] REGISTER_SATELLITE");
                        satelliteInfo = (ConnectivityInfo) recvMessage.getContent();
                        
                        System.out.println("Satellite IP  : " + satelliteInfo.getHost());
                        System.out.println("Satellite Port: " + satelliteInfo.getPort());                        
                        System.out.println("Satellite Name: " + satelliteInfo.getName());
                        
                        // Register the satellite to the SatelliteManager and LoadManager
                        synchronized (Server.satelliteManager) 
                        {
                            Server.satelliteManager.registerSatellite(satelliteInfo);
                        }

                        synchronized (Server.loadManager) 
                        {
                            Server.loadManager.satelliteAdded(satelliteInfo.getName());
                        }
                        break;
                    case JOB_REQUEST:
                        // Get a satellite from the LoadManager using RR policy
                        String satelliteName;
                        synchronized (Server.loadManager) 
                        {
                            satelliteName = Server.loadManager.nextSatellite();
                        }
                        
                        satelliteInfo = 
                            Server.satelliteManager.getSatelliteFromName(satelliteName);
                        
                        System.out.println("[ServerThread.run] Satellite to process job request: " + satelliteInfo.getName());

                        // Get satellite connectivity info
                        String satelliteHost = satelliteInfo.getHost();
                        int satellitePort = satelliteInfo.getPort();
                        
                        // Connect to satellite
                        Socket satellite = new Socket(satelliteHost, satellitePort);
                        ObjectOutputStream toSatellite = new ObjectOutputStream(satellite.getOutputStream());
                        ObjectInputStream fromSatellite = new ObjectInputStream(satellite.getInputStream());
                        
                        // Send the same job request message
                        sendMessage = recvMessage;       
                        toSatellite.writeObject(sendMessage);
                        
                        // Read in the result of the job computation
                        Integer result = (Integer) fromSatellite.readObject();
                        outputStream.writeObject(result);
                        break;
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
