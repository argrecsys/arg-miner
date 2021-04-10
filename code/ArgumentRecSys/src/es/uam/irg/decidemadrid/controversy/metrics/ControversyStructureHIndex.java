package es.uam.irg.decidemadrid.controversy.metrics;

import es.uam.irg.decidemadrid.entities.DMCommentTree;
import es.uam.irg.decidemadrid.controversy.ControversyScore;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.decidemadrid.entities.DMProposal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControversyStructureHIndex extends Controversy {

    public static final String NAME = "H_INDEX";

    public ControversyStructureHIndex(DMDBManager db) throws Exception {
        super(db);
    }

    @Override
    public Map<Integer, ControversyScore> getScores() throws Exception {
        Map<Integer, DMProposal> proposals = db.selectProposals();

        Map<Integer, ControversyScore> values = new HashMap<>();

        Map<Integer, List<DMComment>> proposalComments = db.selectProposalComments();
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

            for (DMCommentTree root : proposalTrees.get(proposalId)) {
                root.expand(comments);
            }

            Map<Integer, Integer> nodesAtLevel = new HashMap<>();
            for (DMCommentTree root : proposalTrees.get(proposalId)) {
                root.countNodesPerLevel(nodesAtLevel);
            }

            int hIndex = 0;
            for (int level : nodesAtLevel.keySet()) {
                int nodes = nodesAtLevel.get(level);
                if (level + 1 <= nodes) {  // may be: level + 1 < nodes
                    if (hIndex < level + 1) {
                        hIndex = level + 1;
                    }
                }
            }

            // Break ties
            int numComments = proposals.get(proposalId).getNumComments();
            double value2 = numComments > 0 ? 1.0 / numComments : 0.0;

            ControversyScore score = new ControversyScore(proposalId, hIndex + value2);
            values.put(proposalId, score);
        }
//
//        for (int id : proposalTrees.keySet()) {
//            if (id != 25155) {
//                continue;
//            }
//            System.out.println(id + " ******************************************");
//            for (CommentTreeNode tree : proposalTrees.get(id)) {
//                System.out.print(tree);
//            }
//
//        }
//        
        return values;
    }

    public static void main(String[] args) {
        try {
            DMDBManager db = new DMDBManager();

            Map<Integer, DMProposal> proposals = db.selectProposals();

            Controversy controversy = new ControversyStructureHIndex(db);
            Map<Integer, ControversyScore> scores = controversy.getScores();

            List<ControversyScore> _scores = new ArrayList<>(scores.values());
            Collections.sort(_scores, Comparator.comparing(ControversyScore::getValue));

            for (ControversyScore score : _scores) {
                double value = score.getValue();
                int id = score.getId();
                System.out.println(id + "\t" + value + "\t" + proposals.get(id).getTitle());
                //System.out.println(proposals.get(id).getNumVotes() + value + "\t");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
