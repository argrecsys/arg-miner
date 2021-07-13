/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

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
    
    /**
     * Empty constructor.
     * 
     * @param sentenceID
     * @param sentenceText 
     */
    public Argument(String sentenceID, String sentenceText) {
        this(sentenceID, sentenceText, new Sentence(), new Sentence(), "", new ArgumentLinker(), "NONE");
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
        this.isValid = true;
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
