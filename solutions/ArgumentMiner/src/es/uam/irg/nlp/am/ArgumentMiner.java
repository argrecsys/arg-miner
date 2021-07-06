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
        int maxProposal = 5;
        
        if (args.length > 0) {
            language = args[0];
            
            if (args.length > 1) {
                maxProposal = Integer.parseInt(args[1]);
            }
        }
        System.out.format(">> Language selected: %s, number of proposals to be analyzed: %s\n", language, maxProposal);
        
        // Get the list of argument linkers
        ArgumentLinkerList linkers = getLinkerTaxonomy(language, false);

        if (linkers != null && linkers.size() > 0) {
            
            // Get the list of argumentative proposals
            Map<Integer, DMProposal> proposals = getArgumentativeProposals(maxProposal, linkers);
            
            // Bulk annotation of proposals
            if (proposals != null && proposals.size() > 0) {
                Map<Integer, List<Argument>> arguments = bulkAnnotation(language, proposals, linkers);
                
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
                System.err.println(">> Error: There are no proposals using the indicated linkers.");
            }
        }
        else {
            System.err.println(">> Error: The linker taxonomy could not be loaded.");
        }
        
        System.out.println(">> PROGRAM ENDS");
    }
    
    /**
     * 
     * @param language
     * @param proposals
     * @param linkers
     * @return 
     */
    private static Map<Integer, List<Argument>> bulkAnnotation(String language, Map<Integer, DMProposal> proposals, ArgumentLinkerList linkers) {
        Map<Integer, List<Argument>> arguments = new HashMap<>();
        
        // Temporary vars
        ArgumentEngine engine = new ArgumentEngine(language);
        ArgumentLinker linker = linkers.getLinker("porque");
        int proposalID;
        DMProposal proposal;
        
        // Analize argumentative proposals
        for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
            proposalID = entry.getKey();
            proposal = entry.getValue(); 
            List<Argument> argList = engine.annotate(proposalID, proposal.getTitle(), proposal.getSummary(), linker);
            arguments.put(proposalID, argList);
        }
        
        return arguments;
    }
    
    /**
     * Wrapper function for (DMDBManager) selectNProposals method.
     * 
     * @param topN
     * @param linkers
     * @return 
     */
    private static Map<Integer, DMProposal> getArgumentativeProposals(int topN, ArgumentLinkerList linkers) {
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
     * Wrapper function for (IOManager) readLinkerTaxonomy method.
     * 
     * @param lang
     * @param verbose
     * @return
     */
    private static ArgumentLinkerList getLinkerTaxonomy(String lang, boolean verbose) {
        return IOManager.readLinkerTaxonomy(lang, verbose);
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
                    item.put("sentence", arg.sentence);
                    item.put("claim", arg.claim);
                    item.put("premise", arg.premise);
                    item.put("relationType", arg.relationType);
                    item.put("approach", arg.approach);
                    item.put("mainVerb", arg.mainVerb);
                    item.put("entityList", arg.getEntityList().toString());
                    
                    // Store JSON object
                    argList.add(item);
                }
            }
            
            // Save JSON file
            String jsonString = argList.toJSONString();
            jsonString = jsonString.replace("},", "},\n ");
            jsonString = jsonString.replace("\":", "\": ");
            jsonString = jsonString.replace("\",\"", "\", \"");
            result = IOManager.saveJsonFile(jsonString, Constants.OUTPUT_FILEPATH);
        }
        
        return result;
    }
    
}
