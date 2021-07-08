/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import es.uam.irg.nlp.am.Constants;

/**
 *
 * @author ansegura
 */
public class ArgumentLinker {
    
    // Class members
    public String category;
    public String linker;
    public int nTokens;
    public String relationType;
    public String subCategory;
    private String spLinker;
    
    /**
     * Empty constructor.
     */
    public ArgumentLinker() {
        this("", "", "", "");
    }
    
    /**
     * Regular constructor.
     * 
     * @param category
     * @param subCategory
     * @param relationType
     * @param linker 
     */
    public ArgumentLinker(String category, String subCategory, String relationType, String linker) {
        this.category = category;
        this.subCategory = subCategory;
        this.relationType = relationType;
        this.linker = linker;
        this.spLinker = linker.replace(" ", Constants.NGRAMS_DELIMITER);
        this.nTokens = linker.split(" ").length;
    }
    
    /**
     * 
     * @return 
     */
    public String getString() {
        return String.format("%s > %s > [%s] %s (%s)", this.category, this.subCategory, this.nTokens, this.linker, this.relationType);
    }
    
    /**
     * 
     * @param nGram
     * @return 
     */
    public boolean isEquals(String nGram) {
        return this.spLinker.equals(nGram);
    }
}
