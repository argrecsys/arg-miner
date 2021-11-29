/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import es.uam.irg.nlp.am.Constants;
import es.uam.irg.utils.FunctionUtils;
import es.uam.irg.utils.StringUtils;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class TreeAnalyzer {
    private final List<ArgumentLinker> linkers;
    
    public TreeAnalyzer(List<ArgumentLinker> linkers) {
        this.linkers = linkers;
    }
    
    /**
     * 
     * @param text
     * @return 
     */
    public ArgumentLinker checkNodeText(String text) {
        ArgumentLinker lnk = null;
        
        for (ArgumentLinker cand : this.linkers) {
            String nGram = getNGram(text, cand.nTokens);

            if (cand.isEquals(nGram)) {
                lnk = cand;
                break;
            }
        }
        
        return lnk;
    }
    
    /**
     * 
     * @param text
     * @param nTokens
     * @return 
     */
    private String getNGram(String text, int nTokens) {
        String newText = StringUtils.cleanText(text, StringUtils.CLEAN_BOTH);
        String[] tokens = newText.split(" ");
        return getNGram(tokens, 0, nTokens);
    }
    
    /**
     * 
     * @param tokens
     * @param init
     * @param nTokens
     * @return 
     */
    private String getNGram(String[] tokens, int init, int nTokens) {
        String nGram = "";
        
        try {
            if (tokens.length > 0) {
                String[] subList = FunctionUtils.getSubArray(tokens, init, init + nTokens);
                nGram = FunctionUtils.arrayToString(subList, Constants.NGRAMS_DELIMITER);
            }
        }
        catch (Exception ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nGram;
    }
}
