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
 * Argument class. The premise justifies, gives reasons for or supports the conclusion (claim).
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
    public String sentenceID;
    public String sentenceText;
    public int userID;
    private boolean isValid;
    private int proposalID;
    
    /**
     * Empty constructor.
     * 
     * @param sentenceID
     * @param userID
     * @param commentID
     * @param parentID
     * @param sentenceText 
     */
    public Argument(String sentenceID, int userID, int commentID, int parentID, String sentenceText) {
        this(sentenceID, userID, commentID, parentID, sentenceText, new Sentence(), new Sentence(), "", new ArgumentLinker(), "NONE");
        this.proposalID = -1;
        
        this.isValid = false;
    }
    
    /**
     * Regular constructor.
     * 
     * @param sentenceID
     * @param userID
     * @param commentID
     * @param parentID
     * @param sentenceText
     * @param claim
     * @param premise
     * @param mainVerb
     * @param linker
     * @param approach 
     */
    public Argument(String sentenceID, int userID, int commentID, int parentID, 
            String sentenceText, Sentence claim, Sentence premise, String mainVerb, ArgumentLinker linker, String approach) {
        this.sentenceID = sentenceID;
        this.userID = userID;
        this.commentID = commentID;
        this.parentID = parentID;
        this.sentenceText = sentenceText;
        this.claim = claim;
        this.premise = premise;
        this.mainVerb = mainVerb;
        this.linker = linker;
        this.approach = approach;
        if (!StringUtils.isEmpty(this.sentenceID)) {
            this.proposalID = Integer.parseInt(StringUtils.getFirstToken(this.sentenceID, "-"));
        }
        
        this.isValid = true;
    }
    
    /**
     * 
     * @param doc 
     */
    public Argument(Document doc) {
        this.sentenceID = doc.getString("argumentID");
        this.userID = (int)doc.get("userID");
        this.commentID = (int)doc.get("commentID");
        this.parentID = (int)doc.get("parentID");
        this.sentenceText = doc.getString("argumentID");
        this.majorClaim = new Sentence(doc.get("majorClaim", Document.class));
        this.claim = new Sentence(doc.get("claim", Document.class));
        this.premise = new Sentence(doc.get("premise", Document.class));
        this.mainVerb = doc.getString("mainVerb");
        this.linker = new ArgumentLinker(doc.get("linker", Document.class));
        this.approach = doc.getString("approach");
        if (!StringUtils.isEmpty(this.sentenceID)) {
            this.proposalID = Integer.parseInt(StringUtils.getFirstToken(this.sentenceID, "-"));
        }
        
        this.isValid = true;
    }
    
    /**
     * Create Document argument.
     *
     * @return
     */
    public Document getDocument() {
        Document doc = new Document();
        doc.append("argumentID", this.sentenceID)
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
                .append("approach", this.approach);
        
        return doc;
    }
    
    /**
     * Create JSON argument.
     *
     * @return
     */
    public JSONObject getJSON() {
        JSONObject json = new JSONObject();
        json.put("proposalID", this.proposalID);
        json.put("userID", this.userID);
        json.put("commentID", this.commentID);
        json.put("parentID", this.parentID);
        json.put("sentence", this.sentenceText);
        json.put("majorClaim",this.majorClaim.getJSON());
        json.put("claim", this.claim.getJSON());
        json.put("premise", this.premise.getJSON());
        json.put("mainVerb", this.mainVerb);
        json.put("linker", this.linker.getJSON());
        json.put("approach", this.approach);
        
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
    public String getString() {
        return String.format("[%s] - %s > %s [vrb: %s, lnk: %s]", 
                this.sentenceID, this.claim.text, this.premise.text, this.mainVerb, this.linker.getString());
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return this.isValid;
    }

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
