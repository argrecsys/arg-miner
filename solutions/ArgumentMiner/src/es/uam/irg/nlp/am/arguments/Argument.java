/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import es.uam.irg.utils.StringUtils;
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
    public ArgumentLinker linker;
    public String mainVerb;
    public Sentence majorClaim;
    public Sentence premise;
    public String sentenceID;
    public String sentenceText;
    private boolean isValid;
    private int proposalID;
    
    /**
     * Empty constructor.
     * 
     * @param sentenceID
     * @param sentenceText 
     */
    public Argument(String sentenceID, String sentenceText) {
        this(sentenceID, sentenceText, new Sentence(), new Sentence(), "", new ArgumentLinker(), "NONE");
        this.proposalID = -1;
        
        this.isValid = false;
    }
    
    /**
     * Regular constructor.
     * 
     * @param sentenceID
     * @param sentenceText
     * @param claim
     * @param premise
     * @param mainVerb
     * @param linker
     * @param approach 
     */
    public Argument(String sentenceID, String sentenceText, Sentence claim, Sentence premise, String mainVerb, ArgumentLinker linker, String approach) {
        this.sentenceID = sentenceID;
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
     * Create Document argument.
     *
     * @return
     */
    public Document getDocument() {
        Document doc = new Document();
        doc.append("argumentID", this.sentenceID)
                .append("proposalID", this.proposalID)
                .append("sentence", this.sentenceText)
                .append("majorClaim", this.majorClaim.getDocument())
                .append("claim", this.claim.getDocument())
                .append("premise", this.premise.getDocument())
                .append("linker", this.linker.getDocument())
                .append("mainVerb", this.mainVerb)
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
        json.put("sentence", this.sentenceText);
        json.put("majorClaim",this.majorClaim.getJSON());
        json.put("claim", this.claim.getJSON());
        json.put("premise", this.premise.getJSON());
        json.put("linker", this.linker.getJSON());
        json.put("mainVerb", this.mainVerb);
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
    
}
