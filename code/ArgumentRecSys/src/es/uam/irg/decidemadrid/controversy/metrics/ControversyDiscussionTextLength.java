package es.uam.irg.decidemadrid.controversy.metrics;

import es.uam.irg.decidemadrid.controversy.ControversyScore;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControversyDiscussionTextLength extends Controversy {

    public static final String NAME = "DISUSSION_TEXT_LENGTH";

    public ControversyDiscussionTextLength(DMDBManager db) throws Exception {
        super(db);
    }

    @Override
    public Map<Integer, ControversyScore> getScores() throws Exception {
        Map<Integer, ControversyScore> values = new HashMap<>();
        Map<Integer, List<DMComment>> proposalComments = db.selectProposalComments();
        for (int proposalId : proposalComments.keySet()) {
            double length = 0.0;
            for (DMComment comment : proposalComments.get(proposalId)) {
                length += comment.getText().length();
            }
            ControversyScore score = new ControversyScore(proposalId, length);
            values.put(proposalId, score);
        }
        return values;
    }

}
