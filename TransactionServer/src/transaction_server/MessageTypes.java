package transaction_server;

public interface MessageTypes {

    // Symbolic static constants

    // Messages sent from client
    public static final int OPEN = 0;
    public static final int CLOSE = 1;
    public static final int READ = 2;
    public static final int WRITE = 3;

    // Messages sent from server
    public static final int COMMITTED = 4;
    public static final int ABORTED = 5;
    
    // Shutdown the server
    public static final int SHUTDOWN = 6;
}
