package transaction_server;

import java.util.ArrayList;
import java.util.HashMap;

// will house getters and setters for ID's and reading and writing actions.
public class Transaction
{
	// init variables for id and account info
    private int transactionID;
    private int transactionNumber;

    // keep track of transaction numbers during working phase
    private int firstTransactionNumber;
    private int lastTransactionNumber;

    // Read and Write List
    private ArrayList<Integer> accountIDReadList;
    private HashMap<Integer, Integer> accountIDWriteList;
    
    // create temporary account object to use until transaction is verified & committed
    Account tempAccount;

    
    // TODO: transaction keep track of client connection info to send success or failure?
    // should be somewhere in TransactionManager mayhaps

	public Transaction( int transactionID, int lastCommittedTID )
	{
		// create instance of id
	    this.transactionID = transactionID;
	    firstTransactionNumber = lastCommittedTID;
	    accountIDReadList = new ArrayList<Integer>();
	    accountIDWriteList = new HashMap<Integer, Integer>();
	}
	
	public int getFirstTransactionNum()
	{
	    return firstTransactionNumber;
	}
	
	public int getID()
	{
		return transactionID;
	}
	
	public int getLastTransactionNum()
	{
	    return lastTransactionNumber;
	}
	
	public void setLastTransactionNum(int number)
	{
	    lastTransactionNumber = number;
	}
	
	public ArrayList<Integer> getReadList()
	{
	    return accountIDReadList;
	}
	
	public HashMap<Integer, Integer> getWriteList()
	{
	    return accountIDWriteList;
	}
	
	public HashMap<Integer, Integer> getWriteSet()
	{
	    return accountIDWriteList;
	}
	
   public int read(int accountNumber)
    {
        Integer balance = accountIDWriteList.get(accountNumber);
        // Using write set check if value to be read was written by this transaction
        if (balance == null)
        {
            // Read balance using AccountManager.read(accountNumber)
            balance = TransactionServer.accountManager.read(accountNumber);
        }
        
        if (!accountIDReadList.contains(accountNumber))
        {
            accountIDReadList.add(accountNumber);
        }
        return balance; // Return the balance
    }
	
	public void setTransactionNum( int num )
	{
	    this.transactionNumber = num;
	}

	public int write(int accountNumber, int balance)
	{
	    // add accountNumber to write set
	    accountIDWriteList.put(accountNumber, balance);
	    return 0; // Temporary stub return
	}

}
