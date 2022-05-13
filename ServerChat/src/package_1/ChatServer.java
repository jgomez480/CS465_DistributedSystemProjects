package package_1;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import utils.PropLoader;

/*Notes: 
 * - Server ONLY listens on server socket
 * - Opens sockets to all clients
 * - Server has only 1 SERVER SOCKET
 * 
 * - Connections are NOT constant
 * - Server will multiplex message to ALL clients
 * - Server needs data structure of joined clients
 * 
 * - JOIN --> sent to server --> connection shutdown --> iterate JOIN's when 
 * 	message received (by callback #)
 * */

//SUPRESSED serialUID warning 
//(The Serializable class ChatServer does not declare a static final serialVersionUID field of type long)
@SuppressWarnings("serial")
public class ChatServer extends Thread implements Serializable
{
    static String filename = "src/config/server.properties";
    static PropLoader pl = new PropLoader(filename);
    static Properties serverProp = pl.getProperty();

    final static String IP = serverProp.getProperty("SERVER_IP"); // TODO guarantee IP is
                                                                  // used
    final static int PORT = Integer.parseInt(serverProp.getProperty("SERVER_PORT"));

    public static void main(String[] args)
    {
        // Create new ArrayList of threads
        // Data Structure for connected client list
        ArrayList<NodeInfo> clientList = new ArrayList<>();
        try
        {
            // Creating object for server side socket
            ServerSocket serversocket = new ServerSocket(PORT);

            // verify server is listening on port
            System.out.println("Server Listening");

            while (true)
            {
                // Accept next connection to server
                Socket socket = serversocket.accept();

                // Create new thread for connection
                ServerThread clientThread = new ServerThread(socket, clientList);

                // starting the client thread
                clientThread.start();
            }
        } catch (Exception e)
        {
            // print statement for testing, showing catch
            System.out.println("CATCH OCCURED");
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
    }
}
