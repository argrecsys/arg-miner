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
    public String claim;
    public String mainVerb;
    public String premise;
    public String relationType;
    private boolean isValid;
    
    /**
     * Empty constructor.
     */
    public Argument() {
        this("", "", "", "");
        this.isValid = false;
    }
    
    /**
     * 
     * 
     * @param premise
     * @param claim
     * @param mainVerb
     * @param relationType 
     */
    public Argument(String premise, String claim, String mainVerb, String relationType) {
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
        return String.format("%s > %s [lnk: %s, vrb: %s]", this.claim, this.premise, this.relationType, this.mainVerb);
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return this.isValid;
    }
    
}
