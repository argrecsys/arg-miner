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
    
    /**
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
    }
    
    /**
     * 
     * @return 
     */
    public String getString() {
        return String.format("%s > %s [%s]", this.premise, this.claim, this.relationType);
    }
    
}
