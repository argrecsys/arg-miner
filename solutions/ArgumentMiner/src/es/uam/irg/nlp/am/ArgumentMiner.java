/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.db.MongoDbManager;
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.io.IOManager;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.nlp.am.arguments.ArgumentEngine;
import es.uam.irg.nlp.am.arguments.ArgumentLinkerManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

/**
 *
 * @author ansegura
 */
public class ArgumentMiner {
    
    // Class members
    private Map<String, Object> dbSetup;
    private String language;
    private ArgumentLinkerManager lnkManager;
    private Map<Integer, DMProposal> proposals;
    private boolean verbose = true;
    
    /**
     * Class constructor.
     * 
     * @param language
     * @param maxProposals 
     */
    public ArgumentMiner(String language, int maxProposals) {
        this.language = language;
        this.dbSetup = getDatabaseConfiguration();
        this.lnkManager = createLinkerManager(language);
        this.proposals = getArgumentativeProposals(maxProposals);
    }
    
    /**
     * 
     * @return 
     */
    public boolean runProgram() {
        boolean result = false;
        
        if (!proposals.isEmpty() && !lnkManager.isEmpty()) {
            
            // Bulk annotation of proposals
            Map<Integer, List<Argument>> arguments = bulkAnnotation(language, proposals, lnkManager);
            
            // Show results
            System.out.println(">> Total proposals: " + proposals.size());
            System.out.println(">> Total arguments in the proposals:");
            proposals.keySet().forEach(key -> {
                System.out.format("   Proposal %s has %s arguments\n", key, arguments.get(key).size());
            });
            
            // Save arguments
            result = storeArguments(arguments);
            if (result) {
                saveArguments(arguments);
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
        
        return result;
    }
    
    /**
     * 
     * @param language
     * @param proposals
     * @param lnkManager
     * @return 
     */
    private Map<Integer, List<Argument>> bulkAnnotation(String language, Map<Integer, DMProposal> proposals, ArgumentLinkerManager lnkManager) {
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
    private ArgumentLinkerManager createLinkerManager(String lang) {
        return IOManager.readLinkerTaxonomy(lang, this.verbose);
    }
    
    /**
     * Wrapper function for (DMDBManager) selectNProposals method.
     * 
     * @param topN
     * @return 
     */
    private Map<Integer, DMProposal> getArgumentativeProposals(int topN) {
        Map<Integer, DMProposal> proposals = null;
        
        try {
            DMDBManager dbManager = null;
            if (dbSetup != null && dbSetup.size() == 4) {
                String dbServer = dbSetup.get("db_server").toString();
                String dbName = dbSetup.get("db_name").toString();
                String dbUserName = dbSetup.get("db_user_name").toString();
                String dbUserPwd = dbSetup.get("db_user_pw").toString();
                
                dbManager = new DMDBManager(dbServer, dbName, dbUserName, dbUserPwd);
            }
            else {
                dbManager = new DMDBManager();
            }
            
            //proposals = dbManager.selectCustomProposals(topN);
            proposals = dbManager.selectProposals(topN, this.lnkManager);
        }
        catch (Exception ex) {
            Logger.getLogger(ArgumentMiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (this.verbose) {
            System.out.println(">> Number of proposals: " + proposals.size());
        }
        
        return proposals;
    }
    
    /**
     * 
     * @return 
     */
    private Map<String, Object> getDatabaseConfiguration() {
        Map<String, Object> setup = IOManager.readYamlFile(Constants.DB_SETUP_FILEPATH);
        return setup;
    }
        
    /**
     * Saves the arguments in a plain text file.
     * 
     * @param arguments
     * @return 
     */
    private boolean saveArguments(Map<Integer, List<Argument>> arguments) {
        boolean result = false;
        
        if (arguments != null) {
            JSONObject argList = new JSONObject();
            
            // Store JSON objects
            for (Map.Entry<Integer, List<Argument>> entry : arguments.entrySet()) {
                for (Argument arg : entry.getValue()) {
                    argList.put(arg.sentenceID, arg.getJSON());
                }
            }
            
            // Save JSON files
            result = IOManager.saveStringToJson(argList.toString(4), Constants.OUTPUT_FILEPATH);
        }
        
        return result;
    }
    
    /**
     * 
     * @param arguments
     * @param proposals
     * @return 
     */
    private boolean storeArguments(Map<Integer, List<Argument>> arguments) {
        boolean result = false;
        
        if (arguments != null) {
            List<Document> argList = new ArrayList<>();
            List<Bson> argFilter = new ArrayList<>();

            // Store Document objects
            for (Map.Entry<Integer, List<Argument>> entry : arguments.entrySet()) {
                for (Argument arg : entry.getValue()) {
                    argList.add(arg.getDocument());
                    argFilter.add(Filters.eq("argumentID", arg.sentenceID));
                }
            }
            
            // Upsert documents
            if (argList.size() > 0) {
                MongoDbManager manager = new MongoDbManager();
                result = manager.upsertDocuments("annotations", argList, argFilter, new UpdateOptions().upsert(true));
            }
        }
        
        return result;
    }
    
}
