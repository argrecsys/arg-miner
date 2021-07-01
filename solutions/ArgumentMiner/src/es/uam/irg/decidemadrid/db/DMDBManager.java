package es.uam.irg.decidemadrid.db;

import es.uam.irg.db.MySQLDBConnector;
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.nlp.am.arguments.ArgumentLinkerList;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DMDBManager {
    
    // Public constants
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
    
    public Map<Integer, DMProposal> selectCustomProposals(int topN, ArgumentLinkerList linkers) throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();
        
        String query = "SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation " +
                       "  FROM proposals " +
                       " WHERE summary LIKE '%porque%' " +
                       " ORDER BY LENGTH(summary) " +
                       " LIMIT " + topN + ";";
        
        query = "SELECT id, url, code, title, userId, date, summary, text, numComments, status, numSupports, isAssociation " +
                "  FROM proposals " +
                " WHERE id IN (992, 19615); ";
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

}
