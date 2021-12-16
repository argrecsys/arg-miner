/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import es.uam.irg.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.Document;
import org.json.JSONObject;

/**
 * Argument class. The premise justifies, gives reasons for or supports the
 * conclusion (claim).
 *
 * @author ansegura
 */
public class Argument {

    // Class members
    public String approach;
    public Sentence claim;
    public int commentID;
    public ArgumentLinker linker;
    public String mainVerb;
    public Sentence majorClaim;
    public int parentID;
    public Sentence premise;
    public String sentenceText;
    public int userID;
    private String argumentID;
    private boolean isValid;
    private int proposalID;
    private String syntacticTree;
    private int treeLevel;

    /**
     * Regular constructor.
     * 
     * @param argumentID
     * @param userID
     * @param commentID
     * @param parentID
     * @param sentenceText
     * @param claim
     * @param premise
     * @param mainVerb
     * @param linker
     * @param approach
     * @param syntacticTree 
     */
    public Argument(String argumentID, int userID, int commentID, int parentID,
            String sentenceText, Sentence claim, Sentence premise, String mainVerb, ArgumentLinker linker, String approach, String syntacticTree) {
        this.argumentID = argumentID;
        this.userID = userID;
        this.commentID = commentID;
        this.parentID = parentID;
        this.sentenceText = sentenceText;
        this.claim = claim;
        this.premise = premise;
        this.mainVerb = mainVerb;
        this.linker = linker;
        this.approach = approach;
        this.syntacticTree = syntacticTree;

        completeArgument();
    }

    /**
     * Alternative constructor.
     * 
     * @param doc 
     */
    public Argument(Document doc) {
        this.argumentID = doc.getString("argumentID");
        this.userID = (int) doc.get("userID");
        this.commentID = (int) doc.get("commentID");
        this.parentID = (int) doc.get("parentID");
        this.sentenceText = doc.getString("argumentID");
        this.majorClaim = new Sentence(doc.get("majorClaim", Document.class));
        this.claim = new Sentence(doc.get("claim", Document.class));
        this.premise = new Sentence(doc.get("premise", Document.class));
        this.mainVerb = doc.getString("mainVerb");
        this.linker = new ArgumentLinker(doc.get("linker", Document.class));
        this.approach = doc.getString("approach");
        this.syntacticTree = doc.getString("syntacticTree");

        completeArgument();
    }

    /**
     *
     * @param arg
     * @return
     */
    public boolean equals(Argument arg) {
        return (this.claim.equals(arg.claim) && this.premise.equals(arg.premise) && this.linker.equals(arg.linker));
    }

    /**
     * Create Document argument.
     *
     * @return
     */
    public Document getDocument() {
        Document doc = new Document();
        doc.append("argumentID", this.argumentID)
                .append("proposalID", this.proposalID)
                .append("userID", this.userID)
                .append("commentID", this.commentID)
                .append("parentID", this.parentID)
                .append("sentence", this.sentenceText)
                .append("majorClaim", this.majorClaim.getDocument())
                .append("claim", this.claim.getDocument())
                .append("premise", this.premise.getDocument())
                .append("mainVerb", this.mainVerb)
                .append("linker", this.linker.getDocument())
                .append("approach", this.approach)
                .append("syntacticTree", this.syntacticTree);

        return doc;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return this.argumentID;
    }

    /**
     * Create JSON argument.
     *
     * @param withSyntTree
     * @return
     */
    public JSONObject getJSON(boolean withSyntTree) {
        JSONObject json = new JSONObject();
        json.put("proposalID", this.proposalID);
        json.put("userID", this.userID);
        json.put("commentID", this.commentID);
        json.put("parentID", this.parentID);
        json.put("sentence", this.sentenceText);
        json.put("majorClaim", this.majorClaim.getJSON());
        json.put("claim", this.claim.getJSON());
        json.put("premise", this.premise.getJSON());
        json.put("mainVerb", this.mainVerb);
        json.put("linker", this.linker.getJSON());
        json.put("approach", this.approach);
        if (withSyntTree) {
            json.put("syntacticTree", this.syntacticTree);
        }

        return json;
    }

    /**
     *
     * @param majorClaim
     */
    public void setMajorClaim(Sentence majorClaim) {
        this.majorClaim = majorClaim;
    }

    /**
     *
     * @return
     */
    public Set<String> getNounsSet() {
        Set<String> nouns = new HashSet<>();
        nouns.addAll(processNouns(this.majorClaim.nouns));
        nouns.addAll(processNouns(this.claim.nouns));
        nouns.addAll(processNouns(this.premise.nouns));
        return nouns;
    }

    /**
     *
     * @return
     */
    public int getProposalId() {
        return this.proposalID;
    }

    /**
     * 
     * @return 
     */
    @Override
    public String toString() {
        return String.format("[%s] - %s > %s [vrb: %s, lnk: %s]",
                this.argumentID, this.claim.text, this.premise.text, this.mainVerb, this.linker.toString());
    }

    /**
     *
     * @return
     */
    public String getSyntacticTree() {
        return this.syntacticTree;
    }

    /**
     *
     * @return
     */
    public int getTreeLevel() {
        return this.treeLevel;
    }

    /**
     *
     * @return
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Is it a valid argument?
     */
    private void completeArgument() {

        if (!StringUtils.isEmpty(this.approach) && !StringUtils.isEmpty(this.argumentID)) {
            var token = StringUtils.getFirstToken(this.approach, "-").replace("[", "").replace("]", "");
            this.treeLevel = Integer.parseInt(token);
            token = StringUtils.getFirstToken(this.argumentID, "-");
            this.proposalID = Integer.parseInt(token);
            this.isValid = true;

        } else {
            this.treeLevel = -1;
            this.proposalID = -1;
            this.isValid = false;
        }
    }

    /**
     *
     * @param nouns
     * @return
     */
    private List<String> processNouns(List<String> nouns) {
        List<String> newList = new ArrayList<>();
        String noun;

        for (String item : nouns) {
            noun = item.trim();
            if (noun.length() > 0) {
                newList.add(noun.toLowerCase());
            }
        }

        return newList;
    }

}
