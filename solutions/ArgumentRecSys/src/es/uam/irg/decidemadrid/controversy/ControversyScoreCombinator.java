package es.uam.irg.decidemadrid.controversy;

import es.uam.irg.decidemadrid.controversy.metrics.Controversy;
import es.uam.irg.decidemadrid.db.DMDBManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ControversyScoreCombinator extends Controversy {

    private List<Map<Integer, ControversyScore>> scoresLists;
    private List<String> metricNames;
    private Map<Integer, ControversyScore> scores;

    public ControversyScoreCombinator(DMDBManager db, List<Map<Integer, ControversyScore>> scoresLists, List<String> metricNames) throws Exception {
        super(db);
        this.scoresLists = scoresLists;
        this.metricNames = metricNames;
        this.combine(scoresLists);
    }

    @Override
    public Map<Integer, ControversyScore> getScores() throws Exception {
        return this.scores;
    }

    private void combine(List<Map<Integer, ControversyScore>> scoresLists) {
        // Normalizing
        for (int i = 0; i < scoresLists.size(); i++) {
            Map<Integer, ControversyScore> _scores = scoresLists.get(i);
            double max = 0.0;
            for (int id : _scores.keySet()) {
                double value = _scores.get(id).getValue();
                if (max < value) {
                    max = value;
                }
            }
            for (int id : _scores.keySet()) {
                double value = _scores.get(id).getValue();
                value /= max;
                scoresLists.get(i).get(id).setValue(value);
            }
        }

        // Aggregating
        int numLists = scoresLists.size();
        Set<Integer> ids = scoresLists.get(0).keySet();
        this.scores = new HashMap<>();
        for (int id : ids) {
            double value = 0.0;
            for (int i = 0; i < scoresLists.size(); i++) {
                value += scoresLists.get(i).get(id).getValue();
            }
            value /= numLists;
            this.scores.put(id, new ControversyScore(id, value));
        }
    }

    public void correlationCoefficient() {
        for (int i = 0; i < metricNames.size(); i++) {
            String name = metricNames.get(i);
            System.out.print("\t" + name);
        }
        System.out.println("");

        for (int i = 0; i < scoresLists.size(); i++) {
            String name1 = metricNames.get(i);
            Map<Integer, ControversyScore> list1 = scoresLists.get(i);
            System.out.print(name1);
            for (int j = 0; j < scoresLists.size(); j++) {
                Map<Integer, ControversyScore> list2 = scoresLists.get(j);
                double correlation = correlationCoefficient(list1, list2);
                System.out.print("\t" + correlation);
            }
            System.out.println("");
        }
    }

    private double correlationCoefficient(Map<Integer, ControversyScore> x, Map<Integer, ControversyScore> y) {
        int n = x.size();

        double sum_X = 0, sum_Y = 0, sum_XY = 0;
        double squareSum_X = 0, squareSum_Y = 0;

        for (int id : x.keySet()) {
            double xValue = x.get(id).getValue();
            double yValue = y.get(id).getValue();

            sum_X += xValue;
            sum_Y += yValue;
            sum_XY += xValue * yValue;

            squareSum_X += xValue * xValue;
            squareSum_Y += yValue * yValue;
        }

        double corr = (n * sum_XY - sum_X * sum_Y)
                / Math.sqrt((n * squareSum_X - sum_X * sum_X) * (n * squareSum_Y - sum_Y * sum_Y));

        return corr;
    }

}
