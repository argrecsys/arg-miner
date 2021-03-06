/**
 * Copyright 2021
 * Ivan Cantador
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
package es.uam.irg.decidemadrid.db;

import es.uam.irg.db.MySQLDBConnector;
import es.uam.irg.decidemadrid.entities.*;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.nlp.am.arguments.ArgumentLinker;
import es.uam.irg.utils.FunctionUtils;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public DMDBManager(Map<String, Object> setup) throws Exception {
        if (setup != null && setup.size() == 4) {
            String dbServer = setup.get("db_server").toString();
            String dbName = setup.get("db_name").toString();
            String dbUserName = setup.get("db_user_name").toString();
            String dbUserPassword = setup.get("db_user_pw").toString();

            this.db = new MySQLDBConnector();
            this.db.connect(dbServer, dbName, dbUserName, dbUserPassword);
        } else {
            this.db = new MySQLDBConnector();
            this.db.connect(DB_SERVER, DB_NAME, DB_USERNAME, DB_USERPASSWORD);
        }
    }

    @Override
    public void finalize() {
        this.db.disconnect();
    }

    public Map<Integer, DMComment> selectComments() throws Exception {
        Map<Integer, DMComment> comments = new HashMap<>();

        String query = "SELECT * FROM proposal_comments;";
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

    public Map<Integer, DMComment> selectComments(Integer[] proposalIds) throws Exception {
        Map<Integer, DMComment> comments = new HashMap<>();

        if (proposalIds.length > 0) {
            String query = "SELECT id, parentId, proposalId, userId, date, time, text, numVotes, numPositiveVotes, numNegativeVotes "
                    + "  FROM proposal_comments "
                    + " WHERE proposalId IN (" + FunctionUtils.arrayToString(proposalIds, ",") + ");";
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
        }

        return comments;
    }

    public Map<Integer, DMComment> selectComments(List<ArgumentLinker> lexicon) throws Exception {
        Map<Integer, DMComment> comments = new HashMap<>();

        if (lexicon.size() > 0) {
            String whereCond = "";
            for (int i = 0; i < lexicon.size(); i++) {
                whereCond += (i > 0 ? " OR " : "") + "(text LIKE '% " + lexicon.get(i).linker + " %' OR text LIKE '%," + lexicon.get(i).linker + " %' OR text LIKE '%..." + lexicon.get(i).linker + " %')";
            }

            String query = "SELECT id, parentId, proposalId, userId, date, time, text, numVotes, numPositiveVotes, numNegativeVotes "
                    + "  FROM proposal_comments "
                    + " WHERE " + whereCond + ";";
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
        }

        return comments;
    }

    public Map<Integer, DMComment> selectComments2() throws Exception {
        Map<Integer, DMComment> comments = new HashMap<>();

        String query = "SELECT * FROM proposal_comments_2_processed;";
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

    public List<String> selectDistrictsNeighborhoods() throws Exception {
        List<String> locations = new ArrayList<>();

        String query = "SELECT name FROM geo_districts;";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String name = rs.getString("name");
            locations.add(name);
        }
        rs.close();

        query = "SELECT name FROM geo_neighborhoods;";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String name = rs.getString("name");
            locations.add(name);
        }
        rs.close();

        return locations;
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

    public Map<Integer, DMProposal> selectProposals(Integer[] proposalIds) throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();

        if (proposalIds.length > 0) {
            String query = "SELECT id, title, userId, date, summary, text, numComments, numSupports "
                    + "  FROM proposals "
                    + " WHERE id IN (" + FunctionUtils.arrayToString(proposalIds, ",") + ");";
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
        }

        return proposals;
    }

    public Map<Integer, DMProposal> selectProposals(List<ArgumentLinker> lexicon) throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();

        if (lexicon.size() > 0) {
            String whereCond = "";
            for (int i = 0; i < lexicon.size(); i++) {
                whereCond += (i > 0 ? " OR " : "") + "(summary LIKE '% " + lexicon.get(i).linker + " %' OR summary LIKE '%," + lexicon.get(i).linker + " %' OR summary LIKE '%..." + lexicon.get(i).linker + " %')";
            }

            String query = "SELECT id, title, userId, date, summary, text, numComments, numSupports "
                    + "  FROM proposals "
                    + " WHERE " + whereCond + ";";
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
        }

        return proposals;
    }

    public Map<Integer, DMProposal> selectProposals2() throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();

        String query = "SELECT * FROM proposals_2_processed;";
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

        if (arguments.size() > 0) {
            Set<Integer> setIds = new HashSet();
            int proposalID;
            String whereCond = "";

            for (Argument argument : arguments) {
                proposalID = argument.getProposalId();

                if (!setIds.contains(proposalID)) {
                    setIds.add(proposalID);
                    whereCond += (whereCond.equals("") ? "" : ", ") + proposalID;
                }
            }

            String query = "SELECT p.id, p.date, p.title, "
                    + "       IFNULL(GROUP_CONCAT(DISTINCT pc.category), '') AS categories, "
                    + "       IFNULL(GROUP_CONCAT(DISTINCT pd.district), '') AS districts, "
                    + "       IFNULL(GROUP_CONCAT(DISTINCT pt.topic), '') AS topic "
                    + "  FROM proposals AS p "
                    + "  LEFT OUTER JOIN "
                    + "       proposal_categories AS pc ON p.id = pc.id "
                    + "  LEFT OUTER JOIN "
                    + "       proposal_locations AS pd ON p.id = pd.id "
                    + "  LEFT OUTER JOIN "
                    + "       proposal_topics AS pt ON p.id = pt.id "
                    + " WHERE p.id IN (" + whereCond + ")"
                    + " GROUP BY p.id, p.date, p.title;";
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
        }

        return proposals;
    }

}
