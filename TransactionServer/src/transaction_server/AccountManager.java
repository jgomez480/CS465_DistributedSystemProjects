package transaction_server;

import java.util.ArrayList;

public class AccountManager 
{

    // Variables
    private static ArrayList<Account> accountList;
    private static int numberAccounts;
    private static int initialBalance;
    
    public AccountManager(int numberAccounts, int initialBalance)
    {
        // Initialize list of accounts
        AccountManager.accountList = new ArrayList<Account>();
        AccountManager.numberAccounts = numberAccounts;
        AccountManager.initialBalance = initialBalance;
        int accountIndex;
        
        
        // Loop through total number of accounts
            // Create new account with initial balance
            // then add to list
        for(accountIndex = 0; accountIndex < numberAccounts; accountIndex++)
        {
            Account tempAccount = new Account( accountIndex, initialBalance );
            accountList.add(tempAccount);
        }
    }
    
    public Account getAccount(int accountNumber)
    {
        return accountList.get(accountNumber);
    }
    
    
    public static ArrayList<Account> getAccounts()
    {
        return accountList;
    }
    
    public int getTotalBalances()
    {
        int total = 0;
        for (Account account : accountList)
        {
            total += account.getCurrentBalance();
        }
        return total;
    }

    public int read(int accountNumber)
    {
        // Get account using accountNumber from list
        Account account = getAccount(accountNumber);
        // Call account.getBalance()
        int balance = account.getCurrentBalance();
        return balance;
    }
    
    // Called at the very end in TransactionManager after validation
    public void write(int accountNumber, int balance)
    {
        // Get account using accountNumber from list
        Account account = getAccount(accountNumber);
        // Call account.getBalance()
        account.setBalance(balance);
    }
}
