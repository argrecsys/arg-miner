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
    private final int nLinkers;

    public TreeAnalyzer(List<ArgumentLinker> linkers) {
        this.linkers = linkers;
        this.nLinkers = linkers.size();
    }

    /**
     * Checks if the text has or starts with a linker.
     *
     * @param text
     * @return
     */
    public ArgumentLinker textHasLinker(String text) {
        ArgumentLinker linker = null;

        if (!StringUtils.isEmpty(text)) {
            for (int i = 0; i < this.nLinkers && linker == null; i++) {
                ArgumentLinker cand = this.linkers.get(i);
                String nGram = getNGram(text.toLowerCase(), cand.nTokens);

                if (cand.isEquals(nGram)) {
                    linker = cand;
                }
            }
        }

        return linker;
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
        } catch (Exception ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nGram;
    }
}
