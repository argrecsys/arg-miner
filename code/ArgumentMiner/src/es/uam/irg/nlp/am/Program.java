/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am;

import java.util.Arrays;

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
        boolean annotateComments = true;
        Integer[] customProposalID = new Integer[0];

        // Read input parameters
        System.out.println(">> N params: " + args.length);
        for (int i = 0; i < args.length; i++) {
            switch (i) {
                case 0 ->
                    language = args[i].toLowerCase();
                case 1 ->
                    annotateComments = Boolean.parseBoolean(args[i]);
                case 2 -> {
                    String[] ids = args[i].split(",");
                    customProposalID = new Integer[ids.length];
                    for (int j = 0; j < ids.length; j++) {
                        customProposalID[j] = Integer.parseInt(ids[j]);
                    }
                }
            }
        }
        System.out.format("   Selected language: %s, annotate comments? %s, customized proposals: %s\n", language, annotateComments, Arrays.toString(customProposalID));

        // Run program
        ArgumentMiner miner = new ArgumentMiner(language, annotateComments, customProposalID);
        boolean result = miner.runProgram();

        if (result) {
            System.out.println(">> The Argument Miner engine was executed correctly.");
        } else {
            System.err.println(">> The Argument Miner engine had an unexpected error.");
        }

        System.out.println(">> ARGMINER ENDS");
    }

}
