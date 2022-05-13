package message;

import java.io.Serializable;

public class Message implements Serializable, MessageTypes
{

    int type;
    Object content;
    boolean propagate;

    public Message(int type, Object content)
    {
        this.type = type;
        this.content = content;
        this.propagate = false;
    }

    public Object getContent()
    {
        return content;
    }

    public boolean getPropagate()
    {
        return propagate;
    }

    public int getType()
    {
        return type;
    }

    public void setContent(Object content)
    {
        this.content = content;
    }

    public void setPropagate(boolean propagate)
    {
        this.propagate = propagate;
    }

    public void setType(int type)
    {
        this.type = type;
    }

}
