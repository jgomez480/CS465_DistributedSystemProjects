package transaction_server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// Holds the client connection to Transaction Server
// Acts as Transaction API for client
public class TransactionClientProxy implements MessageTypes {

    // Variables -----
    Socket socket;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;
    Message msgToSend;
    Message recvMsg;
    
    // TODO: assign transaction ID to transactions to handle committing on server side

    public TransactionClientProxy(String hostName, int portNumber)
    {
        try {
            // Open socket connection with ip and port
            socket = new Socket(hostName, portNumber);
            // Create and assign ObjectOutput and Input Streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } 
        catch (Exception e) { System.out.println(e); }

    }

    public int open()
    {
        int transactionID = -1;
        try {
            // Create Message with type OPEN
            msgToSend = new Message(OPEN, null);
            // Write Message object
            outputStream.writeObject(msgToSend);
            // Read Object from server (transactionID)
            recvMsg = (Message) inputStream.readObject();
            transactionID = (int) recvMsg.getContent();
        }
        catch (Exception e) { System.out.println("ERROR: " + e); System.exit(0); }
        
        // Return TransactionID
        return transactionID;
    }

    public int close()
    {
        int returnMessage = -1;
        try 
        {
            // Create Message with type CLOSE
            msgToSend = new Message(CLOSE, null);
            // Write Message object
            outputStream.writeObject(msgToSend);
            // Read Message from server
            recvMsg = (Message) inputStream.readObject();
            returnMessage = recvMsg.getType();
        }
        catch (Exception e) { e.printStackTrace(); }
        
        // Return messageType from message object
        return returnMessage;
    }

    public void write(int accountNumber, int newBalance)
    {
        try 
        {
            Object[] data = new Object[2];
            data[0] = accountNumber;
            data[1] = newBalance;
            // Create Message with type WRTIE, with content accountNumber and balance
            msgToSend = new Message(WRITE, data);
            // Write Message object
            outputStream.writeObject(msgToSend);
        } 
        catch (Exception e) { e.printStackTrace(); }
    }

    public int read(int accountNumber)
    {
        int balance = -1;
        try 
        {
            // Create Message with type WRTIE, with content accountNumber and balance
            msgToSend = new Message(READ, accountNumber);
            // Write Message object
            outputStream.writeObject(msgToSend);
            // Read Message from server
            recvMsg = (Message) inputStream.readObject();
            // Get balance content from Message Object
            balance = (int) recvMsg.getContent();
        }
        catch (Exception e) { e.printStackTrace(); }

        // Return balance
        return balance;
    }
    
    public void shutdown()
    {
        try
        {
            // Create message with type SHUTDOWN
            msgToSend = new Message(SHUTDOWN, null);
            // Write Message object
            outputStream.writeObject(msgToSend);
            // Close socket
            socket.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }
}
