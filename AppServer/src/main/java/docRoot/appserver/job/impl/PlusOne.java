package docRoot.appserver.job.impl;

import appserver.client.PlusOneClient;
import appserver.job.Tool;

/**
 * Class [PlusOne] Simple POC class that implements the Tool interface
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class PlusOne implements Tool{

    PlusOneAux helper = null;
    
    @Override
    public Object go(Object parameters) {
        
        helper = new PlusOneAux((Integer) parameters);
        return helper.getResult();
    }
    
    public static void main(String[] args) {

        System.out.println("OPENED");
    }  
}
