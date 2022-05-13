package transaction_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import utils.PropLoader;

public class TransactionServer {
	
    // static server variables
    static String fileName;
    static PropLoader filePropLoader;
    static Properties properties;
    private static int msgCount = 0;
    private static boolean running = true;
   
    // TODO: make constants final
    static String IP;
    static int PORT;
    static int NUM_ACCOUNTS;
    static int NUM_TRANSACTIONS;
    static int INIT_BALANCE;
    
    static TransactionServer self;
	// create variable for accounts manager
    static AccountManager accountManager;
    
    static Account account;
	// create variable for transaction manager
	static TransactionManager transactionManager;
	// set up and initialize server socket
	ServerSocket serverSocket;
	Socket socket;
	
	public TransactionServer(String propertiesFile) // TODO: do we need to pass in the file?
	{
	    self = this;
	    
		// initialize external server properties
	    initializeServerProperties();
	
		// initialize new instance of transaction manager
	    transactionManager = new TransactionManager();
	    accountManager = new AccountManager(NUM_ACCOUNTS, INIT_BALANCE);
	    System.out.println("[TransactionServer.TransactionServer] TransactionManager created");
	    System.out.println("[TransactionServer.TransactionServer] AccountManager created");
		// start server socket with properties
	    try 
	    {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[TransactionServer.TransactionServer] ServerSocket created");
        } 
	    catch (IOException e) { e.printStackTrace(); } 
	    run( serverSocket );
	}
	
	public static synchronized int getMsgCount() // Output line counter
	{
	    return msgCount++;
	}
	
	public void initializeServerProperties()
	{
	    // Get server properties file
	    fileName = "src/config/Server.properties";
        filePropLoader = new PropLoader(fileName);
        properties = filePropLoader.getProperty();

        // Get connection info
        IP = properties.getProperty("IP");
        PORT = Integer.parseInt(properties.getProperty("PORT"));
        
        // Open client properties file
        fileName = "src/config/Client.properties";
        filePropLoader = new PropLoader(fileName);
        properties = filePropLoader.getProperty();
        
        // Get the properties of accounts and initial balance
        NUM_ACCOUNTS = Integer.parseInt(properties.getProperty("NUMBER_OF_ACCOUNTS"));
        NUM_TRANSACTIONS = Integer.parseInt(properties.getProperty("NUMBER_OF_TRANSACTIONS"));
        INIT_BALANCE = Integer.parseInt(properties.getProperty("INITIAL_BALANCE"));
	}
	
	public void run( ServerSocket serverSocket )
	{
	     try 
	     {
    		while (running)
    		{
                // Take connections from clients 
                socket = serverSocket.accept();
                // Run the transaction server socket using TransactionManager
                transactionManager.runTransaction(socket);
    		}
            socket.close();
        } catch (Exception e) { System.out.println("[TransactionServer.run] Socket closed, shutting down...\n\n"); }

	    // Shutdown Program
		System.out.println("========================== BRANCH TOTAL ===========================");
		int totalBalances = accountManager.getTotalBalances();
		System.out.println("---> $" + totalBalances);
		System.exit(0);
	}

	public static void shutdown()
	{
	    try 
	    {
            self.serverSocket.close();
        } 
	    catch (IOException e) { e.printStackTrace(); }
	}
	
	public static void main(String[] args)
	{
	    // Create TransactionServer object, pass in server properties file
	    new TransactionServer("../config/Server.properties");
	}
}
