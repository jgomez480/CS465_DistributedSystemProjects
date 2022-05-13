package transaction_server;

// will hold the account information( account number and balance )

// will not implement deposits and withdrawals
public class Account {
	
	// initialize variables for account info and balance
	private static int accountNumber;
	private int currentBalance;
	
	/**
	 * Constructor: Accounts
	 * Approach: Sets the account number and balance to an instance of the class
	 * @param accountNumber
	 * @param currentBalance
	 */
	public Account(int accountNumber, int currentBalance)
	{
		// set up constructor for values
		this.accountNumber = accountNumber;
		this.currentBalance = currentBalance;
	}
	
	/*
	 * Constructor: Account
	 * Approach: copy constructor for transaction use
	 * @param tempAccount
	 */
	public Account( Account permAccount )
	{
	    // set temporary account values equal to permanent storage Account values
	    this.accountNumber = permAccount.getAccountNumber();
	    this.currentBalance = permAccount.getCurrentBalance();
	}

	/**
	 * Method: getCurrentBalance()
	 * Approach: Simple getter function to return the current balance of an account
	 * @return the current balance 
	 */
	public int getCurrentBalance()
	{
		// return the current balance
		return currentBalance;
	}
	/**
	 * Method: getAccountNumber()
	 * Approach: Simple getter function to return the appropriate account number 
	 * @return the account number of a particular account
	 */
	public static int getAccountNumber()
	{
		// return the account number
		return accountNumber;
	}
	/**
     * Method: setBalance()
     * Approach: setter function to set balance to given value 
     * @return nothing
     */
	public void setBalance( int balance )
	{
	    this.currentBalance = balance;
	}

}
