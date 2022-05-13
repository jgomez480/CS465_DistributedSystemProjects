package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.job.Tool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropertyHandler;

/**
 * Class [Satellite] Instances of this class represent computing nodes that execute jobs by
 * calling the callback method of tool a implementation, loading the tool's code dynamically over a network
 * or locally from the cache, if a tool got executed before.
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Satellite extends Thread 
{

    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    private Hashtable toolsCache = null;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) 
    {

        try
        {
            PropertyHandler satelliteFile = new PropertyHandler(satellitePropertiesFile);
            satelliteInfo.setName(satelliteFile.getProperty("NAME"));
            satelliteInfo.setPort(Integer.parseInt(satelliteFile.getProperty("PORT")));
            satelliteInfo.setHost("127.0.0.1");
        }
        catch(IOException e)
        {
            System.err.println("No Satellite config file found ... ");
            System.exit(1);
        }
        
        
        // read properties of the application server and populate serverInfo object
        // other than satellites, the as doesn't have a human-readable name, so leave it out
        // ...
        try
        {
            PropertyHandler serverFile = new PropertyHandler(serverPropertiesFile);
            serverInfo.setPort(Integer.parseInt(serverFile.getProperty("PORT")));
            serverInfo.setHost(serverFile.getProperty("HOST"));
        }
        catch (IOException e)
        {
            System.err.println("No server file found ... ");
            System.exit(1);
            
        }
        
        
        // read properties of the code server and create class loader
        // -------------------
        // ...
        try
        {
            PropertyHandler classLoaderFile = new PropertyHandler(classLoaderPropertiesFile);
            classLoader = new HTTPClassLoader(classLoaderFile.getProperty("HOST"), Integer.parseInt(classLoaderFile.getProperty("PORT")));
            System.out.println("[Satellite.Satellite] HTTPClassLoader created on " + satelliteInfo.getName());
        }
        catch (IOException e)
        {
            System.err.println("No Class Loader File Found ...");
            System.exit(1);
        }

        
        // create tools cache
        // -------------------
        // ...
        toolsCache = new Hashtable();
        
    }

    @Override
    public void run() 
    {

        // register this satellite with the SatelliteManager on the server
        // ---------------------------------------------------------------
        // ...
        try
        {
            // setting up socket object for server connection
            // retrieving host w/ message
            Socket connectServer = new Socket(serverInfo.getHost(), serverInfo.getPort());
            Message registerMessage = new Message(REGISTER_SATELLITE, satelliteInfo);
            ObjectOutputStream writeNet = new ObjectOutputStream(connectServer.getOutputStream());
            
            // display for running message to server 
            System.out.println("[Satellite.run] Satellite " + satelliteInfo.getName() + " connected to server, transfer connectivity information ...");
            writeNet.writeObject(registerMessage);

        }
        catch(IOException e)
        {
            System.err.println("[Satellite.run] Error Creating Socket for Server");
        }
        
        // create server socket
        // ---------------------------------------------------------------
        // ...
        try
        {
            ServerSocket socket = new ServerSocket(satelliteInfo.getPort());
            System.out.println("[Satellite.run] Socket Created on Port #: " + satelliteInfo.getPort());
        
        // start taking job requests in a server loop
        // ---------------------------------------------------------------
        // ...
            while( true )
            {
                new SatelliteThread(socket.accept(), this).run();
            }
        }
        catch(IOException e)
        {
            System.err.println("[Satellite.run] Error Creating Socket");
        }
    }

    // inner helper class that is instanciated in above server loop and processes single job requests
    private class SatelliteThread extends Thread 
    {

        Satellite satellite = null;
        Socket jobRequest = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        SatelliteThread(Socket jobRequest, Satellite satellite)
        {
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run() 
        {
            // setting up object streams
            // ...
            try
            {
                readFromNet = new ObjectInputStream(jobRequest.getInputStream());
                writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());
            
                // reading message
                // ...
                message = (Message) readFromNet.readObject();
                switch (message.getType()) 
                {
                    case JOB_REQUEST:
                        // processing job request
                        // ...
                        System.out.println("[SatelliteThread.run] Recieved Job Request");
                        try
                        {
                            Job currentJob = (Job) message.getContent();
                            String toolString = currentJob.getToolName();
                            Tool tool = getToolObject(toolString);
                            Object result = tool.go(currentJob.getParameters());

                            writeToNet.writeObject(result);
                            System.out.println("\n[SatelliteThread.run] *JOB COMPLETE* Processed job - Result: " + (int) result);
                        }
                        catch (UnknownToolException | InstantiationException | IllegalAccessException | ClassNotFoundException e)
                        {
                            System.err.println("[SatelliteThread.run] Error Processing Job Request");
                            e.printStackTrace();
                        }
                        break;

                    default:
                        System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
                }
            }
            catch(IOException | ClassNotFoundException e)
            {
                System.out.println("[Satellite.SatelliteThread] Error Reading Message From I/O Stream");
            }
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     * If the tool has been used before, it is returned immediately out of the cache,
     * otherwise it is loaded dynamically
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Tool toolObject = (Tool) toolsCache.get(toolClassString);

        // ...
        if(toolObject == null)
        {
            System.out.println("\n Tool Class: " + toolClassString);
            if(toolClassString == null)
            {
                throw new UnknownToolException();
            }
            Class toolClass = classLoader.findClass(toolClassString);
            toolObject = (Tool) toolClass.newInstance();
            toolsCache.put(toolClassString, toolObject);
        }
        else
        {
            System.out.println("Tool Class: \"" + toolClassString + "\" already in Cache");
        }
        
        return toolObject;
    }

    public static void main(String[] args) {
        // start the satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.run();
    }
}
