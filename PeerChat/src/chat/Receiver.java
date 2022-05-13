package chat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import message.Message;
import message.MessageTypes;

public class Receiver extends Thread implements MessageTypes, Serializable
{

    // Variables
    NodeInfo myNodeInfo;
    int portNumber;
    ServerSocket serverSocket;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;
    Sender sender;

    public Receiver(NodeInfo node, Sender sender)
    {
        myNodeInfo = node;
        portNumber = myNodeInfo.getPort();
        this.sender = sender;
    }

    public void handleMessage(Message received, ServerSocket socket)
    {
        NodeInfo receivedNode = null;
        switch (received.getType())
        {
        case JOIN:
            // Send list back to client for them to update
            if (received.getPropagate())
            {
                sendListBack(socket);
            }
            // Add their node into list
            receivedNode = (NodeInfo) received.getContent();
            ChatClient.addNode(receivedNode);
            System.out.println(receivedNode.getName() + " Joined Chat.");
            // if message has not yet been sent to all nodes in mesh
            if (received.getPropagate())
            {
                received.setPropagate(false);
                // send to every node including itself
                sender.sendToAll(received);
            }
            break;
        case LEAVE: // Remove client from list
            receivedNode = (NodeInfo) received.getContent();
            ChatClient.removeNode(receivedNode);
            System.out.println(receivedNode.getName() + " Left Chat.");
            break;
        case NOTE: // Print out the data in message object content
            System.out.println((String) received.getContent());
            break;
        }
    }

    public void run()
    {
        while (true)
        {
            try
            {
                // Accept next connection to server
                serverSocket = new ServerSocket(portNumber);
                Socket socket = serverSocket.accept();

                // Open input and output streams
                inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());

                // Read message object through data stream
                Message receivedMessage = null;
                receivedMessage = (Message) inputStream.readObject();

                // perform corresponding operations based on message received
                handleMessage(receivedMessage, serverSocket);

                serverSocket.close();
            } catch (Exception e)
            {
                System.err.println(e);
                shutdown();
            }
        }
    }

    // Passing in active socket with client. Loops through current list sending each
    // one.
    // Client will add each into their own list
    public void sendListBack(ServerSocket socket)
    {
        try
        {
            // Get current client list
            ArrayList<NodeInfo> activeClients = ChatClient.getActiveClients();
            Message nodeMsg;

            // Sends each of the clients to the newly added client for their list
            for (int index = 0; index < activeClients.size(); index++)
            {
                nodeMsg = new Message(JOIN, activeClients.get(index));
                outputStream.writeObject((Object) nodeMsg);
            }

            // Writing a null as last object tell receiver its end of stream
            outputStream.writeObject(null);
            serverSocket.close();
        } catch (Exception e)
        {
            System.err.println(e);
            shutdown();
        }
    }

    public void shutdown()
    {
        System.exit(1);
    }
}
