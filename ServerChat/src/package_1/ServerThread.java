package package_1;

import message.Message;
import message.MessageTypes;

import java.io.*;
import java.util.*;
import java.net.*;

public class ServerThread extends Thread implements Serializable, message.MessageTypes {

    Socket clientSocket;
    ArrayList<NodeInfo> clientList;
    NodeInfo nodeInfo;
    Message msgToSend;
    
    public ServerThread(Socket clientSocket, ArrayList<NodeInfo> clientList) {
        this.clientSocket = clientSocket;
        this.clientList = clientList;
    }
    
    public void run() {
        
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;

        try {
            // Open the input and output streams
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error opening network streams");
            return;
        }
        
        Message message = null;
        try {
            // Read message object from client
            message = (Message) inputStream.readObject();
        } catch (Exception e) { 
            System.out.println("Message Object Null"); 
            return;
        } 
        
        System.out.println("Received message, type sent: " + message.getType() + " Object Sent: " + message.getContent());
        
        int index;
        // Switch statement logic for each message type
        switch (message.getType()) {
            case JOIN: // If JOIN add client connection information to clientList
                nodeInfo = (NodeInfo) message.getContent();
                clientList.add(nodeInfo);
                System.out.println("Adding client " + nodeInfo.getName());
                break;
            case LEAVE: // if LEAVE remove client connection information from clientList

                nodeInfo = (NodeInfo) message.getContent();
                NodeInfo currentInfo;
                // Run through list to check for nodeInfo match
                for (index = 0; index < clientList.size(); index++) {
                    currentInfo = clientList.get(index);
                    // If all parameters of nodeInfo match to element in client list
                    // Remove the clients information from the list
                    if (nodeInfo.equals(currentInfo)) {
                        System.out.println("Client info matches, removing " + currentInfo.getName() + " from list");
                        clientList.remove(index);
                    }
                }
                break;
            case NOTE: // if NOTE send message to all clients using connection info in clientList
                NodeInfo connectionInfo;
                msgToSend = new Message(NOTE, (String) message.getContent());
                
                Socket socket;
                for (index = 0; index < clientList.size(); index++) {
                    // Go through each NodeInfo in list, using that connectivity info, open socket connection to it.
                    // Once connected send the message and close the connection
                    System.out.println("Sending message to clients");
                    connectionInfo = clientList.get(index);
                    System.out.println("Connecting to IP: " + connectionInfo.getIP() + " and port: " + connectionInfo.getPort());
                    try {
                        socket = new Socket(connectionInfo.getIP(), connectionInfo.getPort());
                        inputStream = new ObjectInputStream(socket.getInputStream());
                        outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(msgToSend);
                        socket.close();
                    } catch (Exception e) { System.out.println(e); }
                }
                break;
        }
        try {
            // Close socket connection
            clientSocket.close();
        } catch (Exception e) { System.out.println("Socket Closed"); }
    }
}
