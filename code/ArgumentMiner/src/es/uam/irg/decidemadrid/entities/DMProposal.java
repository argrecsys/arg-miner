package es.uam.irg.decidemadrid.entities;

public class DMProposal {

    private int id;
    private String title;
    private int userId;
    private String date;
    private int day;
    private int month;
    private int year;
    private String summary;
    private String text;
    private int numComments;
    private int numSupports;
    
    public DMProposal(int id, String title, int userId, String date, String summary, String text, int numComments, int numSupports) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.date = date;
        String tokens[] = date.split("-");
        this.day = Integer.valueOf(tokens[2]);
        this.month = Integer.valueOf(tokens[1]);
        this.year = Integer.valueOf(tokens[0]);
        this.summary = summary;
        this.text = text;
        this.numComments = numComments;
        this.numSupports = numSupports;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String getSummary() {
        return summary;
    }

    public String getText() {
        return text;
    }

    public int getNumComments() {
        return numComments;
    }

    public int getNumSupports() {
        return numSupports;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.id;
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
        final DMProposal other = (DMProposal) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DMProposal{" + "id=" + id + ", title=" + title + ", userId=" + userId + ", date=" + date + ", summary=" + summary + ", text=" + text + ", numComments=" + numComments + ", numSupports=" + numSupports + '}';
    }

}
