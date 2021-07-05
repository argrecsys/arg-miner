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
                saveArguments(arguments);
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
            List<Argument> argList = engine.annotate(proposalID, proposal.getSummary(), linker);
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
            proposals = dbManager.selectCustomProposals(topN, linkers);
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
     * @param proposals
     * @param arguments 
     */
    private static void saveArguments(Map<Integer, List<Argument>> arguments) {
        
        if (arguments != null) {
            
            for (Map.Entry<Integer, List<Argument>> entry : arguments.entrySet()) {
                
            }
        }
    }
    
}
