/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ansegura
 */
public class ArgumentLinkerList {
    
    private List<ArgumentLinker> linkers;
    private int nItems;
            
    /**
     * Class constructor.
     */
    public ArgumentLinkerList() {
        this.linkers = new LinkedList<>();
        this.nItems = 0;
    }
    
    /**
     * 
     * @param linker 
     */
    public void addLinker(ArgumentLinker linker) {
        this.linkers.add(linker);
        this.nItems++;
    }
    
    /**
     * 
     * @param category
     * @param subCategory
     * @param relationType
     * @param linkerText 
     */
    public void addLinker(String category, String subCategory, String relationType, String linkerText) {
        ArgumentLinker linker = new ArgumentLinker(category, subCategory, relationType, linkerText);
        this.addLinker(linker);
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public ArgumentLinker getLinker(int index) {
        ArgumentLinker linker = null;
        
        if (index < this.nItems) {
            linker = this.linkers.get(index);
        }
        
        return linker;
    }
    
    /**
     * 
     * @param linkerText
     * @return 
     */
    public ArgumentLinker getLinker(String linkerText) {
        ArgumentLinker linker = null;
        
        for (int i = 0; i < this.nItems && linker == null; i++) {
            ArgumentLinker currLinker = this.linkers.get(i);
            if (linkerText.equals(currLinker.linker)) {
                linker = currLinker;
            }
        }
        
        return linker;
    }
    
    /**
     * 
     * @return 
     */
    public int getSize() {
        return nItems;
    }
}
