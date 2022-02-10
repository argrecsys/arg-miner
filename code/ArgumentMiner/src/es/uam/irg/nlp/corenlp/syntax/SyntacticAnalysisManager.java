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

import es.uam.irg.nlp.am.arguments.ArgumentPattern;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebank;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebankNode;
import es.uam.irg.utils.StringUtils;
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

    public static final String PUNCT_MARKS = ",...!?)/:;";

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

        if (!StringUtils.isEmpty(pattern)) {
            for (String argPattern : argPatterns) {
                if (pattern.startsWith(argPattern)) {
                    return true;
                }
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
    public static ArgumentPattern createSentencePattern(SyntacticTreebank tree, SyntacticTreebankNode parent, SyntacticTreebankNode currNode) {
        ArgumentPattern sentPattern = new ArgumentPattern();

        try {
            String pattern = "";
            String lnkTag;

            for (int childId : tree.getChildrenIdsOf(parent)) {
                SyntacticTreebankNode child = tree.getNode(childId);
                if (child.getId() == currNode.getId() || child.getId() == tree.getParentIdOf(currNode.getId())) {
                    lnkTag = "_LNK";
                } else {
                    lnkTag = "";
                }
                pattern += "[" + child.getTag() + lnkTag + "]-";
            }
            pattern = pattern.substring(0, pattern.length() - 1);
            sentPattern = new ArgumentPattern(pattern, (parent.getLevel() + 1));

        } catch (Exception ex) {
            Logger.getLogger(SyntacticAnalysisManager.class.getName()).log(Level.SEVERE, null, ex);
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
     * @param lnkNode
     * @param level
     * @return
     */
    private static SyntacticTreebankNode getLinkerParentNode(SyntacticTreebank tree, SyntacticTreebankNode lnkNode, int level) {
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
            Logger.getLogger(SyntacticAnalysisManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return parent;
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
            if (node.getTag().equals("PUNCT")) {
                text = translatePunctuationMark(node.getWord());
            } else {
                text = node.getWord();
            }
        } else {
            String token = "";
            String lastToken = "";
            for (int childId : tree.getChildrenIdsOf(node.getId())) {
                token = getTreeInnerText(tree, tree.getNode(childId));
                text += translateTextSpaces(token, lastToken);
                lastToken = token;
            }
        }

        return text;
    }

    /**
     *
     * @param mark
     * @return
     */
    private static String translatePunctuationMark(String mark) {
        String newMark;
        newMark = switch (mark) {
            case "-LRB-" ->
                "(";
            case "-RRB-" ->
                ")";
            default ->
                mark;
        };
        return newMark;
    }

    /**
     *
     * @param currToken
     * @param lastToken
     * @return
     */
    private static String translateTextSpaces(String currToken, String lastToken) {
        String text = "";

        if (!StringUtils.isEmpty(currToken)) {
            if (StringUtils.isEmpty(lastToken) || lastToken.charAt(0) == '(' || lastToken.charAt(0) == '/' || PUNCT_MARKS.contains("" + currToken.charAt(0))) {
                text = currToken;
            } else {
                text = " " + currToken;
            }
        }

        return text;
    }

}
