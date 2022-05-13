package chat;

import java.io.Serializable;

// Class that hold tuple of client connection data
public class NodeInfo implements Serializable
{

    String ip;
    int portNum;
    String logicalName;

    public NodeInfo(String ip, int portNum, String logicalName)
    {
        this.ip = ip;
        this.portNum = portNum;
        this.logicalName = logicalName;
    }

    public boolean equals(NodeInfo otherNode)
    {
        return (this.ip.equals(otherNode.ip) && this.portNum == otherNode.portNum);
    }

    public String getIP()
    {
        return ip;
    }

    public int getPort()
    {
        return portNum;
    }

    public String getName()
    {
        return logicalName;
    }

    public void setIP(String ip)
    {
        this.ip = ip;
    }

    public void setPort(int portNum)
    {
        this.portNum = portNum;
    }

    public void setName(String logicalName)
    {
        this.logicalName = logicalName;
    }
}
