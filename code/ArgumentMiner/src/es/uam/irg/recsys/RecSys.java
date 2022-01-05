/**
 * Copyright 2021
 * Andrés Segura-Tinoco
 * Information Retrieval Group at Universidad Autonoma de Madrid
 *
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the current software. If not, see <http://www.gnu.org/licenses/>.
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
