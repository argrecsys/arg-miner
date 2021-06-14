package es.uam.irg.decidemadrid.controversy;

public class ControversyScore {

    private int id;
    private double value;

    public ControversyScore(int id, double value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public double getValue() {
        return value;
    }
    
    void setValue(double value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ControversyScore other = (ControversyScore) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ControversyScore{" + "id=" + id + ", value=" + value + '}';
    }

}
