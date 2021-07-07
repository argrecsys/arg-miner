/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am;

import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.*;
import es.uam.irg.io.IOManager;
import es.uam.irg.nlp.am.arguments.*;
import es.uam.irg.utils.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ansegura
 */
public class ArgumentMiner implements Constants {
    
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        System.out.println(">> PROGRAM BEGINS");
        
        // Read input parameters
        String language = LANG_ES;
        int maxProposal = 100;
        
        if (args.length > 0) {
            language = args[0];
            
            if (args.length > 1) {
                maxProposal = Integer.parseInt(args[1]);
            }
        }
        System.out.format(">> Language selected: %s, max number of proposals to be analyzed: %s\n", language, maxProposal);

        // Get the list of proposals
        Map<Integer, DMProposal> proposals = getArgumentativeProposals(maxProposal);
        
        // Create linkers manager
        ArgumentLinkerManager lnkManager = createLinkerManager(language, true);
        
        if (!proposals.isEmpty() && !lnkManager.isEmpty()) {
            
            // Bulk annotation of proposals
            Map<Integer, List<Argument>> arguments = bulkAnnotation(language, proposals, lnkManager);

            // Show results
            System.out.println(">> Total proposals: " + proposals.size());
            System.out.println(">> Total proposals with arguments: " + arguments.size());
            proposals.keySet().forEach(key -> {
                System.out.format("   Proposal %s has %s arguments\n", key, arguments.get(key).size());
            });

            // Save arguments
            boolean result = saveArguments(arguments, proposals);
            if (result) {
                System.out.println(">> Arguments saved correctly.");
            }
            else {
                System.err.println(">> An unexpected error occurred while saving the arguments.");
            }
        }
        else {
            if (proposals.isEmpty()) {
                System.err.println(">> Error: There are no argumentative proposals available.");
            }
            if (lnkManager.isEmpty()) {
                System.err.println(">> Error: There are no argumentative linkers available.");
            }
        }
        
        System.out.println(">> PROGRAM ENDS");
    }
    
    /**
     * 
     * @param language
     * @param proposals
     * @param lnkManager
     * @return 
     */
    private static Map<Integer, List<Argument>> bulkAnnotation(String language, Map<Integer, DMProposal> proposals, ArgumentLinkerManager lnkManager) {
        Map<Integer, List<Argument>> arguments = new HashMap<>();
        
        // Temporary vars
        ArgumentEngine engine = new ArgumentEngine(language, lnkManager);
        int proposalID;
        DMProposal proposal;
        
        // Analize argumentative proposals
        for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
            proposalID = entry.getKey();
            proposal = entry.getValue();
            
            List<Argument> argList = engine.annotate(proposalID, proposal.getTitle(), proposal.getSummary());
            arguments.put(proposalID, argList);
        }
        
        return arguments;
    }
    
    /**
     * Create the linker manager object.
     * 
     * @param lang
     * @param verbose
     * @return
     */
    private static ArgumentLinkerManager createLinkerManager(String lang, boolean verbose) {
        return IOManager.readLinkerTaxonomy(lang, verbose);
    }
    
    /**
     * Wrapper function for (DMDBManager) selectNProposals method.
     *
     * @param topN
     * @param linkers
     * @return 
     */
    private static Map<Integer, DMProposal> getArgumentativeProposals(int topN) {
        Map<Integer, DMProposal> proposals = null;
        try {
            DMDBManager dbManager = new DMDBManager();
            proposals = dbManager.selectCustomProposals(topN);
        }
        catch (Exception ex) {
            Logger.getLogger(ArgumentMiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return proposals;
    }
    
    /**
     * Saves the arguments in a plain text file.
     * 
     * @param arguments
     * @param proposals
     * @return 
     */
    private static boolean saveArguments(Map<Integer, List<Argument>> arguments, Map<Integer, DMProposal> proposals) {
        boolean result = false;
        
        if (arguments != null) {
            JSONArray argList = new JSONArray();
            
            for (Map.Entry<Integer, List<Argument>> entry : arguments.entrySet()) {
                DMProposal prop = proposals.get(entry.getKey());
                
                for (Argument arg : entry.getValue()) {
                    
                    // Create JSON object
                    JSONObject item = new JSONObject();
                    item.put("proposalID", entry.getKey());
                    item.put("sentenceID", arg.sentenceID);
                    item.put("majorClaim", prop.getTitle());
                    item.put("sentence", arg.sentenceText);
                    item.put("claim", arg.claim);
                    item.put("premise", arg.premise);
                    item.put("mainVerb", arg.mainVerb);
                    item.put("relationType", arg.linker.relationType);
                    item.put("linker", arg.linker.linker);
                    item.put("linkerCategory", arg.linker.category);
                    item.put("linkerSubCategory", arg.linker.subCategory);
                    item.put("entityList", arg.getEntityList().toString());
                    item.put("nounList", arg.getNounList().toString());
                    item.put("approach", arg.approach);
                    
                    // Store JSON object
                    argList.add(item);
                }
            }
            
            // Save JSON file
            String jsonString = StringUtils.prettyJSON(argList.toJSONString());
            result = IOManager.saveJsonFile(jsonString, Constants.OUTPUT_FILEPATH);
        }
        
        return result;
    }
    
}
