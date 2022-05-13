package package_1;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;

import message.Message;
import message.MessageTypes;
import utils.PropLoader;

public class Sender extends Thread implements MessageTypes, Serializable
{

    static String filename = "src/config/client.properties";
    static PropLoader pl = new PropLoader(filename);
    static Properties clientProp = pl.getProperty();

    final static String IP = clientProp.getProperty("CLIENT_IP");
    final static int PORT = Integer.parseInt(clientProp.getProperty("CLIENT_PORT"));
    final static String SPACE = " ";

    // Initialize variables
    ChatClient client;
    Scanner input; // Keyboard input
    Socket socket;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;

    String name; // Logical name prepended to messages
    int localPort; // Used for reciever to open connection on this port
    Receiver receiver;

    public Sender(ChatClient newClient)
    {
        client = newClient;
    }

    public void run()
    {

        System.out.println("IP is: " + getMyIP());

        Message message;
        String data;
        input = new Scanner(System.in);

        while (true)
        {
            data = input.nextLine();
            try
            {
                // Open connection to server and create input output streams
                socket = new Socket(IP, PORT);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e)
            {
                System.out.println("Error: " + e);
                System.exit(1);
            }

            // Create message object
            message = createMessage(data);

            if (message == null)
            {
                continue;
            } // If there was error with message creation skip loop and get next input
            if (message.getType() == SHUTDOWN)
            {
                continue;
            } // If shutdown dont send message
            if (message.getType() == NOTE)
            {
                if (!client.hasJoined())
                {
                    // If not joined and attempting to send note dont send message
                    System.out.println(
                            "You must first join chat in order to send messages");
                    continue;
                } else
                {
                    // Else the logical name is added to the end of message
                    message.setContent((String) message.getContent() + " - " + name);
                }
            }
            if (message.getType() == JOIN)
            {
                if (!client.hasJoined())
                {
                    // If join command, save name and port for use with receiver and note
                    // messages
                    client.joinedChat();
                    System.out.println("Joining Chat");
                    NodeInfo temp = (NodeInfo) message.getContent();
                    name = temp.getName();
                    localPort = temp.getPort();

                    // Start receiver with port
                    if (receiver != null)
                    {
                        receiver.interrupt();
                    }
                    receiver = new Receiver(localPort);
                    receiver.start();
                } else
                {
                    System.out.println("You have already joined the chat");
                    continue;
                }
            }
            if (message.getType() == LEAVE)
            {
                if (!client.hasJoined())
                {
                    System.out.println("You must join chat first to leave chat");
                } else
                {
                    System.out.println("Leaving Chat");
                    client.leftChat();
                }
            }

            try
            {
                // Send message object through data stream
                outputStream.writeObject((Object) message);
            } catch (Exception e)
            {
                System.out.println(e);
            }

        }
    }

    // Create message object from inputed data string
    private Message createMessage(String data)
    {
        Message message;
        int type;
        NodeInfo nodeInfo;

        // If string starts with JOIN or LEAVE, set type and create a NodeInfo object
        // out of input data
        // Create message using type and nodeInfo object
        if (data.startsWith("JOIN"))
        {
            if (data.split(SPACE).length < 4)
            {
                System.out.println("You must enter all info <CMD> <IP> <PORT> <NAME>");
                return null;
            }
            type = JOIN;
            nodeInfo = createNodeInfo(data);
            message = new Message(type, nodeInfo);
        } else if (data.startsWith("LEAVE"))
        {
            if (data.split(SPACE).length < 4)
            {
                System.out.println("You must enter all info <CMD> <IP> <PORT> <NAME>");
                return null;
            }
            type = LEAVE;
            nodeInfo = createNodeInfo(data);
            message = new Message(type, nodeInfo);
        }
        // If message is of type SHUTDOWN set type to SHUTDOWN and terminate program
        else if (data.startsWith("SHUTDOWN"))
        {
            type = SHUTDOWN;
            message = null;

            if (client.hasJoined())
            {
                System.out.println("Chat must first be left in order to shutdown");
            } else
            {
                // Shutdown
                receiver.interrupt();
                client.shutdown();
            }
        }
        // If message does not start with JOIN LEAVE or SHUTDOWN set type NOTE and send
        // full message to server
        else
        {
            type = NOTE;
            message = new Message(type, data);
        }

        return message;
    }

    // Create a NodeInfo Object using inputed data string
    private NodeInfo createNodeInfo(String data)
    {
        // Splits the string into by space char
        String[] splitString = data.split(SPACE);
        // Get NodeInfo data from array format
        // [MESSAGETYPE, IP, PORT, NAME]
        String ip = splitString[1];
        int port = Integer.parseInt(splitString[2]);
        String name = splitString[3];

        return new NodeInfo(ip, port, name);
    }

    // Function to return the user's ip for connection purposes
    public String getMyIP()
    {
        try
        {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()
                        || networkInterface.isVirtual()
                        || networkInterface.isPointToPoint())
                {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements())
                {
                    InetAddress address = addresses.nextElement();

                    final String myIP = address.getHostAddress();
                    if (Inet4Address.class == address.getClass())
                    {
                        return myIP;
                    }
                }
            }
        } catch (SocketException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }
}
