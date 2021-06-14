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

public class ControversyStructureDepth extends Controversy {

    public static final String NAME = "DEPTH";

    public ControversyStructureDepth(DMDBManager db) throws Exception {
        super(db);
    }

    @Override
    public Map<Integer, ControversyScore> getScores() throws Exception {
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

            int depth = 0;
            for (DMCommentTree root : proposalTrees.get(proposalId)) {
                root.expand(comments);
                int d = root.countDepth();
                if (d > depth) {
                    depth = d;
                }
            }

            ControversyScore score = new ControversyScore(proposalId, (double) depth);
            values.put(proposalId, score);
        }

        return values;
    }

    public static void main(String[] args) {
        try {
            DMDBManager db = new DMDBManager();
            
            Map<Integer, DMProposal> proposals = db.selectProposals();

            Controversy controversy = new ControversyStructureDepth(db);
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
