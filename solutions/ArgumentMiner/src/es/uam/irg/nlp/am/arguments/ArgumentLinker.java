/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

/**
 *
 * @author ansegura
 */
public class ArgumentLinker {
    
    // Class members
    public String category;
    public String linker;
    public String relationType;
    public String subCategory;
    
    /**
     * Class constructor.
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
    }
    
    /**
     * 
     * @return 
     */
    public String getString() {
        return String.format("%s > %s > %s (%s)", this.category, this.subCategory, this.linker, this.relationType);
    }
    
}
