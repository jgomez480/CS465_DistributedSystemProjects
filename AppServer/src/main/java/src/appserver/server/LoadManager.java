package appserver.server;

import java.util.ArrayList;

public class LoadManager {
    static ArrayList satellites;
    static int lastSatelliteIndex = -1;
    
    public LoadManager() 
    {
        satellites = new ArrayList<String>();
    }
    
    public void satelliteAdded(String satelliteName) 
    { 
        // Adds satellite name to array list
        satellites.add(satelliteName);
        System.out.println("[LoadManager.satelliteAdded] " + satelliteName + " added");
    }
    
    
    public String nextSatellite() throws Exception
    {
        String satelliteName;
        synchronized(satellites) 
        {
            int numSatellites = satellites.size();
            
            // Uses Round Robin policy
            // Increments to next index
            int currentSatelliteInd = lastSatelliteIndex + 1;
            // If index goes over max number of satellites, will fall back to 0
            if (currentSatelliteInd >= numSatellites)
            {
                currentSatelliteInd = 0;
            }
            
            // Returns the satellite's string name
            satelliteName = (String) satellites.get(currentSatelliteInd);
            lastSatelliteIndex = currentSatelliteInd;
        }
        
        return satelliteName;
    }
}
