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
    public String sentenceID;
    public String sentence;
    public String claim;
    public String mainVerb;
    public String premise;
    public String relationType;
    private boolean isValid;
    
    /**
     * Empty constructor.
     * 
     * @param sentenceID
     * @param sentence 
     */
    public Argument(String sentenceID, String sentence) {
        this(sentenceID, sentence, "", "", "", "");
        this.isValid = false;
    }
    
    /**
     * Regular constructor.
     * 
     * @param sentenceID
     * @param sentence
     * @param premise
     * @param claim
     * @param mainVerb
     * @param relationType 
     */
    public Argument(String sentenceID, String sentence, String premise, String claim, String mainVerb, String relationType) {
        this.sentenceID = sentenceID;
        this.sentence = sentence;
        this.premise = premise;
        this.claim = claim;
        this.mainVerb = mainVerb;
        this.relationType = relationType;
        this.isValid = true;
    }
    
    /**
     * 
     * @return 
     */
    public String getString() {
        return String.format("[%s] - %s > %s [lnk: %s, vrb: %s]", this.sentenceID, this.claim, this.premise, this.relationType, this.mainVerb);
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return this.isValid;
    }
    
}
