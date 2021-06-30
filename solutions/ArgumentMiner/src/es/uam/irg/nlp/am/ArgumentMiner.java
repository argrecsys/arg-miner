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
import es.uam.irg.utils.Constants;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ansegura
 */
public class ArgumentMiner implements Constants {
    
    public static ArgumentEngine engine;
    public static DMDBManager dbManager;
    
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String language;
        if (args.length > 0) {
            language = args[0];
        }
        else {
            language = LANG_ES;
        }
        
        engine = new ArgumentEngine(language);
        dbManager = new DMDBManager();

        // Get the list of argument linkers
        ArgumentLinkerList linkers = readLinkerTaxonomy(language, true);

        if (linkers != null) {
            ArgumentLinker linker = linkers.getLinker("porque");
            
            // Get the list of argumentative proposals
            int maxProposal = 1;
            Map<Integer, DMProposal> proposals = getArgumentativeProposals(maxProposal, linkers);

            // Analize argumentative proposals
            if (proposals != null) {
                System.out.println(">> List of custom argumentative proposals:");
                int proposalID;
                DMProposal proposal;

                for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
                    proposalID = entry.getKey();
                    proposal = entry.getValue(); 
                    engine.annotate(proposalID, proposal.getSummary(), linker);
                }
            }
        }
        
    }
    
    /**
     * Wrapper function for (IOManager) readLinkerTaxonomy
     * @param lang
     * @param verbose
     * @return 
     */
    private static ArgumentLinkerList readLinkerTaxonomy(String lang, boolean verbose) {
        ArgumentLinkerList linkers = IOManager.readLinkerTaxonomy(lang, verbose);
        return linkers;
    }
    
    /**
     * Wrapper function for (DMDBManager) selectNProposals
     * @param topN
     * @param linkers
     * @return 
     */
    private static Map<Integer, DMProposal> getArgumentativeProposals(int topN, ArgumentLinkerList linkers) {
        Map<Integer, DMProposal> proposals = null;
        try {
            proposals = dbManager.selectCustomProposals(topN, linkers);
        } catch (Exception ex) {
            Logger.getLogger(ArgumentMiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return proposals;
    }
    
}
