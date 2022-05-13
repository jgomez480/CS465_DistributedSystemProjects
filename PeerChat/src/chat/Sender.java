package chat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import message.Message;
import message.MessageTypes;

public class Sender extends Thread implements MessageTypes, Serializable
{

    // Variables
    final static String SPACE = " ";

    ChatClient chatClient;
    NodeInfo myNodeInfo;
    String name;
    Scanner input;
    String data;
    Message msg;
    boolean joined = false;

    String ipToConnect;
    int portToConnect;
    Socket socket;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;

    public Sender(NodeInfo node, ChatClient client)
    {
        chatClient = client;
        myNodeInfo = node;
        name = myNodeInfo.getName();
        input = new Scanner(System.in);
    }

    public void run()
    {

        while (true)
        {
            data = input.nextLine();
            if (data.compareTo("STATUS") == 0) // DEBUG - shows status of list
            {
                ArrayList<NodeInfo> activeClients = ChatClient.getActiveClients();
                for (int i = 0; i < activeClients.size(); i++)
                {
                    System.out.println(
                            "Index " + i + " Name: " + activeClients.get(i).getName()
                                    + " Port: " + activeClients.get(i).getPort());
                }
                continue;
            } 

            msg = createMessage(data);

            if (msg == null)
            {
                System.out.println("Incorrect command format");
            } else
            {
                boolean send = true; // Whether the message should be sent at all
                boolean sendAll = true; // Whether message should be sent to every client
                                        // or just one
                switch (msg.getType())
                {
                case JOIN:
                    if (!joined)
                    {
                        if (msg.getContent() != null) // JOIN command with IP and PORT to
                                                      // connect
                        {
                            sendAll = false;
                            joined = true;
                            // set propagate to true
                            msg.setPropagate(true);
                            System.out.println("Joining Chat Application...");
                        } else // Single JOIN command
                        {
                            send = false;
                            joined = true;
                            ChatClient.activeClients.add(myNodeInfo);
                            System.out.println("Initiated Chat Client");
                        }
                    } else
                    {
                        send = false;
                        System.out.println("Already Joined");
                    }
                    break;
                case LEAVE:
                    if (!joined)
                    {
                        send = false;
                        System.out.println("Must Join Chat to Leave");
                    } else
                    {
                        joined = false;
                        System.out.println("Leaving chat...");

                    }
                    break;
                case SHUTDOWN:
                    if (joined)
                    {
                        send = false;
                        System.out.println("Must first leave chat to shutdown");
                    } else
                    {
                        System.out.println("Shutting down chat client...");
                        chatClient.shutdown();
                    }
                    break;
                case NOTE:
                    if (!joined)
                    {
                        send = false;
                        System.out.println("Must First Join Chat to Send Messages");
                    }
                    break;
                }
                try
                {
                    if (send)
                    {
                        if (sendAll)
                        {
                            sendToAll(msg);
                        } else
                        {
                            sendToOne(msg);
                        }
                        if (msg.getType() == LEAVE)
                        {
                            ChatClient.emptyList();
                        }
                    }
                } catch( Exception e)
                {
                    System.err.println( "Error sending message" );
                    System.exit(1);
                }
            }
        }
    }

    public void sendToOne(Message message)
    {
        try
        {
            // Open connection to server and create input output streams
            socket = new Socket(ipToConnect, portToConnect);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // Send message object through data stream
            outputStream.writeObject((Object) msg);

            // If JOIN command, will read from connection for any NodeInfo needed to add
            // to list.
            Message receivedMessage = (Message) inputStream.readObject();
            while (receivedMessage != null)
            {
                ChatClient.addNode((NodeInfo) receivedMessage.getContent());
                receivedMessage = (Message) inputStream.readObject();
            }
            socket.close();
        } catch (Exception e)
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    public void sendToAll(Message message)
    {
        ArrayList<NodeInfo> activeClients = ChatClient.getActiveClients();

        for (NodeInfo node : activeClients)
        {
            if (myNodeInfo.equals(node)) // Don't connect to self
            {
                continue;
            }
            try
            {
                // Open connection to server and create input output streams
                socket = new Socket(node.getIP(), node.getPort());
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                // Send message object through data stream
                outputStream.writeObject((Object) message);
                socket.close();
            } catch (Exception e)
            {
                System.out.println(e);
                System.exit(1);
            }
        }
    }

    public Message createMessage(String data)
    {
        if (data.startsWith("JOIN"))
        {
            String[] dataParts = data.split(SPACE);

            if (dataParts.length == 1) // If singular JOIN command
            {
                return new Message(JOIN, null);
            } 
            else if (dataParts.length >= 3) // If targeted JOIN <IP> <PORT> command
            {
                ipToConnect = dataParts[1];
                portToConnect = Integer.parseInt(dataParts[2]);
                return new Message(JOIN, myNodeInfo);
            } 
            else // Incorrect JOIN command format
            {
                return null;
            }
        } 
        else if (data.startsWith("LEAVE"))
        {
            return new Message(LEAVE, myNodeInfo);
        } 
        else if (data.startsWith("SHUTDOWN"))
        {
            return new Message(SHUTDOWN, myNodeInfo);
        } 
        else
        {   // NOTE
            data = name + ":\t" + data;
            return new Message(NOTE, data);
        }
    }

    public void shutdown()
    {
        System.exit(1);
    }
}
