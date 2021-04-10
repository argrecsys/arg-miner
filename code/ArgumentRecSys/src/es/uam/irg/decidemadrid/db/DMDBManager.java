package es.uam.irg.decidemadrid.db;

import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMTopic;
import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.decidemadrid.entities.DMCategory;
import es.uam.irg.db.MySQLDBConnector;
import es.uam.irg.decidemadrid.entities.DMCommentTree;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMDBManager {

    public static final String DB_SERVER = "localhost";
    public static final String DB_NAME = "decide.madrid_2019_09";
    public static final String DB_USERNAME = "root";
    public static final String DB_USERPASSWORD = "Ovs001993";

    public MySQLDBConnector db;

    public DMDBManager() throws Exception {
        this(DB_SERVER, DB_NAME, DB_USERNAME, DB_USERPASSWORD);
    }

    public DMDBManager(String dbServer, String dbName, String dbUserName, String dbUserPassword) throws Exception {
        this.db = new MySQLDBConnector();
        this.db.connect(dbServer, dbName, dbUserName, dbUserPassword);
    }

    @Override
    public void finalize() {
        this.db.disconnect();
    }

    public Map<Integer, String> selectDistricts() throws Exception {
        String query = "SELECT * FROM geo_districts";

        Map<Integer, String> districts = new HashMap<>();

        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            districts.put(id, name);
        }
        rs.close();

        return districts;
    }

    public Map<Integer, Map<Integer, String>> selectNeighborhoods() throws Exception {
        String query = "SELECT * FROM geo_neighborhoods";

        Map<Integer, Map<Integer, String>> neighborhoods = new HashMap<>();

        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int districtId = rs.getInt("districtId");

            if (!neighborhoods.containsKey(districtId)) {
                neighborhoods.put(districtId, new HashMap<>());
            }
            neighborhoods.get(districtId).put(id, name);
        }
        rs.close();

        return neighborhoods;
    }

    public Map<Integer, DMProposal> selectProposals() throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();

        String query = "SELECT * FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            int userId = rs.getInt("userId");
            String date = rs.getString("date");
            String summary = rs.getString("summary");
            String text = rs.getString("text");
            int numComments = rs.getInt("numComments");
            int numSupports = rs.getInt("numSupports");

            DMProposal proposal = new DMProposal(id, title, userId, date, summary, text, numComments, numSupports);

            proposals.put(id, proposal);
        }
        rs.close();

        return proposals;
    }

    public Map<Integer, DMComment> selectComments() throws Exception {
        Map<Integer, DMComment> comments = new HashMap<>();

        String query = "SELECT * FROM proposal_comments";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            int parentId = rs.getInt("parentId");
            int proposalId = rs.getInt("proposalId");
            int userId = rs.getInt("userId");
            String date = rs.getDate("date").toString();
            String time = rs.getTime("time").toString();
            String text = rs.getString("text");
            int votes = rs.getInt("numVotes");
            int votesUp = rs.getInt("numPositiveVotes");
            int votesDown = rs.getInt("numNegativeVotes");

            DMComment comment = new DMComment(id, parentId, proposalId, userId, date, time, text, votes, votesUp, votesDown);
            comments.put(id, comment);
        }
        rs.close();

        return comments;
    }

    public Map<Integer, String> selectProposalTitles() throws Exception {
        Map<Integer, String> titles = new HashMap<>();

        String query = "SELECT * FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            titles.put(id, title);
        }
        rs.close();

        return titles;
    }

    public Map<Integer, String> selectProposalSummaries() throws Exception {
        Map<Integer, String> summaries = new HashMap<>();

        String query = "SELECT * FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String summary = rs.getString("summary");
            summaries.put(id, summary);
        }
        rs.close();

        return summaries;
    }

    public Map<Integer, String> selectProposalTexts() throws Exception {
        Map<Integer, String> texts = new HashMap<>();

        String query = "SELECT * FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String text = rs.getString("text");
            texts.put(id, text);
        }
        rs.close();

        return texts;
    }

    public Map<Integer, List<DMComment>> selectProposalComments() throws Exception {
        Map<Integer, List<DMComment>> comments = new HashMap<>();

        String query = "SELECT * FROM proposal_comments";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            int parentId = rs.getInt("parentId");
            int proposalId = rs.getInt("proposalId");
            int userId = rs.getInt("userId");
            String date = rs.getDate("date").toString();
            String time = rs.getTime("time").toString();
            String text = rs.getString("text");
            int votes = rs.getInt("numVotes");
            int votesUp = rs.getInt("numPositiveVotes");
            int votesDown = rs.getInt("numNegativeVotes");

            DMComment comment = new DMComment(id, parentId, proposalId, userId, date, time, text, votes, votesUp, votesDown);
            if (!comments.containsKey(proposalId)) {
                comments.put(proposalId, new ArrayList<>());
            }
            comments.get(proposalId).add(comment);
        }
        rs.close();

        return comments;
    }

    public Map<Integer, List<String>> selectProposalDistricts() throws Exception {
        Map<Integer, List<String>> proposalDistricts = new HashMap<>();

        String query = "SELECT * FROM proposal_locations";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String district = rs.getString("district");

            if (!proposalDistricts.containsKey(id)) {
                proposalDistricts.put(id, new ArrayList<>());
            }
            if (!proposalDistricts.get(id).contains(district)) {
                proposalDistricts.get(id).add(district);
            }
        }
        rs.close();

        return proposalDistricts;
    }

    public Map<Integer, List<DMTopic>> selectProposalTopics() throws Exception {
        Map<Integer, List<DMTopic>> results = new HashMap<>();
        String query = "SELECT * FROM proposal_topics";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String topic = rs.getString("topic");
            double weight = rs.getDouble("weight");
            double nWeight = rs.getDouble("n_weight");
            String source = rs.getString("source");
            DMTopic result = new DMTopic(topic, weight, nWeight, source);

            if (!results.containsKey(id)) {
                results.put(id, new ArrayList<>());
            }
            results.get(id).add(result);
        }
        rs.close();
        return results;
    }

    public Map<Integer, List<DMCategory>> selectProposalCategories() throws Exception {
        Map<Integer, List<DMCategory>> results = new HashMap<>();
        String query = "SELECT * FROM proposal_categories";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String category = rs.getString("category");
            double weight = rs.getDouble("weight");
            double nWeight = rs.getDouble("n_weight");
            String source = rs.getString("source");
            DMCategory result = new DMCategory(category, weight, nWeight, source);

            if (!results.containsKey(id)) {
                results.put(id, new ArrayList<>());
            }
            results.get(id).add(result);
        }
        rs.close();
        return results;
    }

    public Map<Integer, List<DMCommentTree>> selectCommentTrees() throws Exception {
        Map<Integer, List<DMComment>> proposalComments = this.selectProposalComments();
        List<Integer> proposalIds = new ArrayList<>(proposalComments.keySet());
        Collections.sort(proposalIds);

        Map<Integer, List<DMCommentTree>> proposalTrees = new HashMap<>();
        for (int proposalId : proposalIds) {
            proposalTrees.put(proposalId, new ArrayList<>());

            List<DMComment> comments = proposalComments.get(proposalId);

            List<Integer> commentIds = new ArrayList<>();
            for (DMComment comment : comments) {
                commentIds.add(comment.getId());
            }

            // Root comments
            for (DMComment comment : comments) {
                int commentId = comment.getId();
                int parentId = comment.getParentId();
                if (parentId == -1) {
                    DMCommentTree root = new DMCommentTree(commentId, 0);
                    if (!proposalTrees.get(proposalId).contains(root)) {
                        proposalTrees.get(proposalId).add(root);
                    }
                }
            }

            // Root comments' children
            for (DMCommentTree root : proposalTrees.get(proposalId)) {
                root.expand(comments);
            }
        }

        return proposalTrees;
    }

    public static void main(String[] args) {
        try {
            DMDBManager db = new DMDBManager();

            Map<Integer, List<DMComment>> comments = db.selectProposalComments();
            List<Integer> proposalIds = new ArrayList<>(comments.keySet());
            Collections.sort(proposalIds);
            for (int proposalId : proposalIds) {
                System.out.println(proposalId);
                List<DMComment> _comments = comments.get(proposalId);
                for (DMComment comment : _comments) {
                    System.out.println("\t" + comment);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
