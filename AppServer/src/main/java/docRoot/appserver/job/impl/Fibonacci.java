package docRoot.appserver.job.impl;

import appserver.job.Tool;

public class Fibonacci implements Tool {

    Integer number = null;
    
    public Object go(Object parameters) 
    {
        number = (Integer) parameters;
        return getResult();
    }

    public Integer getResult() 
    {
        // Iterative fibonacci algorithm
        int result = 1;
        int firstPreviousNum = 1;
        int secondPreviousNum = 1;
        int index = 2; // Start computing at 2
        while (index < number) 
        {
            result = firstPreviousNum + secondPreviousNum;
            secondPreviousNum = firstPreviousNum;
            firstPreviousNum = result;
            
            index++;
        }
        return result;
    }
    
    public static void main(String[] args) 
    {
        System.out.println("OPENED");
    }  
}
