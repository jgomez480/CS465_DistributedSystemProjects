package appserver.server;

import appserver.comm.ConnectivityInfo;
import appserver.satellite.Satellite;
import java.util.HashMap;

public class SatelliteManager {
    HashMap<String, ConnectivityInfo> map = null;
    
    public SatelliteManager() 
    {
        map = new HashMap<String, ConnectivityInfo>();
    }
    
    // Put satellite connectivity info in hashmap
    public void registerSatellite(ConnectivityInfo satelliteInfo)
    {
        map.put(satelliteInfo.getName(), satelliteInfo);
    }
    
    // Get the satellite's connectivity info from hashmap mapping
    public ConnectivityInfo getSatelliteFromName(String name)
    {
        return map.get(name);
    }
}
