package es.uam.irg.decidemadrid.entities;

import java.util.Objects;

public class DMLocation {
    
    private String district;
    private String neighborhood;
    private String location;
    private String tag;

    public DMLocation(String district, String neighborhood, String location, String tag) throws Exception {
        if (district == null) {
            throw new IllegalArgumentException("Null district");
        }
        this.district = district;
        this.neighborhood = neighborhood != null ? neighborhood : "";
        this.location = location != null ? location : "";
        this.tag = tag != null ? tag : "";
    }

    public String getDistrict() {
        return district;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getLocation() {
        return location;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.district);
        hash = 53 * hash + Objects.hashCode(this.neighborhood);
        hash = 53 * hash + Objects.hashCode(this.location);
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
        final DMLocation other = (DMLocation) obj;
        if (!Objects.equals(this.district, other.district)) {
            return false;
        }
        if (!Objects.equals(this.neighborhood, other.neighborhood)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DMLocation{" + "district=" + district + ", neighborhood=" + neighborhood + ", location=" + location + ", tag=" + tag + '}';
    }
}
