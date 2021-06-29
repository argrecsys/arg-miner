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
    
    public String category;
    public String subCategory;
    public String relationType;
    public String linker;
    
    public ArgumentLinker(String category, String subCategory, String relationType, String linker) {
        this.category = category;
        this.subCategory = subCategory;
        this.relationType = relationType;
        this.linker = linker;
    }
    
}
