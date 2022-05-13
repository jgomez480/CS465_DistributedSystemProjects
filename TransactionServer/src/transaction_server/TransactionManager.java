package transaction_server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MANAGES TRANSACTIONS (ID's & Logging Information)
 *
 * Transaction File to handle transaction worker threads and
 * supply loop for transaction operations.
 *
 * Serves as a container for the transactions, keeping track of active transactions in a list
 *
 */
public class TransactionManager implements MessageTypes
{
    // initialize transactionID
    int transactionID;
    // string for temporary output operations
    String output = "";
    private ArrayList<Transaction> workingTransactions;
    private ArrayList<Transaction> abortedTransactions;
    private ArrayList<Transaction> committedTransactions;
    
    // String for output storage
    String outputStorage = "";
    
    // 1. need to write to permanent storage in AccountManager from TransactionManager when a transaction is being committed
    // 2. need to be able to send response to the TransactionClientProx, but
    //    but abortTransaction is within the transaction class, and commit/writeTransaction is within  TransactionServer
    
    // pass in AccountManager?
    public TransactionManager()
    {
        transactionID = -1;
        workingTransactions = new ArrayList<Transaction>();
        abortedTransactions = new ArrayList<Transaction>();
        committedTransactions = new ArrayList<Transaction>();
    }
    
    public void addToOutput( String outputLine )
    {
        outputStorage = outputStorage + outputLine;
    }

    // returns true if any conflicts were found
    public boolean checkReadVsWrite( Transaction recent, Transaction previous )
    {
        boolean returnVal = false;
        Set keySet = previous.getWriteList().keySet();
        
        // Check for any intersections between the read set 
        // of recent vs the write set of previous ( double check me on that )
        for( Integer currentRead : recent.getReadList() )
        {
            if( keySet.contains( currentRead ) )
            {
                returnVal = true;
                int tid = recent.getID();
                output = TransactionServer.getMsgCount() + " Transaction ID#: " + tid + 
                        "[TransactionManager.validateTransaction] Transaction ID#: " + tid + 
                        " failed: r/w conflict on account #: " + currentRead + " with Transaction ID#: " + previous.getID() + "\n";
                
                displayAndStore( output );
            }
        }
        
        return returnVal;
    }
    
    public void displayAndStore( String outputLine )
    {
        System.out.print( outputLine );
        addToOutput( outputLine );
    }
    
	// counter to keep track of the number of transactions
	public int getNextID()
	{
	    transactionID++;
	    return transactionID;
	}
	
	public Transaction createTransaction()
	{
	    int id = getNextID();
	    int first = committedTransactions.size();
	    Transaction transaction = new Transaction(id, first);
	    workingTransactions.add(transaction);
	    return transaction;
	}

	public ArrayList<Transaction> getWorkingTransactions()
	{
	    return this.workingTransactions;
	}

	public ArrayList<Transaction> getAbortedTransactions()
    {
        return this.abortedTransactions;
    }

	public ArrayList<Transaction> getCommittedTransactions()
    {
        return this.committedTransactions;
    }

	public void runTransaction(Socket socket)
	{
	    // Pass off socket to new TransactionManagerWorker thread
	    TransactionManagerWorker workerThread = new TransactionManagerWorker(socket);
	    // Start thread
	    workerThread.start();
	}
	
	public boolean testAndCommit( Transaction transaction )
	{
	    // Use a synchronized block to only ever validate & commit one transaction at a time
	    boolean isValid = false;
	    synchronized (this)
	    {
	        isValid = validateTransaction( transaction );
	        if( isValid )
	        {
	            writeTransaction( transaction );
	            committedTransactions.add(transaction);
	        }
	        else 
	        {
	            abortedTransactions.add(transaction);
	        }
	        workingTransactions.remove(transaction);
	    }
	    
	    return isValid;
	}

    public boolean validateTransaction(Transaction transaction)
    {
        boolean returnVal = true;
        boolean conflicts = false;
        
        transaction.setLastTransactionNum(committedTransactions.size() - 1);
        transaction.setTransactionNum( committedTransactions.size() ); // need to keep indexing in mind for possible bugs
        
        ArrayList<Transaction> transactionWindow = new ArrayList<Transaction>();

        // grab only committed transactions that occurred during the working phase
        transactionWindow = new ArrayList<Transaction> (committedTransactions.subList
                (transaction.getFirstTransactionNum(), transaction.getLastTransactionNum() + 1));
        
        for ( Transaction committed : transactionWindow )
        {
            // check read set of current transaction against write set
            conflicts = checkReadVsWrite( transaction, committed );
            
            // only one conflict needs to occur to fail the verification
            if( conflicts )
            {
                returnVal = false;
                return returnVal;
            }
        }
        int tid = transaction.getID();
        if (returnVal)
        {
            output = TransactionServer.getMsgCount() + " Transaction ID#: " + tid + 
                    " [TransactionManager.validateTranasaction] Transaction ID#: " + 
                    tid + " successfully validated\n";
            displayAndStore( output );
        }
        return returnVal;
    }

    public void writeTransaction(Transaction transaction)
    {
        int accNum;
        int balance;
        int tid = transaction.getID();
        HashMap<Integer, Integer> writeSet = transaction.getWriteSet();
        
        // Loop through write set of transaction (tempAccountStorage ArrayList)
        for (Map.Entry element : writeSet.entrySet())
        {
            accNum = (int) element.getKey();
            balance = (int) element.getValue();
            // and overwrite permanent storage ( link into AccountManager somehow )
            TransactionServer.accountManager.write(accNum, balance);
        }
        output = TransactionServer.getMsgCount() + " Transaction ID#: " + tid + 
                " [TransactionManager.writeTransaction] Transaction ID#: " + tid + " written\n";
        displayAndStore( output );
    }
}
