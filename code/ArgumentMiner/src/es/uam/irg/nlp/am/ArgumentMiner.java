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
import es.uam.irg.utils.FunctionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private boolean annotateComments;
    private String language;
    private ArgumentLinkerManager lnkManager;
    private Map<String, Object> mdbSetup;
    private Map<String, Object> msqlSetup;
    private Map<Integer, DMProposal> proposals;
    private HashSet<String> stopwords;
    private boolean verbose = true;
    
    /**
     * Class constructor.
     * 
     * @param language 
     * @param annotateComments 
     * @param customProposalID 
     */
    public ArgumentMiner(String language, boolean annotateComments, Integer[] customProposalID) {
        this.language = language;
        this.annotateComments = annotateComments;
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(Constants.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(Constants.MYSQL_DB);
        this.lnkManager = createLinkerManager(language);
        this.proposals = getArgumentativeProposals(customProposalID);
        this.stopwords = getStopwordList(language);
    }
    
    /**
     * 
     * @return 
     */
    public boolean runProgram() {
        boolean result = false;
        
        if (!proposals.isEmpty() && !lnkManager.isEmpty()) {
            
            // Bulk annotation of proposals
            Map<Integer, List<Argument>> arguments = bulkAnnotation(language, proposals, lnkManager, stopwords);
            
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
    private Map<Integer, List<Argument>> bulkAnnotation(String language, Map<Integer, DMProposal> proposals, ArgumentLinkerManager lnkManager, HashSet<String> stopwords) {
        Map<Integer, List<Argument>> arguments = new HashMap<>();
        
        // Temporary vars
        ArgumentEngine engine = new ArgumentEngine(language, lnkManager, stopwords);
        int proposalID;
        DMProposal proposal;
        
        // Analize argumentative proposals
        for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
            proposalID = entry.getKey();
            proposal = entry.getValue();
            
            List<Argument> argList = engine.extract(proposalID, proposal.getTitle(), proposal.getSummary());
            arguments.put(proposalID, argList);
        }
        
        return arguments;
    }
    
    /**
     * Create the linker manager object.
     * 
     * @param lang
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
    private Map<Integer, DMProposal> getArgumentativeProposals(Integer[] customProposalID) {
        Map<Integer, DMProposal> proposals = null;
        
        try {
            DMDBManager dbManager = null;
            if (msqlSetup != null && msqlSetup.size() == 4) {
                String dbServer = msqlSetup.get("db_server").toString();
                String dbName = msqlSetup.get("db_name").toString();
                String dbUserName = msqlSetup.get("db_user_name").toString();
                String dbUserPwd = msqlSetup.get("db_user_pw").toString();
                
                dbManager = new DMDBManager(dbServer, dbName, dbUserName, dbUserPwd);
            }
            else {
                dbManager = new DMDBManager();
            }
            
            if (customProposalID.length > 0) {
                proposals = dbManager.selectProposals(customProposalID);
            }
            else {
                proposals = dbManager.selectProposals(this.lnkManager.getLexicon(false));
            }
            
            if (this.verbose) {
                System.out.println(">> Number of proposals: " + proposals.size());
            }
        }
        catch (Exception ex) {
            Logger.getLogger(ArgumentMiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return proposals;
    }
    
    /**
     * 
     * @param language
     * @return 
     */
    private HashSet<String> getStopwordList(String lang) {
        return IOManager.readStopwordList(lang, this.verbose);
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
            arguments.entrySet().forEach(entry -> {
                entry.getValue().forEach(arg -> {
                    argList.put(arg.sentenceID, arg.getJSON());
                });
            });
            
            // Save JSON files
            result = IOManager.saveStringToJson(argList.toString(4), Constants.ARGUMENTS_FILEPATH);
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
            arguments.entrySet().forEach(entry -> {
                for (Argument arg : entry.getValue()) {
                    argList.add(arg.getDocument());
                    argFilter.add(Filters.eq("argumentID", arg.sentenceID));
                }
            });
            
            // Upsert documents
            if (argList.size() > 0) {
                MongoDbManager dbManager = null;
                if (mdbSetup != null && mdbSetup.size() == 3) {
                    String dbServer = mdbSetup.get("db_server").toString();
                    int dbPort = Integer.parseInt(mdbSetup.get("db_port").toString());
                    String dbName = mdbSetup.get("db_name").toString();

                    dbManager = new MongoDbManager(dbServer, dbPort, dbName);
                }
                else {
                    dbManager = new MongoDbManager();
                }
                
                result = dbManager.upsertDocuments("annotations", argList, argFilter, new UpdateOptions().upsert(true));
            }
        }
        
        return result;
    }
    
}
