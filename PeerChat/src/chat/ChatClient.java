package chat;

import java.util.ArrayList;
import java.util.Scanner;

import message.MessageTypes;

public class ChatClient implements MessageTypes
{

    public static ArrayList<NodeInfo> activeClients = null;
    Sender sender;
    Receiver receiver;
    NodeInfo thisClientInfo;

    Scanner input;
    String ip;
    int port;
    String name;

    public ChatClient()
    {
        activeClients = new ArrayList<NodeInfo>();

        // Get client connection info and create node info
        input = new Scanner(System.in);
               
        initializeClient();
        
        sender.start();
        receiver.start();
            
    }

    public static void addNode(NodeInfo node)
    {
        activeClients.add(node);
    }

    public static void emptyList()
    {
        activeClients = new ArrayList<NodeInfo>();
    }

    public static ArrayList<NodeInfo> getActiveClients()
    {
        return activeClients;
    }

    // sets all NodeInfo values to initialize ChatClient
    public void initializeClient()
    {
        System.out.print("Enter your IP address: ");
        ip = input.next();
        System.out.print("Enter your port number: ");
        port = input.nextInt();
        System.out.print("Enter your name: ");
        name = input.next();
        System.out.println(" ");
        
        thisClientInfo = new NodeInfo(ip, port, name);
        
        // Create and start sender and receiver
        // pass in client to keep track of activeClients
        sender = new Sender(thisClientInfo, this);
        receiver = new Receiver(thisClientInfo, sender);
    }
    
    public static void main(String[] args)
    {
        new ChatClient();
    }

    public static void removeNode(NodeInfo node)
    {
        // Searches through clients and removes passed in node
        int index;
        int savedIndex = 0;
        NodeInfo listNode;
        for (index = 0; index < activeClients.size(); index++)
        {
            listNode = activeClients.get(index);
            if (listNode.equals(node))
            {
                // Saves index of matching node
                savedIndex = index;
            }
        }
        // Removes node after loop
        activeClients.remove(savedIndex);
    }
    
    public void shutdown()
    {
        sender.shutdown();
        receiver.shutdown();
        System.exit(1);
    }
}
