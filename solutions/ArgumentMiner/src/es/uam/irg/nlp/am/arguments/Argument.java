/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public String sentence;
    public String sentenceID;
    public String approach;
    private List<String> entityList;
    private boolean isValid;
    
    /**
     * Empty constructor.
     * 
     * @param sentenceID
     * @param sentence 
     */
    public Argument(String sentenceID, String sentence) {
        this(sentenceID, sentence, "", "", "", "", "NONE");
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
     * @param approach 
     */
    public Argument(String sentenceID, String sentence, String premise, String claim, String mainVerb, String relationType, String approach) {
        this.sentenceID = sentenceID;
        this.sentence = sentence;
        this.premise = premise;
        this.claim = claim;
        this.mainVerb = mainVerb;
        this.relationType = relationType;
        this.approach = approach;
        this.entityList = new ArrayList<>();
        this.isValid = true;
    }
    
    /**
     *
     * @return 
     */
    public List<String> getEntityList() {
        return this.entityList;
    }
    
    /**
     *
     * @param entities 
     */
    public void setEntityList(Map<String, String> entities) {
        entities.entrySet().forEach(entry -> {
            this.entityList.add(entry.getKey() + ":" + entry.getValue());
        });
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
