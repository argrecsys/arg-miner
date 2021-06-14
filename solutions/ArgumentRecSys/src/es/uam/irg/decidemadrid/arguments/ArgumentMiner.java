package es.uam.irg.decidemadrid.arguments;

import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.decidemadrid.entities.DMCommentTree;
import es.uam.irg.decidemadrid.entities.DMProposal;
import java.util.List;
import java.util.Map;

public class ArgumentMiner {

    public static void main(String[] args) {
        try {
            DMDBManager db = new DMDBManager();

            Map<Integer, DMProposal> proposals = db.selectProposals();
            Map<Integer, DMComment> comments = db.selectComments();

            Map<Integer, List<DMCommentTree>> proposalCommentTrees = db.selectCommentTrees();
            for (int proposalId : proposalCommentTrees.keySet()) {
                System.out.println("********************************");
                System.out.println("Proposal " + proposalId + " (u" + proposals.get(proposalId).getUserId() + "): " + proposals.get(proposalId).getTitle());
                System.out.println("Summary:" + proposals.get(proposalId).getSummary());
                System.out.println("Text:" + proposals.get(proposalId).getText());
                System.out.println("Comments:");
                List<DMCommentTree> trees = proposalCommentTrees.get(proposalId);
                for (DMCommentTree tree : trees) {
                    System.out.print(tree.toString(comments));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
