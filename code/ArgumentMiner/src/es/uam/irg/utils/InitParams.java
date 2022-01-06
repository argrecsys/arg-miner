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
package es.uam.irg.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Usuario
 */
public class InitParams {

    private final static String FILE_PATH = "Resources/config/params.json";

    public static Map<String, Object> readInitParams() {
        Map<String, Object> params = new HashMap<>();
        String jsonText = readJsonFile();

        if (!"".equals(jsonText)) {
            JSONObject json = new JSONObject(jsonText);

            if (!json.isEmpty()) {
                // General parameters
                List<Object> ids = ((JSONArray) json.get("customProposalID")).toList();
                Integer[] customProposals = new Integer[ids.size()];
                for (int i = 0; i < ids.size(); i++) {
                    customProposals[i] = Integer.parseInt(ids.get(i).toString());
                }
                params.put("customProposals", customProposals);
                params.put("language", json.getString("language"));

                // Extraction process parameters
                Map<String, Object> extraction = new HashMap<>();
                extraction.put("annotateComments", true);
                params.put("extraction", extraction);

                // Recommendation process parameters
                Map<String, Object> recommendation = new HashMap<>();
                recommendation.put("maxTreeLevel", 2);
                recommendation.put("minAspectOccur", 1);
                recommendation.put("topic", "-");
                params.put("recommendation", recommendation);

                // Linkers parameters
                Map<String, Object> linkers = new HashMap<>();
                Map<String, Object> en = new HashMap<>();
                en.put("invalidAspects", new String[]{"also", "thing", "mine", "sometimes", "too", "other"});
                en.put("invalidLinkers", new String[]{"and", "or"});
                en.put("validLinkers", new String[]{});
                linkers.put("en", en);
                Map<String, Object> es = new HashMap<>();
                es.put("invalidAspects", new String[]{"tambien", "cosa", "mia", "veces", "ademas", "demas"});
                es.put("invalidLinkers", new String[]{"o", "y"});
                es.put("validLinkers", new String[]{});
                linkers.put("en", es);
                params.put("linkers", linkers);
            }
        }

        return params;
    }

    private static String readJsonFile() {
        StringBuilder jsonText = new StringBuilder();

        File jsonFile = new File(FILE_PATH);
        if (jsonFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
                String row;
                while ((row = reader.readLine()) != null) {
                    jsonText.append(row + "\n");
                }
                reader.close();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(InitParams.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(InitParams.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jsonText.toString();
    }

}
