package es.uam.irg.decidemadrid.controversy;

import es.uam.irg.decidemadrid.controversy.metrics.ControversyDiscussionTextLength;
import es.uam.irg.decidemadrid.controversy.metrics.ControversyStructureHIndex;
import es.uam.irg.decidemadrid.controversy.metrics.ControversyVotesProduct;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMCategory;
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMTopic;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControversyAnalyzer {

    private DMDBManager db;

    public ControversyAnalyzer(DMDBManager db) {
        this.db = db;
    }

    public void testCorrelations() throws Exception {
        List<String> metricNames = new ArrayList<>();
        List<Map<Integer, ControversyScore>> scoresLists = new ArrayList<>();
        metricNames.add(ControversyVotesProduct.NAME);
        scoresLists.add(new ControversyVotesProduct(db).getScores());
        metricNames.add(ControversyDiscussionTextLength.NAME);
        scoresLists.add(new ControversyDiscussionTextLength(db).getScores());
        metricNames.add(ControversyStructureHIndex.NAME);
        scoresLists.add(new ControversyStructureHIndex(db).getScores());
//        metricNames.add(ControversyNumberComments.NAME);
//        scoresLists.add(new ControversyNumberComments().getScores());
//        metricNames.add(ControversyStructureDepth.NAME);
//        scoresLists.add(new ControversyStructureDepth().getScores());
        ControversyScoreCombinator controversy = new ControversyScoreCombinator(db, scoresLists, metricNames);

        // Correlation metric matrix
        controversy.correlationCoefficient();

        // Controversy vs. number of supports
        Map<Integer, DMProposal> proposals = db.selectProposals();

        Map<Integer, ControversyScore> controversyScores = controversy.getScores();
        for (int id : proposals.keySet()) {
            DMProposal proposal = proposals.get(id);

            if (!controversyScores.containsKey(id)) {
                continue;
            }

            int numSupports = proposal.getNumSupports();
            double score = controversyScores.get(id).getValue();
            System.out.println(numSupports + "\t" + score);
        }
    }

    public void testProposalVisualizations() throws Exception {
//        Controversy controversy = new ControversyStructureHIndex();

        List<String> metricNames = new ArrayList<>();
        List<Map<Integer, ControversyScore>> scoresLists = new ArrayList<>();
        metricNames.add(ControversyVotesProduct.NAME);
        scoresLists.add(new ControversyVotesProduct(db).getScores());
        metricNames.add(ControversyDiscussionTextLength.NAME);
        scoresLists.add(new ControversyDiscussionTextLength(db).getScores());
        metricNames.add(ControversyStructureHIndex.NAME);
        scoresLists.add(new ControversyStructureHIndex(db).getScores());
        ControversyScoreCombinator controversy = new ControversyScoreCombinator(db, scoresLists, metricNames);

        Map<Integer, DMProposal> proposals = db.selectProposals();
        Map<Integer, List<String>> districts = db.selectProposalDistricts();
        Map<Integer, List<DMCategory>> categories = db.selectProposalCategories();
        Map<Integer, List<DMTopic>> topics = db.selectProposalTopics();

        Map<Integer, ControversyScore> controversyScores = controversy.getScores();
        List<ControversyScore> scores = new ArrayList<>(controversyScores.values());
        Collections.sort(scores, Comparator.comparing(ControversyScore::getValue));

        System.out.println("Id\tYear\t#supports\t#comments\tControversy\tTitle\tDistricts\tCategories\tTopics");

        for (ControversyScore score : scores) {
            int id = score.getId();
            double controversyValue = score.getValue();

            String title = proposals.get(id).getTitle();
            List<String> d = districts.containsKey(id) ? districts.get(id) : new ArrayList<>();
            String _districts = d.toString().replace("[", "").replace("]", "");

            List<DMCategory> c = categories.containsKey(id) ? categories.get(id) : new ArrayList<>();

            boolean hasTitleSource = false;
            for (DMCategory category : c) {
                String source = category.getSource();
                if (source.contains("title")) {
                    hasTitleSource = true;
                }
            }

            Map<String, Double> finalCategories = new HashMap<>();
            double totalWeight = 0.0;
            for (DMCategory category : c) {
                String name = category.getName();
                double weight = category.getNormalizedWeight();
                String source = category.getSource();

                if (hasTitleSource) {
                    if (source.contains("title")) {
                        if (!finalCategories.containsKey(name)) {
                            finalCategories.put(name, 0.0);
                        }
                        finalCategories.put(name, weight + finalCategories.get(name));
                        totalWeight += weight;
                    }
                } else {
                    if (!finalCategories.containsKey(name)) {
                        finalCategories.put(name, 0.0);
                    }
                    finalCategories.put(name, weight + finalCategories.get(name));
                    totalWeight += weight;
                }
            }
            List<String> names = new ArrayList<>(finalCategories.keySet());
            for (String name : names) {
                finalCategories.put(name, finalCategories.get(name) / totalWeight);
            }
            String _categories = "";
            for (String category : finalCategories.keySet()) {
                _categories += category + ",";
            }
            if (_categories.length() > 0) {
                _categories = _categories.substring(0, _categories.length() - 1);
            }

            List<DMTopic> t = topics.containsKey(id) ? topics.get(id) : new ArrayList<>();
            hasTitleSource = false;
            for (DMTopic topic : t) {
                String source = topic.getSource();
                if (source.contains("title")) {
                    hasTitleSource = true;
                }
            }

            Map<String, Double> finalTopics = new HashMap<>();

            totalWeight = 0.0;
            for (DMTopic topic : t) {
                String name = topic.getName();
                double weight = topic.getNormalizedWeight();
                String source = topic.getSource();

                if (hasTitleSource) {
                    if (source.contains("title")) {
                        if (!finalTopics.containsKey(name)) {
                            finalTopics.put(name, 0.0);
                        }
                        finalTopics.put(name, weight + finalTopics.get(name));
                        totalWeight += weight;
                    }
                } else {
                    if (!finalTopics.containsKey(name)) {
                        finalTopics.put(name, 0.0);
                    }
                    finalTopics.put(name, weight + finalTopics.get(name));
                    totalWeight += weight;
                }
            }
            names = new ArrayList<>(finalTopics.keySet());
            for (String name : names) {
                finalTopics.put(name, finalTopics.get(name) / totalWeight);
            }
            String _topics = "";
            for (String category : finalTopics.keySet()) {
                _topics += category + ",";
            }
            if (_topics.length() > 0) {
                _topics = _topics.substring(0, _topics.length() - 1);
            }

            int numSupports = proposals.get(id).getNumSupports();
            int numComments = proposals.get(id).getNumComments();

            Calendar calendar = Calendar.getInstance();
            calendar.set(proposals.get(id).getYear(), proposals.get(id).getMonth(), proposals.get(id).getDay());
            int year = calendar.get(Calendar.YEAR);

            System.out.println(id + "\t" + year + "\t" + numSupports + "\t" + numComments + "\t" + controversyValue + "\t" + title + "\t" + _districts + "\t" + _categories + "\t" + _topics);
        }
    }

    public Map<String, List<Double>> testCategories() throws Exception {
        Map<String, List<Double>> categoryMetrics = new HashMap<>();

        List<String> metricNames = new ArrayList<>();
        List<Map<Integer, ControversyScore>> scoresLists = new ArrayList<>();
        metricNames.add(ControversyVotesProduct.NAME);
        scoresLists.add(new ControversyVotesProduct(db).getScores());
        metricNames.add(ControversyDiscussionTextLength.NAME);
        scoresLists.add(new ControversyDiscussionTextLength(db).getScores());
        metricNames.add(ControversyStructureHIndex.NAME);
        scoresLists.add(new ControversyStructureHIndex(db).getScores());
        ControversyScoreCombinator controversy = new ControversyScoreCombinator(db, scoresLists, metricNames);

        Map<Integer, DMProposal> proposals = db.selectProposals();
        //Map<Integer, List<String>> proposalDistricts = db.selectProposalDistricts();
        Map<Integer, List<DMCategory>> proposalCategories = db.selectProposalCategories();
        //Map<Integer, List<DMTopic>> proposalTopics = db.selectProposalTopics();

        Map<Integer, ControversyScore> controversyScores = controversy.getScores();

        for (int id : proposals.keySet()) {
            DMProposal proposal = proposals.get(id);

            if (!controversyScores.containsKey(id)) {
                continue;
            }

            if (!proposalCategories.containsKey(id)) {
                continue;
            }

            int numSupports = proposal.getNumSupports();
            double score = controversyScores.get(id).getValue();

            List<DMCategory> categories = proposalCategories.get(id);
            boolean hasTitleSource = false;
            for (DMCategory category : categories) {
                String source = category.getSource();
                if (source.contains("title")) {
                    hasTitleSource = true;
                }
            }

            Map<String, Double> finalCategories = new HashMap<>();
            double totalWeight = 0.0;
            for (DMCategory category : categories) {
                String name = category.getName();
                double weight = category.getNormalizedWeight();
                String source = category.getSource();

                // Selecting final categories
                if (hasTitleSource) {
                    if (source.contains("title")) {
                        if (!finalCategories.containsKey(name)) {
                            finalCategories.put(name, 0.0);
                        }
                        finalCategories.put(name, weight + finalCategories.get(name));
                        totalWeight += weight;
                    }
                } else {
                    if (!finalCategories.containsKey(name)) {
                        finalCategories.put(name, 0.0);
                    }
                    finalCategories.put(name, weight + finalCategories.get(name));
                    totalWeight += weight;
                }
            }
            List<String> names = new ArrayList<>(finalCategories.keySet());
            for (String name : names) {
                //finalCategories.put(name, finalCategories.get(name) / totalWeight);
                finalCategories.put(name, 1.0);
            }

            // Printing information
            for (String name : finalCategories.keySet()) {
                double weight = finalCategories.get(name);
                //System.out.println(name + "\t" + weight + "\t" + numSupports + "\t" + score);

                if (!categoryMetrics.containsKey(name)) {
                    categoryMetrics.put(name, new ArrayList<>());
                    categoryMetrics.get(name).add(0.0);
                    categoryMetrics.get(name).add(0.0);
                    categoryMetrics.get(name).add(0.0);
                }
                categoryMetrics.get(name).set(0, weight + categoryMetrics.get(name).get(0));
                categoryMetrics.get(name).set(1, numSupports + categoryMetrics.get(name).get(1));
                categoryMetrics.get(name).set(2, score + categoryMetrics.get(name).get(2));
            }
        }

        List<String> _categories = new ArrayList<>(categoryMetrics.keySet());
        for (String category : _categories) {
            categoryMetrics.get(category).set(1, categoryMetrics.get(category).get(1) / categoryMetrics.get(category).get(0));
            categoryMetrics.get(category).set(2, categoryMetrics.get(category).get(2) / categoryMetrics.get(category).get(0));
        }

        return categoryMetrics;
    }

    public Map<String, List<Double>> testTopics() throws Exception {
        Map<String, List<Double>> topicMetrics = new HashMap<>();

        List<String> metricNames = new ArrayList<>();
        List<Map<Integer, ControversyScore>> scoresLists = new ArrayList<>();
        metricNames.add(ControversyVotesProduct.NAME);
        scoresLists.add(new ControversyVotesProduct(db).getScores());
        metricNames.add(ControversyDiscussionTextLength.NAME);
        scoresLists.add(new ControversyDiscussionTextLength(db).getScores());
        metricNames.add(ControversyStructureHIndex.NAME);
        scoresLists.add(new ControversyStructureHIndex(db).getScores());
        ControversyScoreCombinator controversy = new ControversyScoreCombinator(db, scoresLists, metricNames);

        Map<Integer, DMProposal> proposals = db.selectProposals();
        //Map<Integer, List<String>> proposalDistricts = db.selectProposalDistricts();
        //Map<Integer, List<DMCategory>> proposalCategories = db.selectProposalCategories();
        Map<Integer, List<DMTopic>> proposalTopics = db.selectProposalTopics();

        Map<Integer, ControversyScore> controversyScores = controversy.getScores();
        for (int id : proposals.keySet()) {
            DMProposal proposal = proposals.get(id);

            if (!controversyScores.containsKey(id)) {
                continue;
            }

            if (!proposalTopics.containsKey(id)) {
                continue;
            }

            int numSupports = proposal.getNumSupports();
            double score = controversyScores.get(id).getValue();

            List<DMTopic> topics = proposalTopics.get(id);
            boolean hasTitleSource = false;
            for (DMTopic topic : topics) {
                String source = topic.getSource();
                if (source.contains("title")) {
                    hasTitleSource = true;
                }
            }

            Map<String, Double> finalTopics = new HashMap<>();
            double totalWeight = 0.0;
            for (DMTopic topic : topics) {
                String name = topic.getName();
                double weight = topic.getNormalizedWeight();
                String source = topic.getSource();

                // Selecting final topics
                if (hasTitleSource) {
                    if (source.contains("title")) {
                        if (!finalTopics.containsKey(name)) {
                            finalTopics.put(name, 0.0);
                        }
                        finalTopics.put(name, weight + finalTopics.get(name));
                        totalWeight += weight;
                    }
                } else {
                    if (!finalTopics.containsKey(name)) {
                        finalTopics.put(name, 0.0);
                    }
                    finalTopics.put(name, weight + finalTopics.get(name));
                    totalWeight += weight;
                }
            }
            List<String> names = new ArrayList<>(finalTopics.keySet());
            for (String name : names) {
                finalTopics.put(name, finalTopics.get(name) / totalWeight);
            }

            // Printing information
            for (String name : finalTopics.keySet()) {
                double weight = finalTopics.get(name);
                //System.out.println(name + "\t" + weight + "\t" + numSupports + "\t" + score);

                if (!topicMetrics.containsKey(name)) {
                    topicMetrics.put(name, new ArrayList<>());
                    topicMetrics.get(name).add(0.0);
                    topicMetrics.get(name).add(0.0);
                    topicMetrics.get(name).add(0.0);
                }
                topicMetrics.get(name).set(0, weight + topicMetrics.get(name).get(0));
                topicMetrics.get(name).set(1, numSupports + topicMetrics.get(name).get(1));
                topicMetrics.get(name).set(2, score + topicMetrics.get(name).get(2));
            }
        }

        List<String> _topics = new ArrayList<>(topicMetrics.keySet());
        for (String topic : _topics) {
            topicMetrics.get(topic).set(1, topicMetrics.get(topic).get(1) / topicMetrics.get(topic).get(0));
            topicMetrics.get(topic).set(2, topicMetrics.get(topic).get(2) / topicMetrics.get(topic).get(0));
        }

        return topicMetrics;
    }

    public Map<String, List<Double>> testDistricts() throws Exception {
        Map<String, List<Double>> districtMetrics = new HashMap<>();

        List<String> metricNames = new ArrayList<>();
        List<Map<Integer, ControversyScore>> scoresLists = new ArrayList<>();
        metricNames.add(ControversyVotesProduct.NAME);
        scoresLists.add(new ControversyVotesProduct(db).getScores());
        metricNames.add(ControversyDiscussionTextLength.NAME);
        scoresLists.add(new ControversyDiscussionTextLength(db).getScores());
        metricNames.add(ControversyStructureHIndex.NAME);
        scoresLists.add(new ControversyStructureHIndex(db).getScores());
        ControversyScoreCombinator controversy = new ControversyScoreCombinator(db, scoresLists, metricNames);

        Map<Integer, DMProposal> proposals = db.selectProposals();
        Map<Integer, List<String>> proposalDistricts = db.selectProposalDistricts();

        Map<Integer, ControversyScore> controversyScores = controversy.getScores();
        for (int id : proposals.keySet()) {
            DMProposal proposal = proposals.get(id);

            if (!controversyScores.containsKey(id)) {
                continue;
            }

            if (!proposalDistricts.containsKey(id)) {
                continue;
            }

            int numSupports = proposal.getNumSupports();
            double score = controversyScores.get(id).getValue();

            List<String> districts = proposalDistricts.get(id);
            districts.remove("Ciudad");
            if (districts.isEmpty()) {
                continue;
            }

            for (String district : districts) {
                double weight = 1.0;
                //System.out.println(district + "\t" + weight + "\t" + numSupports + "\t" + score);

                if (!districtMetrics.containsKey(district)) {
                    districtMetrics.put(district, new ArrayList<>());
                    districtMetrics.get(district).add(0.0);
                    districtMetrics.get(district).add(0.0);
                    districtMetrics.get(district).add(0.0);
                }
                districtMetrics.get(district).set(0, weight + districtMetrics.get(district).get(0));
                districtMetrics.get(district).set(1, numSupports + districtMetrics.get(district).get(1));
                districtMetrics.get(district).set(2, score + districtMetrics.get(district).get(2));
            }
        }

        List<String> _districts = new ArrayList<>(districtMetrics.keySet());
        for (String district : _districts) {
            districtMetrics.get(district).set(1, districtMetrics.get(district).get(1) / districtMetrics.get(district).get(0));
            districtMetrics.get(district).set(2, districtMetrics.get(district).get(2) / districtMetrics.get(district).get(0));
        }

        return districtMetrics;
    }

    public static void main(String[] args) {
        try {
            DMDBManager db = new DMDBManager();

            ControversyAnalyzer analyzer = new ControversyAnalyzer(db);

            // =================================================================
//            analyzer.testProposalVisualizations();
            // =================================================================
//            analyzer.testCorrelations();
            // =================================================================
            Map<String, List<Double>> categoryMetrics = analyzer.testCategories();
            for (String category : categoryMetrics.keySet()) {
                int numProposals = categoryMetrics.get(category).get(0).intValue();
                double avgNumSupports = categoryMetrics.get(category).get(1);
                double avgControversy = categoryMetrics.get(category).get(2);
                System.out.println(category + "\t" + numProposals + "\t" + avgNumSupports + "\t" + avgControversy);
            }

            System.out.println("********************************");

            Map<String, List<Double>> topicMetrics = analyzer.testTopics();
            for (String topic : topicMetrics.keySet()) {
                int numProposals = topicMetrics.get(topic).get(0).intValue();
                double avgNumSupports = topicMetrics.get(topic).get(1);
                double avgControversy = topicMetrics.get(topic).get(2);
                System.out.println(topic + "\t" + numProposals + "\t" + avgNumSupports + "\t" + avgControversy);
            }

            System.out.println("********************************");

            Map<String, List<Double>> districtMetrics = analyzer.testDistricts();
            for (String topic : districtMetrics.keySet()) {
                int numProposals = districtMetrics.get(topic).get(0).intValue();
                double avgNumSupports = districtMetrics.get(topic).get(1);
                double avgControversy = districtMetrics.get(topic).get(2);
                System.out.println(topic + "\t" + numProposals + "\t" + avgNumSupports + "\t" + avgControversy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
