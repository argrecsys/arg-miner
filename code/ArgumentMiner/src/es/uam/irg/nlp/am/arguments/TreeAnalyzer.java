/**
 * Copyright 2021
 * Andrés Segura-Tinoco
 * Information Retrieval Group at Universidad Autonoma de Madrid
 *
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the current software. If not, see <http://www.gnu.org/licenses/>.
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
