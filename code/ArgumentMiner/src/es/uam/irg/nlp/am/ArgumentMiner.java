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
import es.uam.irg.nlp.am.arguments.ArgumentLinker;
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
 * Argument miner class.
 */
public class ArgumentMiner {

    // Class constants
    private static final String ARGUMENTS_FILEPATH = "../../results/arguments.json";
    private static final boolean VERBOSE = true;

    // Class members
    private final HashSet<String> invalidLinkers;
    private final String language;
    private final ArgumentLinkerManager lnkManager;
    private final Map<String, Object> mdbSetup;
    private final Map<String, Object> msqlSetup;
    private Map<Integer, DMComment> proposalComments;
    private Map<Integer, DMProposal> proposals;
    private final HashSet<String> stopwords;

    /**
     * Class constructor.
     *
     * @param language
     * @param customProposalIds
     * @param annotateComments
     * @param validLinkers
     * @param invalidLinkers
     */
    public ArgumentMiner(String language, Integer[] customProposalIds, boolean annotateComments, HashSet<String> validLinkers, HashSet<String> invalidLinkers) {
        this.language = language;
        this.invalidLinkers = invalidLinkers;
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MYSQL_DB);
        this.lnkManager = createLinkerManager(language, validLinkers, invalidLinkers);
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
            Map<Integer, List<Argument>> arguments = bulkAnnotation(language, proposals, proposalComments, lnkManager, invalidLinkers, stopwords);

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
     * Automatically annotates (identifies and extracts arguments) from
     * proposals and comments.
     *
     * @param language
     * @param proposals
     * @param proposalComments
     * @param lnkManager
     * @param invalidLinkers
     * @param stopwords
     * @return
     */
    private Map<Integer, List<Argument>> bulkAnnotation(String language, Map<Integer, DMProposal> proposals, Map<Integer, DMComment> proposalComments, ArgumentLinkerManager lnkManager, HashSet<String> invalidLinkers, HashSet<String> stopwords) {
        Map<Integer, List<Argument>> arguments = new HashMap<>();

        // Temporary vars
        List<ArgumentLinker> lexicon = lnkManager.getLexicon(false);
        ArgumentEngine engine = new ArgumentEngine(language, lexicon, invalidLinkers, stopwords);
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
     * @param validLinkers
     * @param invalidLinkers
     * @return
     */
    private ArgumentLinkerManager createLinkerManager(String lang, HashSet<String> validLinkers, HashSet<String> invalidLinkers) {
        return IOManager.readLinkerTaxonomy(lang, validLinkers, invalidLinkers, VERBOSE);
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
            // Connecting to databse
            DMDBManager dbManager = null;
            if (msqlSetup != null && msqlSetup.size() == 4) {
                dbManager = new DMDBManager(msqlSetup);
            } else {
                dbManager = new DMDBManager();
            }

            // Get lexicon
            List<ArgumentLinker> lexicon = this.lnkManager.getLexicon(false);

            // Get proposals with linkers
            if (customProposalIds.length > 0) {
                proposals = dbManager.selectProposals(customProposalIds);
            } else {
                proposals = dbManager.selectProposals(lexicon);
                //proposals = dbManager.selectProposals2();
            }

            // Get proposal comments with linkers
            if (annotateComments) {
                if (customProposalIds.length > 0) {
                    proposalComments = dbManager.selectComments(customProposalIds);
                } else {
                    proposalComments = dbManager.selectComments(lexicon);
                    //proposalComments = dbManager.selectComments2();
                }
            }

            // Show results
            if (VERBOSE) {
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
        return IOManager.readStopwordList(lang, VERBOSE);
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
            result = IOManager.saveStringToJson(argList.toString(4), ARGUMENTS_FILEPATH);
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
                    dbManager = new MongoDbManager(mdbSetup);
                } else {
                    dbManager = new MongoDbManager();
                }

                result = dbManager.upsertDocuments(argList, argFilter, new UpdateOptions().upsert(true));
            }
        }

        return result;
    }

}
