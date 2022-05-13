package transaction_server;

import java.io.Serializable;

// Now uses an object array to be able to pass both account number and balance in same message object
public class Message implements Serializable {

    int type;
    Object[] content;

    public Message(int type, Object content)
    {
        this.type = type;
        this.content = new Object[1];
        this.content[0] = content;
    }
    
    public Message(int type, Object[] content)
    {
        this.type = type;
        this.content = content;
    }

    public int getType()
    {
        return type;
    }

    public Object getContent()
    {
        return content[0];
    }
    
    public Object[] getContents()
    {
        return content;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public void setContent(Object[] content)
    {
        this.content = content;
    }
}
