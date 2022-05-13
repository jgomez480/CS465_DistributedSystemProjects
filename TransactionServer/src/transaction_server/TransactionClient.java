package transaction_server;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import utils.PropLoader;

// Necessary imports util, net, etc

public class TransactionClient implements MessageTypes
{

    // Variables -------
    int numAccounts;
    int initialBalance;
    ArrayList<TransactionThread> transactionThreads;

    
    static String fileName;
    static String IP;
    static int PORT;
    static PropLoader filePropLoader;
    static Properties properties;
    
    // TODO: make integer constants final
    static int NUM_ACCOUNTS;
    static int NUM_TRANSACTIONS;
    static int INIT_BALANCE;

    final static String SPACE = " ";
    
    
    
    public TransactionClient()
    {
        // set values from ../config/*.properties files
        initializeProperties();
        transactionThreads = new ArrayList<TransactionThread>();
        
        // Run through for loop of number of transactions
        // Create thread for each transaction and start
        // clientTransaction = new TransactionThread();
        TransactionThread thread;
        int index;
        for (index = 0; index < NUM_TRANSACTIONS; index++)
        {
            thread = new TransactionThread();
            transactionThreads.add(thread);
            thread.start();
        }
        
        // Wait for all threads to finish
        for (index = 0; index < transactionThreads.size(); index++)
        {
            try 
            {
                transactionThreads.get(index).join();
            } 
            catch (InterruptedException e) { e.printStackTrace(); } 
        }
        
        TransactionClientProxy transaction = new TransactionClientProxy(IP, PORT);
        transaction.shutdown();
        
        // Finished transactions, shutdown
        System.out.println("");
        System.out.println("==================================== FINISHED, SHUTTING DOWN ======================================");
    }
    
    public static void main(String[] args)
    {
        // Create TransactionServer object, pass in server properties file
        new TransactionClient();
    }
    
    public int getBalanceToTake( Account accountA )
    {
       Random random = new Random();
       
       // random number from 1 to bound
       int returnVal = random.nextInt( accountA.getCurrentBalance() ) + 1;
       return returnVal; 
    }
    
    public void initializeProperties()
    {
        // Open Client properties file
        fileName = "src/config/Client.properties";
        filePropLoader = new PropLoader(fileName);
        properties = filePropLoader.getProperty();
        
        // Assign respective values for # of accounts, # of transactions and initial balance 
        NUM_ACCOUNTS = Integer.parseInt(properties.getProperty("NUMBER_OF_ACCOUNTS"));
        NUM_TRANSACTIONS = Integer.parseInt(properties.getProperty("NUMBER_OF_TRANSACTIONS"));
        INIT_BALANCE = Integer.parseInt(properties.getProperty("INITIAL_BALANCE"));
        
        // Open Server properties file
        fileName = "src/config/Server.properties";
        filePropLoader = new PropLoader(fileName);
        properties = filePropLoader.getProperty();
        
        // Assign values of the server IP and Port number
        IP = properties.getProperty("IP");
        PORT = Integer.parseInt(properties.getProperty("PORT"));

    }
    
    
    class TransactionThread extends Thread
    {
        public void run()
        {
            TransactionClientProxy transaction;
            int transactionID;
            int previousTID = -1;
            int balance;
            int accountFrom = 0;
            int accountTo = 0;
            int amount = 0;
            int returnCode;
            boolean committed = false;
            
            try 
            {
                Thread.sleep((int) Math.floor(Math.random() * 1000));
            } 
            catch (InterruptedException e) { e.printStackTrace(); }
            
            // keep trying until transaction is verified and committed to permanent storage
            while( !committed )
            {
                // client will request two accounts as well as the balance change
                // reference the two accounts (A, B) involved and the amount to transfer using random numbers
                
                transaction = new TransactionClientProxy(IP, PORT);
                
                // Call openTransaction on the TransactionProxy
                transactionID = transaction.open();
                // Returns transaction ID for printing
                if (previousTID != -1)
                {
                    System.out.println("Prior Transaction #" + previousTID + " restarted as Transaction #" + transactionID + 
                                       ", transfer $" + amount + ": " + accountFrom + "->" + accountTo);
                }
                else
                {
                    accountFrom = (int) Math.floor(Math.random() * NUM_ACCOUNTS);
                    accountTo = (int) Math.floor(Math.random() * NUM_ACCOUNTS);
                    amount = (int) Math.ceil(Math.random() * INIT_BALANCE);
                    System.out.println("Transaction #" + transactionID + " starts, transfer $" + amount + ": " + accountFrom + "->" + accountTo);
                }
                // Read the balance from account A
                balance = transaction.read(accountFrom);
                // Write the balance minus the determined amount to account A
                transaction.write(accountFrom, balance - amount);
                
                try 
                {
                    Thread.sleep((int) Math.floor(Math.random() * 500));
                } 
                catch (InterruptedException e) { e.printStackTrace(); }
                
                // Read the balance of account B
                balance = transaction.read(accountTo);
                // Write the balance plus the determined amount to account B
                transaction.write(accountTo, balance + amount);
                
                // Call closeTransaction on TrasactionProxy and return the completion status
                returnCode = transaction.close();
                
                switch(returnCode)
                {
                    case COMMITTED:
                        System.out.println("Transaction #" + transactionID + " COMMITTED");
                        committed = true;
                        break;
                    case ABORTED:
                        System.out.println("      Transaction #" + transactionID + " ABORTED");
                        previousTID = transactionID;
                        break;
                }
            }
        }
    }
}
