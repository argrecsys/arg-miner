/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.recsys;

import java.util.Arrays;

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
        String language = Constants.LANG_ES;
        int minAspectOccur = 1;
        String topic = Constants.NO_TOPIC;
        Integer[] customProposalID = new Integer[0];

        // Read input parameters
        System.out.println(">> N params: " + args.length);
        for (int i = 0; i < args.length; i++) {
            switch (i) {
                case 0 ->
                    minAspectOccur = Integer.parseInt(args[i]);
                case 1 -> {
                    if (!args[i].equals(Constants.NO_TOPIC)) {
                        topic = args[i].toLowerCase();
                    }
                }
                case 2 -> {
                    String[] ids = args[i].split(",");
                    customProposalID = new Integer[ids.length];
                    for (int j = 0; j < ids.length; j++) {
                        customProposalID[j] = Integer.parseInt(ids[j]);
                    }
                }
            }
        }
        System.out.format("   Minimum occurrences per aspect: %s, topic selected: %s, customized proposals: %s\n", minAspectOccur, topic, Arrays.toString(customProposalID));

        // Run program
        ArguRecSys recSys = new ArguRecSys(language, minAspectOccur, topic, customProposalID);
        boolean result = recSys.runRecSys();

        if (result) {
            System.out.println(">> The Argument-based RecSys was executed correctly.");
        } else {
            System.err.println(">> The Argument-based RecSys had an unexpected error.");
        }

        System.out.println(">> RECSYS ENDS");
    }

}
