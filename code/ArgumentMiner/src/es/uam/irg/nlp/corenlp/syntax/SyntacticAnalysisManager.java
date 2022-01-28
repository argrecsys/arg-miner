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
package es.uam.irg.nlp.corenlp.syntax;

import es.uam.irg.nlp.am.arguments.ArgumentEngine;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebank;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebankNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class SyntacticAnalysisManager {

    // List of valid argument patterns
    private final static List<String> argPatterns = new ArrayList<>(Arrays.asList(
            "[grup.verb]-[sn]-[S_LNK]",
            "[neg]-[grup.verb]-[sn]-[S_LNK]",
            "[grup.verb]-[sn]-[sp_LNK]",
            "[neg]-[grup.verb]-[sn]-[sp_LNK]",
            "[S]-[conj_LNK]-[S]",
            "[S]-[conj]-[S_LNK]-[S]",
            "[sn]-[grup.verb]-[S_LNK]",
            "[sn]-[neg]-[grup.verb]-[S_LNK]",
            "[sn]-[grup.verb]-[sp_LNK]",
            "[sn]-[grup.verb]-[S]-[S_LNK]",
            "[sn]-[neg]-[grup.verb]-[S]-[S_LNK]",
            "[sn]-[neg]-[grup.verb]-[sp_LNK]",
            "[sn]-[grup.verb]-[sn]-[S_LNK]",
            "[sn]-[neg]-[grup.verb]-[sn]-[S_LNK]",
            "[sn]-[grup.verb]-[sn]-[sp_LNK]",
            "[sn]-[neg]-[grup.verb]-[sn]-[sp_LNK]",
            "[sp]-[grup.verb]-[sn]-[S_LNK]",
            "[sp]-[neg]-[grup.verb]-[sn]-[S_LNK]",
            "[sp]-[grup.verb]-[sn]-[sp_LNK]",
            "[sp]-[neg]-[grup.verb]-[sn]-[sp_LNK]",
            "[S]-[PUNCT]-[S_LNK]"
    ));

    /**
     *
     * @param pattern
     * @return
     */
    public static boolean checkArgumentPattern(String pattern) {
        String sentPattern = pattern.substring(pattern.indexOf('-') + 1);

        for (String argPattern : argPatterns) {
            if (sentPattern.startsWith(argPattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param tree
     * @param parent
     * @param currNode
     * @return
     */
    public static String createSentencePattern(SyntacticTreebank tree, SyntacticTreebankNode parent, SyntacticTreebankNode currNode) {
        String sentPattern = "[" + (parent.getLevel() + 1) + "]";
        String lnkTag;

        try {
            for (Integer childId : tree.getChildrenIdsOf(parent)) {
                SyntacticTreebankNode child = tree.getNode(childId);
                if (child.getId() == currNode.getId() || child.getId() == tree.getParentIdOf(currNode.getId())) {
                    lnkTag = "_LNK";
                } else {
                    lnkTag = "";
                }
                sentPattern += "-[" + child.getTag() + lnkTag + "]";
            }
        } catch (Exception ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sentPattern;
    }

    /**
     *
     * @param tree
     * @param lnkNode
     * @return
     */
    public static String getLinkerNodeText(SyntacticTreebank tree, SyntacticTreebankNode lnkNode) {
        String text = null;

        if (lnkNode.getType() == SyntacticTreebankNode.NODE_WORD) {
            text = lnkNode.getWord();

        } else if (lnkNode.getTag().equals("conj") || lnkNode.getTag().equals("prep")) {
            text = getTreeText(tree, lnkNode);
        }

        return text;
    }

    /**
     *
     * @param tree
     * @param lnkNode
     * @return
     */
    public static SyntacticTreebankNode getLinkerParentNode(SyntacticTreebank tree, SyntacticTreebankNode lnkNode) {
        return getLinkerParentNode(tree, lnkNode, 2);
    }

    /**
     *
     * @param tree
     * @param lnkNode
     * @param level
     * @return
     */
    public static SyntacticTreebankNode getLinkerParentNode(SyntacticTreebank tree, SyntacticTreebankNode lnkNode, int level) {
        SyntacticTreebankNode parent = null;

        try {
            if (lnkNode.getLevel() > 0) {
                int l = 0;
                parent = lnkNode;

                do {
                    parent = tree.getParentNodetOf(parent);
                    l++;
                } while (l < level && parent.getLevel() > 0);
            }
        } catch (Exception ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return parent;
    }

    /**
     *
     * @param tree
     * @param node
     * @return
     */
    public static String getTreeText(SyntacticTreebank tree, SyntacticTreebankNode node) {
        String text = "";
        try {
            text = getTreeInnerText(tree, node);
        } catch (Exception ex) {
            Logger.getLogger(SyntacticAnalysisManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text.trim();
    }

    /**
     *
     * @param tree
     * @param node
     * @return
     * @throws Exception
     */
    private static String getTreeInnerText(SyntacticTreebank tree, SyntacticTreebankNode node) throws Exception {
        String text = "";

        if (node.getType() == SyntacticTreebankNode.NODE_WORD) {
            text = (node.getTag().equals("PUNCT") ? "" : " ") + node.getWord();
        } else {
            for (int nodeId : tree.getChildrenIdsOf(node)) {
                text += getTreeInnerText(tree, tree.getNode(nodeId));
            }
        }

        return text;
    }

}
