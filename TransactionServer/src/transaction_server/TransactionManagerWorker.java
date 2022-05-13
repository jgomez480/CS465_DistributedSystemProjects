package transaction_server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/* Class : transactionManagerWorker()
 * Loops through and reads messages from client.
 * 
 * This class will be instantiated in the SERVER LOOP
 * 
 */
public class TransactionManagerWorker extends Thread implements MessageTypes
{

    // Variables ---
    Socket socket;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;
    Message readMsg;
    Message sendMsg;
    Transaction transaction;
    int transactionID;

    public TransactionManagerWorker(Socket socket)
    {
        try
        {
            // Assign socket
            this.socket = socket;
            // Open ObjectInputStream
            inputStream = new ObjectInputStream(socket.getInputStream());
            // Open ObjectOuputStream
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (Exception e) { System.out.println(e); }
    }
    
    public void run()
    {
        int accNumber;
        int balance;
        boolean transactionResult;
        boolean transactionClosed = false;
        Object[] content;
        
        // While the transaction is still active
        while (!transactionClosed)
        {
            try 
            {
                // Read Message from serverSocket
                readMsg = (Message) inputStream.readObject();
                
                // Switch statement of messageType
                switch (readMsg.getType())
                {
                    case OPEN:
                        // Create Transaction
                        transaction = TransactionServer.transactionManager.createTransaction();
                        // Create Message with TID
                        transactionID = transaction.getID();
                        sendMsg = new Message(OPEN, transactionID);
                        System.out.println(TransactionServer.getMsgCount() + " Transaction #" + transactionID + " [TransactionMangerWorker.run] OPEN_TRANSACTION #" + transactionID);
                        // Write Message Object
                        outputStream.writeObject(sendMsg);
                        break;
                    case CLOSE:
                        transactionClosed = true;
                        // Call validateTransaction
                        transactionResult = TransactionServer.transactionManager.testAndCommit(transaction);
                        // If it returns true create Message with COMMITED and writeTransaction
                        if (transactionResult)
                        {
                            sendMsg = new Message(COMMITTED, null);
                            System.out.println(TransactionServer.getMsgCount() + " Transaction #" + transactionID + 
                                    " [TransactionMangerWorker.run] CLOSE_TRANSACTION # " + transactionID + " - COMMITTED");
                        }
                        // else create Message with ABORTED and abort transaction
                        else
                        {
                            sendMsg = new Message(ABORTED, null);
                            System.out.println(TransactionServer.getMsgCount() + " Transaction #" + transactionID + 
                                    " [TransactionMangerWorker.run] CLOSE_TRANSACTION # " + transactionID + " - ABORTED");
                        }
                        // Write Message Object
                        outputStream.writeObject(sendMsg);
                        break;
                    case READ:
                        // Get accountNumber
                        accNumber = (int) readMsg.getContent();
                        System.out.println(TransactionServer.getMsgCount() + " Transaction #" + transactionID + 
                                " [TransactionMangerWorker.run] READ_REQUEST >>>>>>>>>>>>>>>>> account #" + accNumber);
                        // Get balance using accountNumber
                        balance = transaction.read(accNumber); 
                        // Create Message with balance
                        sendMsg = new Message(READ, balance);
                        System.out.println(TransactionServer.getMsgCount() + " Transaction #" + transactionID + 
                                " [TransactionMangerWorker.run] READ_REQUEST <<<<<<<<<<<<<<<<< account #" + accNumber + ", balance $" + balance);
                        // Write Message Object
                        outputStream.writeObject(sendMsg);
                        break;
                    case WRITE:
                        content = readMsg.getContents();
                        // Get accountNumber from content
                        accNumber = (int) content[0];
                        // Get newBalance from content
                        balance = (int) content[1];
                        sendMsg = new Message(READ, balance);
                        System.out.println(TransactionServer.getMsgCount() + " Transaction #" + transactionID + 
                                " [TransactionMangerWorker.run] WRITE_REQUEST >>>>>>>>>>>>>>>>> account #" + accNumber + ", new balance $" + balance);
                        // Write balance using accountNumber
                        transaction.write(accNumber, balance);
                        break;
                    case SHUTDOWN:
                        TransactionServer.shutdown();
                        transactionClosed = true;
                }
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        
        try 
        {
            socket.close();
        } 
        catch (IOException e) { e.printStackTrace(); }
    }
}
