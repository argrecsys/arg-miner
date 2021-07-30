package es.uam.irg.decidemadrid.entities;

import java.util.Objects;

public class DMTopic {

    private String name;
    private double weight;
    private double nWeight;
    private String source;

    public DMTopic(String name, double weight, double nWeight, String source) {
        this.name = name;
        this.weight = weight;
        this.nWeight = nWeight;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public double getNormalizedWeight() {
        return nWeight;
    }

    public String getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.name);
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
        final DMTopic other = (DMTopic) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DMTopic{" + "name=" + name + ", weight=" + weight + ", nWeight=" + nWeight + ", source=" + source + '}';
    }

}
