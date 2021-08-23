package es.uam.irg.decidemadrid.db;

import es.uam.irg.db.MySQLDBConnector;
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMProposalSummary;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.nlp.am.arguments.ArgumentLinker;
import es.uam.irg.utils.FunctionUtils;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMDBManager {
    
    // Public constants
    public static final String DB_NAME = "decide.madrid_2019_09";
    public static final String DB_SERVER = "localhost";
    public static final String DB_USERNAME = "root";
    public static final String DB_USERPASSWORD = "";
    
    // Private connector object
    private MySQLDBConnector db;

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
    
    public Map<Integer, DMProposal> selectCustomProposals(int topN) throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();
        Integer[] proposalList = {340, 867, 992, 1267, 1287, 1432, 1985, 4065, 4107, 4671, 
                                  4696, 6600, 7250, 7341, 7700, 8116, 8296, 11402, 11530, 11890, 
                                  12003, 15004, 15538, 15645, 15707, 16479, 16516, 17080, 17524, 17562, 
                                  18138, 18302, 19615, 19803, 23248, 23366, 23783, 24451, 24600, 24693};
        
        String query = "SELECT id, title, userId, date, summary, text, numComments, numSupports " +
                       "  FROM proposals " +
                       " WHERE id IN (" + FunctionUtils.arrayToString(proposalList, ",") + ") " +
                       " LIMIT " + topN + ";";
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

    public Map<Integer, DMProposal> selectProposals() throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();

        String query = "SELECT * FROM proposals;";
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
    
    public Map<Integer, DMProposal> selectProposals(int topN, List<ArgumentLinker> lexicon) throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();
        String whereCond = "";
        
        for (int i = 0; i < lexicon.size(); i++) {
            whereCond += (i > 0 ? " OR " : "") + "summary LIKE '% " + lexicon.get(i).linker + " %'";
        }
        
        String query = "SELECT id, title, userId, date, summary, text, numComments, numSupports " +
                       "  FROM proposals " + 
                       " WHERE " + whereCond +
                       " ORDER BY LENGTH(summary) " +
                       " LIMIT " + topN + ";";
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
    
    public Map<Integer, DMProposalSummary> selectProposalsSummary(List<Argument> arguments) throws Exception {
        Map<Integer, DMProposalSummary> proposals = new HashMap<>();
        Argument argument;
        String proposalID;
        String whereCond = "";
        
        for (int i = 0; i < arguments.size(); i++) {
            argument = arguments.get(i);
            proposalID = argument.sentenceID.split("-")[0];
            whereCond += (i > 0 ? ", " : "") + proposalID;
        }
        System.out.println("Id list: " + whereCond);
        
        String query = "SELECT p.id, p.date, p.title, " +
                       "       IFNULL(GROUP_CONCAT(DISTINCT pc.category), '') AS categories, " +
                       "       IFNULL(GROUP_CONCAT(DISTINCT pd.district), '') AS districts, " +
                       "       IFNULL(GROUP_CONCAT(DISTINCT pt.topic), '') AS topic " +
                       "  FROM proposals AS p " +
                       "  LEFT OUTER JOIN " +
                       "       proposal_categories AS pc ON p.id = pc.id " +
                       "  LEFT OUTER JOIN " +
                       "       proposal_locations AS pd ON p.id = pd.id " +
                       "  LEFT OUTER JOIN " +
                       "       proposal_topics AS pt ON p.id = pt.id " +
                       " WHERE p.id IN (" + whereCond + ")" +
                       " GROUP BY p.id, p.date, p.title;";
        ResultSet rs = this.db.executeSelect(query);
        
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            String date = rs.getString("date");
            String categories = rs.getString("categories").toLowerCase();
            String districts = rs.getString("districts").toLowerCase();
            String topics = rs.getString("topic").toLowerCase();

            DMProposalSummary proposal = new DMProposalSummary(id, title, date, categories, districts, topics);
            proposals.put(id, proposal);
        }
        rs.close();
        
        return proposals;
    }
    
}
