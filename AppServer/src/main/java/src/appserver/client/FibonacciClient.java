package appserver.client;

import appserver.comm.Message;
import appserver.comm.MessageTypes;
import appserver.job.Job;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;
import utils.PropertyHandler;

public class FibonacciClient extends Thread implements MessageTypes {
    
    String host = null;
    int port;
    int number;

    Properties properties;
    
    public FibonacciClient(String filePath, int number)
    {
        try {
            // Reading properties file for server information
            properties = new PropertyHandler(filePath);
            host = properties.getProperty("HOST");
            port = Integer.parseInt(properties.getProperty("PORT"));
            System.out.println("[FibonacciClient.FibonacciClient] Host: " + host);
            System.out.println("[FibonacciClient.FibonacciClient] Port: " + port);
            this.number = number;
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    
    public void run() {
        try { 
            // connect to application server
            Socket server = new Socket(host, port);
                        
            // hard-coded string of class, aka tool name
            String classString = "docRoot.appserver.job.impl.Fibonacci";
            
            // create job and job request message
            Job job = new Job(classString, number);
            Message message = new Message(JOB_REQUEST, job);
            
            // sending job out to the application server in a message
            ObjectOutputStream writeToNet = new ObjectOutputStream(server.getOutputStream());
            writeToNet.writeObject(message);
            
            // reading result back in from application server
            // for simplicity, the result is not encapsulated in a message
            ObjectInputStream readFromNet = new ObjectInputStream(server.getInputStream());
            Integer result = (Integer) readFromNet.readObject();
            System.out.println("Fibonacci of " + number  + ": " + result);
        } catch (Exception ex) {
            System.err.println("[FibonacciClient.FibonacciClient] Error occurred");
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) 
    {
        // Spawns 48 threads to compute each fibonacci number 1-48
        // 47 and 48 are too large for integer variables however
        for (int i = 48; i > 0; i--) 
        {
            new FibonacciClient(args[0], i).start();
        }
    }
}
