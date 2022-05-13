package package_1;

import message.Message;
import message.MessageTypes;

import java.io.*;
import java.util.*;
import java.net.*;

public class Receiver extends Thread implements MessageTypes, Serializable {
    
    ChatClient client;
    ServerSocket serverSocket;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;
    int port;
    boolean reset = false;
    
    public Receiver(int port) {
        this.port = port;
    }
    
    public void run() {
        
        while (true) {
            
            try {
                serverSocket = new ServerSocket(port);
                // Accept next connection to server
                Socket socket = serverSocket.accept();

                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
            }
            catch (Exception e) { System.out.println("Error" + e); System.exit(1); }
            
            Message receivedMessage = null;
            try { 
                // Read message object through data stream
                receivedMessage = (Message) inputStream.readObject();
            } catch (Exception e) { System.out.println(e); }

            // Print out the data in message object content
            System.out.println("MESSAGE RECEIVED: " + (String) receivedMessage.getContent());
            
            try {
                // If there is nothing left to read (socket is closed) close this socket
                if (inputStream.read() == -1) {
                    serverSocket.close();
                }
            } catch (Exception e) { System.err.println(e); }
        }
    }
}
