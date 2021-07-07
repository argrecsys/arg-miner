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
    public String approach;
    public String claim;
    public String mainVerb;
    public String premise;
    public ArgumentLinker linker;
    public String sentenceID;
    public String sentenceText;
    private List<String> entityList;
    private boolean isValid;
    private List<String> nounList;
    
    /**
     * Empty constructor.
     * 
     * @param sentenceID
     * @param sentenceText 
     */
    public Argument(String sentenceID, String sentenceText) {
        this(sentenceID, sentenceText, "", "", "", "NONE", new ArgumentLinker());
        this.isValid = false;
    }
    
    /**
     * Regular constructor.
     * 
     * @param sentenceID
     * @param sentenceText
     * @param premise
     * @param claim
     * @param mainVerb
     * @param approach
     * @param linker 
     */
    public Argument(String sentenceID, String sentenceText, String premise, String claim, String mainVerb, String approach, ArgumentLinker linker) {
        this.sentenceID = sentenceID;
        this.sentenceText = sentenceText;
        this.premise = premise;
        this.claim = claim;
        this.mainVerb = mainVerb;
        this.approach = approach;
        this.linker = linker;
        this.entityList = new ArrayList<>();
        this.nounList = new ArrayList<>();
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
        this.entityList.addAll(entities.keySet());
    }
    
    /**
     *
     * @return
     */
    public List<String> getNounList() {
        return this.nounList;
    }
    
    /**
     * 
     * @param nounList 
     */
    public void setNounList(List<String> nounList) {
        this.nounList = nounList;
    }
    
    /**
     * 
     * @return 
     */
    public String getString() {
        return String.format("[%s] - %s > %s [vrb: %s, lnk: %s]", this.sentenceID, this.claim, this.premise, this.mainVerb, this.linker.getString());
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return this.isValid;
    }
    
}
