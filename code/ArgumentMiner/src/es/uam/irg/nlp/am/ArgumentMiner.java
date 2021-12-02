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
import es.uam.irg.decidemadrid.entities.DMComment;
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
    private String language;
    private ArgumentLinkerManager lnkManager;
    private Map<String, Object> mdbSetup;
    private Map<String, Object> msqlSetup;
    private Map<Integer, DMComment> proposalComments;
    private Map<Integer, DMProposal> proposals;
    private HashSet<String> stopwords;
    private boolean verbose = true;

    /**
     * Class constructor.
     *
     * @param language
     * @param annotateComments
     * @param customProposalIds
     */
    public ArgumentMiner(String language, boolean annotateComments, Integer[] customProposalIds) {
        this.language = language;
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(Constants.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(Constants.MYSQL_DB);
        this.lnkManager = createLinkerManager(language);
        this.stopwords = getStopwordList(language);

        // Read argumentative proposals and their comments
        getArgumentativeProposals(customProposalIds, annotateComments);
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
            } else {
                System.err.println(">> An unexpected error occurred while saving the arguments.");
            }
        } else {
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
        int commentID;
        DMProposal proposal;
        DMComment comment;

        // 1. Analize argumentative proposals
        System.out.println("Proposals annotation");
        for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
            proposalID = entry.getKey();
            proposal = entry.getValue();

            List<Argument> argList = engine.extract(proposalID, proposal.getUserId(), -1, -1, proposal.getTitle(), proposal.getSummary());
            arguments.put(proposalID, argList);
        }

        // 2. Analize argumentative comments
        System.out.println("Comments annotation");
        for (Map.Entry<Integer, DMComment> entry : proposalComments.entrySet()) {
            commentID = entry.getKey();
            comment = entry.getValue();
            proposalID = comment.getProposalId();

            List<Argument> argList = engine.extract(proposalID, comment.getUserId(), commentID, comment.getParentId(), "", comment.getText());
            if (arguments.containsKey(proposalID)) {
                if (argList.size() > 0) {
                    arguments.get(proposalID).addAll(argList);
                }
            } else {
                System.err.println(">> Error: Comments should always be associated with a proposal.");
            }
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
     * Read argumentative proposals and their comments.
     *
     * @param topN
     * @return
     */
    private void getArgumentativeProposals(Integer[] customProposalIds, boolean annotateComments) {
        proposals = new HashMap<>();
        proposalComments = new HashMap<>();

        try {
            DMDBManager dbManager = null;
            if (msqlSetup != null && msqlSetup.size() == 4) {
                String dbServer = msqlSetup.get("db_server").toString();
                String dbName = msqlSetup.get("db_name").toString();
                String dbUserName = msqlSetup.get("db_user_name").toString();
                String dbUserPwd = msqlSetup.get("db_user_pw").toString();

                dbManager = new DMDBManager(dbServer, dbName, dbUserName, dbUserPwd);
            } else {
                dbManager = new DMDBManager();
            }

            if (customProposalIds.length > 0) {
                proposals = dbManager.selectProposals(customProposalIds);
            } else {
                proposals = dbManager.selectProposals(this.lnkManager.getLexicon(false));

                if (annotateComments) {
                    customProposalIds = new Integer[proposals.size()];

                    int i = 0;
                    for (Integer key : proposals.keySet()) {
                        customProposalIds[i++] = key;
                    }
                }
            }

            if (annotateComments) {
                proposalComments = dbManager.selectComments(customProposalIds);
            }

            if (this.verbose) {
                System.out.println(">> Number of proposals: " + proposals.size());
                System.out.println("   Number of comments: " + proposalComments.size());
            }
        } catch (Exception ex) {
            Logger.getLogger(ArgumentMiner.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                    argList.put(arg.getId(), arg.getJSON(false));
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
                    argFilter.add(Filters.eq("argumentID", arg.getId()));
                }
            });

            // Upsert documents
            if (argList.size() > 0) {
                MongoDbManager dbManager = null;
                if (mdbSetup != null && mdbSetup.size() == 4) {
                    String dbServer = mdbSetup.get("db_server").toString();
                    int dbPort = Integer.parseInt(mdbSetup.get("db_port").toString());
                    String dbName = mdbSetup.get("db_name").toString();
                    String collName = mdbSetup.get("db_collection").toString();

                    dbManager = new MongoDbManager(dbServer, dbPort, dbName, collName);
                } else {
                    dbManager = new MongoDbManager();
                }

                result = dbManager.upsertDocuments(argList, argFilter, new UpdateOptions().upsert(true));
            }
        }

        return result;
    }

}
