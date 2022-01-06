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
package es.uam.irg.recsys;

import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.db.MongoDbManager;
import es.uam.irg.decidemadrid.entities.DMProposalSummary;
import es.uam.irg.io.IOManager;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.utils.FunctionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import org.bson.Document;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 *
 * @author ansegura
 */
public class ArguRecSys {

    // Class constants
    public static final String NO_TOPIC = "-";
    private static final HashSet<String> INVALID_ASPECTS = new HashSet(Arrays.asList("tambien", "cosa", "mia", "veces", "ademas", "demas"));
    private static final String LANG_EN = "en";
    private static final String LANG_ES = "es";
    private static final String RECOMMENDATIONS_FILEPATH = "../../results/recommendations_{}.xml";

    // Class members
    private Integer[] customProposalIds;
    private String language;
    private int maxTreeLevel;
    private Map<String, Object> mdbSetup;
    private int minAspectOccur;
    private Map<String, Object> msqlSetup;
    private String topic;

    /**
     * Class constructor.
     *
     * @param language
     * @param maxTreeLevel
     * @param minAspectOccur
     * @param topic
     * @param customProposalIds
     */
    public ArguRecSys(String language, int maxTreeLevel, int minAspectOccur, String topic, Integer[] customProposalIds) {
        this.language = language;
        this.maxTreeLevel = maxTreeLevel;
        this.minAspectOccur = minAspectOccur;
        this.topic = topic;
        this.customProposalIds = customProposalIds;
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MYSQL_DB);
    }

    /**
     *
     * @return
     */
    public boolean runRecSys() {
        boolean result = false;

        // Get list of arguments by specific topic
        List<Argument> arguments = getArgumentsByFilter(this.topic, this.customProposalIds);
        System.out.println(">> Total arguments: " + arguments.size());

        // Get proposals summary for selected arguments
        Map<Integer, DMProposalSummary> proposals = getProposalsSummary(arguments);
        System.out.println(">> Total proposals: " + proposals.size());

        if (!arguments.isEmpty() && !proposals.isEmpty()) {
            Map<String, Integer> aspects = getFreqAspects(arguments);
            System.out.println(">> Aspects:");
            System.out.println(aspects);

            // Save arguments
            Map<String, List<Argument>> recommendations = getRecommendations(arguments, aspects, this.minAspectOccur);
            System.out.println(">> Total recommended topics: " + recommendations.size());

            result = saveRecommendations(this.topic, proposals, recommendations);
            if (result) {
                System.out.println(">> Recommendations saved correctly.");
            } else {
                System.err.println(">> An unexpected error occurred while saving the recommendations.");
            }
        }

        return result;
    }

    /**
     * Creates and argument element and its properties.
     *
     * @param doc
     * @param argument
     * @return
     */
    private Element createRecommendationElement(org.w3c.dom.Document doc, Argument argument) {
        Element nArgu = doc.createElement("argument");

        // Argument element and its properties
        Attr attr = doc.createAttribute("id");
        attr.setValue(argument.getId());
        nArgu.setAttributeNode(attr);
        attr = doc.createAttribute("userid");
        attr.setValue("" + argument.userID);
        nArgu.setAttributeNode(attr);
        attr = doc.createAttribute("parentid");
        attr.setValue("" + argument.parentID);
        nArgu.setAttributeNode(attr);
        attr = doc.createAttribute("commentid");
        attr.setValue("" + argument.commentID);
        nArgu.setAttributeNode(attr);

        Element nClaim = doc.createElement("claim");
        nClaim.appendChild(doc.createTextNode(argument.claim.text));
        nArgu.appendChild(nClaim);

        Element nLinker = doc.createElement("connector");
        nLinker.appendChild(doc.createTextNode(argument.linker.linker));
        nArgu.appendChild(nLinker);

        attr = doc.createAttribute("category");
        attr.setValue(argument.linker.category.toLowerCase());
        nLinker.setAttributeNode(attr);

        attr = doc.createAttribute("subcategory");
        attr.setValue(argument.linker.subCategory.toLowerCase());
        nLinker.setAttributeNode(attr);

        attr = doc.createAttribute("function");
        attr.setValue(argument.linker.relationType);
        nLinker.setAttributeNode(attr);

        Element nPremise = doc.createElement("premise");
        nPremise.appendChild(doc.createTextNode(argument.premise.text));
        nArgu.appendChild(nPremise);

        return nArgu;
    }

    /**
     *
     * @param topic
     * @param customProposalIds
     * @return
     */
    private List<Argument> getArgumentsByFilter(String topic, Integer[] customProposalIds) {
        List<Argument> arguments = new ArrayList<>();

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

        List<Document> docs = dbManager.getDocumentsByFilter(topic, customProposalIds);

        docs.forEach(doc -> {
            Argument arg = new Argument(doc);
            if (arg.getTreeLevel() <= this.maxTreeLevel) {
                arguments.add(arg);
            }
        });

        return arguments;
    }

    /**
     *
     * @param arguments
     * @return
     */
    private Map<String, Integer> getFreqAspects(List<Argument> arguments) {
        Map<String, Integer> aspects = new HashMap<>();
        List<String> listAspects = new ArrayList<>();
        Set<String> nouns;
        int count;

        for (Argument argument : arguments) {
            nouns = argument.getNounsSet();
            listAspects.addAll(nouns);
            System.out.println(argument.getId() + ": " + nouns.toString());
        }

        System.out.println("Invalid aspects:");
        for (String aspect : listAspects) {
            if (aspect.length() > 2 && !INVALID_ASPECTS.contains(aspect)) {
                count = aspects.getOrDefault(aspect, 0);
                aspects.put(aspect, count + 1);
            } else {
                System.out.println("- " + aspect);
            }
        }

        return FunctionUtils.sortMapByValue(aspects);
    }

    /**
     *
     * @param arguments
     * @return
     */
    private Map<Integer, DMProposalSummary> getProposalsSummary(List<Argument> arguments) {
        Map<Integer, DMProposalSummary> proposals = null;

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

            proposals = dbManager.selectProposalsSummary(arguments);

        } catch (Exception ex) {
            Logger.getLogger(ArguRecSys.class.getName()).log(Level.SEVERE, null, ex);
        }

        return proposals;
    }

    /**
     *
     * @param arguments
     * @param aspects
     * @param minAspectOccur
     * @return
     */
    private Map<String, List<Argument>> getRecommendations(List<Argument> arguments, Map<String, Integer> aspects, int minAspectOccur) {
        Map<String, List<Argument>> result = new HashMap<>();

        // Local variables
        Map<String, List<Argument>> recommendations = new HashMap<>();
        List<String> aspectList = new ArrayList(aspects.keySet());

        // Select arguments
        arguments.forEach(argument -> {
            String aspect = "";
            Set<String> nouns = argument.getNounsSet();

            for (int i = 0; i < aspectList.size() && "".equals(aspect); i++) {
                if (nouns.contains(aspectList.get(i))) {
                    aspect = aspectList.get(i);
                }
            }

            if ("".equals(aspect)) {
                aspect = (language.equals(LANG_ES) ? "otros" : "others");
            }
            List<Argument> arguList = recommendations.getOrDefault(aspect, new ArrayList<>());
            arguList.add(argument);
            recommendations.put(aspect, arguList);
        });

        // Filtering arguments
        int total = 0;
        for (Map.Entry<String, List<Argument>> recommendation : recommendations.entrySet()) {
            int totalAspect = recommendation.getValue().size();
            if (totalAspect >= minAspectOccur) {
                result.put(recommendation.getKey(), recommendation.getValue());
                System.out.println(recommendation.getKey() + ", " + totalAspect);
                total += totalAspect;
            }
        }

        System.out.println("Total arguments extracted: " + total);

        return result;
    }

    /**
     *
     * @param topic
     * @param proposals
     * @param recommendations
     * @return
     */
    private boolean saveRecommendations(String topic, Map<Integer, DMProposalSummary> proposals, Map<String, List<Argument>> recommendations) {
        boolean result = false;
        String filename = RECOMMENDATIONS_FILEPATH.replace("_{}", (topic.equals(NO_TOPIC) ? "" : "_" + topic));
        Attr attr;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.newDocument();

            // Root element
            Element rootElement = doc.createElement("recommendations");
            doc.appendChild(rootElement);

            // Proposals element
            Element nProposals = doc.createElement("proposals");
            rootElement.appendChild(nProposals);
            attr = doc.createAttribute("quantity");
            attr.setValue("" + proposals.size());
            nProposals.setAttributeNode(attr);

            for (Map.Entry<Integer, DMProposalSummary> entry : proposals.entrySet()) {
                DMProposalSummary proposal = entry.getValue();

                // Proposal element
                Element nProposal = doc.createElement("proposal");
                nProposal.appendChild(doc.createTextNode(proposal.getTitle()));
                nProposals.appendChild(nProposal);

                attr = doc.createAttribute("id");
                attr.setValue("" + proposal.getId());
                nProposal.setAttributeNode(attr);

                attr = doc.createAttribute("date");
                attr.setValue(proposal.getDate());
                nProposal.setAttributeNode(attr);

                attr = doc.createAttribute("categories");
                attr.setValue(proposal.getCategories());
                nProposal.setAttributeNode(attr);

                attr = doc.createAttribute("districts");
                attr.setValue(proposal.getDistricts());
                nProposal.setAttributeNode(attr);

                attr = doc.createAttribute("topics");
                attr.setValue(proposal.getTopics());
                nProposal.setAttributeNode(attr);
            }

            // Topics element
            Element nTopics = doc.createElement("topics");
            rootElement.appendChild(nTopics);
            attr = doc.createAttribute("quantity");
            attr.setValue("1");
            nTopics.setAttributeNode(attr);

            // Topic element
            Element nTopic = doc.createElement("topic");
            nTopics.appendChild(nTopic);
            attr = doc.createAttribute("value");
            attr.setValue(topic);
            nTopic.setAttributeNode(attr);
            attr = doc.createAttribute("quantity");
            attr.setValue("" + recommendations.size());
            nTopic.setAttributeNode(attr);

            for (Map.Entry<String, List<Argument>> entry : recommendations.entrySet()) {

                // Topic element
                Element nAspect = doc.createElement("aspect");
                nTopic.appendChild(nAspect);

                attr = doc.createAttribute("value");
                attr.setValue(entry.getKey());
                nAspect.setAttributeNode(attr);

                attr = doc.createAttribute("quantity");
                attr.setValue("" + entry.getValue().size());
                nAspect.setAttributeNode(attr);

                for (Argument argument : entry.getValue()) {
                    if (argument.commentID >= -1) {
                        Element nArgu = createRecommendationElement(doc, argument);
                        nAspect.appendChild(nArgu);
                    }
                }
            }

            // Write the content into xml file
            DOMSource source = new DOMSource(doc);
            result = IOManager.saveDomToXML(source, filename);

        } catch (ParserConfigurationException | DOMException ex) {
            System.err.println(ex.getMessage());
        }

        return result;
    }

}
