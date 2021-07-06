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
        
        // Get linkers in argumentative proposals
        Map<Integer, ArgumentLinker> argLinkers = getLinkersInProposals(language, proposals);
        
        if (argLinkers.size() > 0) {
            
            // Bulk annotation of proposals
            Map<Integer, List<Argument>> arguments = bulkAnnotation(language, proposals, argLinkers);

            // Show results
            System.out.println(">> Total proposals: " + argLinkers.size());
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
            System.err.println(">> Error: No argumentative proposals (with linkers) were found.");
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
    private static Map<Integer, List<Argument>> bulkAnnotation(String language, Map<Integer, DMProposal> proposals, Map<Integer, ArgumentLinker> argLinkers) {
        Map<Integer, List<Argument>> arguments = new HashMap<>();
        
        // Temporary vars
        ArgumentEngine engine = new ArgumentEngine(language);
        int proposalID;
        ArgumentLinker linker;
        DMProposal proposal;
        
        // Analize argumentative proposals
        for (Map.Entry<Integer, ArgumentLinker> entry : argLinkers.entrySet()) {
            proposalID = entry.getKey();
            linker = entry.getValue();
            proposal = proposals.get(proposalID);
            
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
     * 
     * @param language
     * @param proposals
     * @return 
     */
    private static Map<Integer, ArgumentLinker> getLinkersInProposals(String language, Map<Integer, DMProposal> proposals) {
        Map<Integer, ArgumentLinker> propLinkers = new HashMap<>();
        
        // Get the list of argument linkers
        ArgumentLinkerList linkers = getLinkerTaxonomy(language, true);
        
        if (linkers.size() > 0) {
            proposals.keySet().forEach(key -> {
                DMProposal proposal = proposals.get(key);
                ArgumentLinker linker = null;
                boolean flag = false;
                
                for (int i = 0; i < linkers.size() && !flag; i++) {
                    linker = linkers.getLinker(i);
                    if (proposal.getSummary().contains(linker.linker)) {
                        flag = true;
                    }
                }
                
                if (flag && linker != null) {
                    propLinkers.put(key, linker);
                }
                else {
                    System.err.println("Proposal " + key + " has no linkers.");
                }
                
            });
        }
        
        return propLinkers;
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
