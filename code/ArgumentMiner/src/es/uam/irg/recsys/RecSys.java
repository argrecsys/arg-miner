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

import es.uam.irg.utils.InitParams;
import java.util.Arrays;
import java.util.Map;

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

        // Program hyperparameters from JSON config file
        Map<String, Object> params = InitParams.readInitParams();
        String language = (String) params.get("language");
        Integer[] customProposalIds = (Integer[]) params.get("customProposals");
        Map<String, Object> recommendation = (Map<String, Object>) params.get("recommendation");
        int maxTreeLevel = (int) recommendation.get("maxTreeLevel");
        int minAspectOccur = (int) recommendation.get("minAspectOccur");
        String topic = (String) recommendation.get("topic");
        System.out.format(">> Analysis language: %s, Maximum level of the syntactic tree: %s, Minimum occurrences per aspect: %s, Selected topic: %s, Ids of customized proposals: %s\n",
                language, maxTreeLevel, minAspectOccur, topic, Arrays.toString(customProposalIds));

        // Run program
        ArguRecSys recSys = new ArguRecSys(language, maxTreeLevel, minAspectOccur, topic, customProposalIds);
        boolean result = recSys.runRecSys();

        if (result) {
            System.out.println(">> The Argument-based RecSys was executed correctly.");
        } else {
            System.err.println(">> The Argument-based RecSys had an unexpected error.");
        }

        System.out.println(">> RECSYS ENDS");
    }

}
