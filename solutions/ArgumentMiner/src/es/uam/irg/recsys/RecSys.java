/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.recsys;

/**
 *
 * @author ansegura
 */
public class RecSys {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println(">> RECSYS BEGINS");
        
        // Program hyperparameters with default values
        String topic = "transporte";
        
        // Read input parameters
        if (args.length > 0) {
            topic = args[0].toLowerCase();
        }
        System.out.format(">> Topic selected: %s\n", topic);
        
        // Run program
        ArguRecSys recSys = new ArguRecSys(topic);
        boolean result = recSys.runRecSys();
        
        if (result) {
            System.out.println(">> The Argument-based RecSys was executed correctly.");
        }
        else {
            System.err.println(">> The Argument-based RecSys had an unexpected error.");
        }
        
        System.out.println(">> RECSYS ENDS");
    }
    
}
