package es.uam.irg.decidemadrid.controversy.metrics;

import es.uam.irg.decidemadrid.controversy.ControversyScore;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControversyNumberComments extends Controversy {

    public static final String NAME = "NUMBER_COMMENTS";

    public ControversyNumberComments(DMDBManager db) throws Exception {
        super(db);
    }

    @Override
    public Map<Integer, ControversyScore> getScores() throws Exception {
        Map<Integer, ControversyScore> values = new HashMap<>();
        Map<Integer, List<DMComment>> proposalComments = db.selectProposalComments();
        for (int proposalId : proposalComments.keySet()) {
            int numComments = proposalComments.get(proposalId).size();
            ControversyScore score = new ControversyScore(proposalId, numComments);
            values.put(proposalId, score);
        }
        return values;
    }

}
