package es.uam.irg.decidemadrid.controversy.metrics;

import es.uam.irg.decidemadrid.controversy.ControversyScore;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControversyVotesProduct extends Controversy {

    public static final String NAME = "COMMENT_VOTES_PRODUCT";

    public ControversyVotesProduct(DMDBManager db) throws Exception {
        super(db);
    }

    @Override
    public Map<Integer, ControversyScore> getScores() throws Exception {
        Map<Integer, ControversyScore> values = new HashMap<>();

        Map<Integer, List<DMComment>> comments = db.selectProposalComments();
        List<Integer> ids = new ArrayList<>(comments.keySet());
        Collections.sort(ids);

        for (int id : ids) {
            List<DMComment> _comments = comments.get(id);
            double pos = 0;
            double neg = 0;
            for (DMComment comment : _comments) {
                pos += comment.getNumVotesUp();
                neg += comment.getNumVotesDown();
            }
            double value = 0;
            if (pos != 0 && neg != 0) {
                value = 1 + Math.min(pos, neg) * Math.min(pos, neg) / Math.max(pos, neg);
            }
            ControversyScore score = new ControversyScore(id, value);
            values.put(id, score);
        }

        return values;
    }

    public static void main(String[] args) {
        try {
            DMDBManager db = new DMDBManager();

            Map<Integer, String> titles = db.selectProposalTitles();

            Controversy controversy = new ControversyVotesProduct(db);
            Map<Integer, ControversyScore> scores = controversy.getScores();

            List<ControversyScore> _scores = new ArrayList<>(scores.values());
            Collections.sort(_scores, Comparator.comparing(ControversyScore::getValue));

            for (ControversyScore score : _scores) {
                double value = score.getValue();
                int id = score.getId();
                System.out.println(id + "\t" + value + "\t" + titles.get(id));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
