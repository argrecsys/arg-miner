package es.uam.irg.decidemadrid.controversy.metrics;

import es.uam.irg.decidemadrid.controversy.ControversyScore;
import es.uam.irg.decidemadrid.db.DMDBManager;
import java.util.Map;

public abstract class Controversy {

    protected DMDBManager db;

    public Controversy(DMDBManager db) {
        this.db = db;
    }

    public DMDBManager getDB() {
        return db;
    }

    // proposalId - controversy values
    public abstract Map<Integer, ControversyScore> getScores() throws Exception;

}
