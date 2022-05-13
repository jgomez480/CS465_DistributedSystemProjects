package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropLoader {
	
	String filename = null;
	
	public PropLoader(String filename) {
		this.filename = filename;
	}
	
	public Properties getProperty() {
    	Properties prop = new Properties();
    	InputStream propInputStream = null;
    	
    	try {
    		propInputStream = new FileInputStream(filename);
    		prop.load(propInputStream);
    	}catch (IOException ex) {
    		ex.printStackTrace();
    	}finally {
    		if(propInputStream != null) {
    			try {
    				propInputStream.close();
    			}catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	return prop;
	}
}
