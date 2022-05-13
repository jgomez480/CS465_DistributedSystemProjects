package package_1;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import message.Message;
import message.MessageTypes;
import utils.PropLoader;

public class ChatClient implements MessageTypes {
    
    Sender sender;
    boolean joined;
    
    public ChatClient() {
        joined = false;
        sender = new Sender(this);
        sender.start();
    }
        
    public boolean hasJoined() {
        return joined;
    }
    
    public void joinedChat() {
        joined = true;
    }
    
    public void leftChat() {
        joined = false;
    }
    
    public void shutdown() {
        System.out.println("Shutting down client application");
        sender.interrupt();
        System.exit(0);
    }
    
    public static void main(String[] args) {
        new ChatClient();
    }
}
