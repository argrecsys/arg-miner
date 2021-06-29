package es.uam.irg.decidemadrid.entities;

public class DMComment {

    private int id;
    private int parentId;
    private int proposalId;
    private int userId;
    private String date;
    private String time;  
    private String text;
    private int numVotes;
    private int numVotesUp;
    private int numVotesDown;

    public DMComment(int id, int parentId, int proposalId, int userId, String date, String time, String text, int numVotes, int numVotesUp, int numVotesDown) {
        this.id = id;
        this.parentId = parentId;
        this.proposalId = proposalId;
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.text = text;
        this.numVotes = numVotes;
        this.numVotesUp = numVotesUp;
        this.numVotesDown = numVotesDown;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }

    public int getProposalId() {
        return proposalId;
    }

    public int getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public int getNumVotes() {
        return numVotes;
    }

    public int getNumVotesUp() {
        return numVotesUp;
    }

    public int getNumVotesDown() {
        return numVotesDown;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.id;
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
        final DMComment other = (DMComment) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DMComment{" + "id=" + id + ", parentId=" + parentId + ", proposalId=" + proposalId + ", userId=" + userId + ", date=" + date + ", time=" + time + ", text=" + text + ", numVotes=" + numVotes + ", numVotesUp=" + numVotesUp + ", numVotesDown=" + numVotesDown + '}';
    }

}
