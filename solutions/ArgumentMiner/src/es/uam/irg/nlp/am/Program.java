/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am;

/**
 *
 * @author ansegura
 */
public class Program {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println(">> ARGMINER BEGINS");
        
        // Program hyperparameters with default values
        String language = Constants.LANG_ES;
        int maxProposal = 100;
        
        // Read input parameters
        if (args.length > 0) {
            language = args[0].toLowerCase();
            
            if (args.length > 1) {
                maxProposal = Integer.parseInt(args[1]);
            }
        }
        System.out.format(">> Language selected: %s, max number of proposals to be analyzed: %s\n", language, maxProposal);
        
        // Run program
        ArgumentMiner miner = new ArgumentMiner(language, maxProposal);
        boolean result = miner.runProgram();
        
        if (result) {
            System.out.println(">> The Argument Miner engine was executed correctly.");
        }
        else {
            System.err.println(">> The Argument Miner engine had an unexpected error.");
        }
        
        System.out.println(">> ARGMINER ENDS");
    }
    
}
