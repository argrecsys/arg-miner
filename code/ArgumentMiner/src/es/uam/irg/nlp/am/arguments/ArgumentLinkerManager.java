/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import es.uam.irg.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ansegura
 */
public class ArgumentLinkerManager {
    
    private final static boolean DUPLICATE_CLEAN_LINKER = true;
    private Map<String, Map<String, List<ArgumentLinker>>> taxonomy;

    /**
     * Class constructor.
     */
    public ArgumentLinkerManager() {
        this.taxonomy = new HashMap<>();
    }
    
    /**
     * Add item by parameters.
     *
     * @param category
     * @param subCategory
     * @param relationType
     * @param linkerText
     */
    public void addLinker(String category, String subCategory, String relationType, String linkerText) {
        ArgumentLinker linker = new ArgumentLinker(category, subCategory, relationType, linkerText);
        this.addLinker(linker);
        
        if (DUPLICATE_CLEAN_LINKER) {
            String cleanLinker = StringUtils.unaccent(linker.linker);
            if (!linker.linker.equals(cleanLinker)) {
                ArgumentLinker newLinker = new ArgumentLinker(category, subCategory, relationType, cleanLinker);
                this.addLinker(newLinker);
            }
        }
    }
    
    /**
     * 
     * @param sorted
     * @return 
     */
    public List<ArgumentLinker> getLexicon(boolean sorted) {
        List<ArgumentLinker> lexicon = new ArrayList<>();
        
        this.taxonomy.entrySet().forEach(entry -> {
            for (Map.Entry<String, List<ArgumentLinker>> subentry : entry.getValue().entrySet()) {
                List<ArgumentLinker> items = subentry.getValue();
                
                for (int i = 0; i < items.size(); i++) {
                    ArgumentLinker currLinker = items.get(i);
                    lexicon.add(currLinker);
                }
            }
        });
        
        // Sort list
        if (sorted) {
            Collections.sort(lexicon, new Comparator<ArgumentLinker>() {
                @Override
                public int compare(ArgumentLinker o1, ArgumentLinker o2) {
                    return o2.linker.length() - o1.linker.length();
                }
            });
        }
        
        return lexicon;
    }
    
    /**
     *
     * @return
     */
    public Map<String, Map<String, List<ArgumentLinker>>> getTaxonomy() {
        return this.taxonomy;
    }
    
    /**
     * 
     * @return 
     */
    public boolean isEmpty() {
        return (this.taxonomy.isEmpty());
    }
    
    /**
     * Add linker by object.
     *
     * @param linker
     */
    private void addLinker(ArgumentLinker linker) {
        
        Map<String, List<ArgumentLinker>> subcategory;
        if (this.taxonomy.containsKey(linker.category)) {
            subcategory = this.taxonomy.get(linker.category);
        }
        else {
            subcategory = new HashMap<>();
            this.taxonomy.put(linker.category, subcategory);
        }
        
        List<ArgumentLinker> linkers;
        if (subcategory.containsKey(linker.subCategory)) {
            linkers = subcategory.get(linker.subCategory);
        }
        else {
            linkers = new ArrayList<>();
            subcategory.put(linker.subCategory, linkers);
        }
        
        linkers.add(linker);
    }
    
}
