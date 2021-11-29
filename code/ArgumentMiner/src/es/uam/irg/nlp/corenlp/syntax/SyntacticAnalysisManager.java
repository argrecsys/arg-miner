/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.corenlp.syntax;

import es.uam.irg.nlp.am.arguments.ArgumentEngine;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebank;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebankNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class SyntacticAnalysisManager {
    
    /**
     *
     * @param tree
     * @param lnkNode
     * @param level
     * @return
     */
    public static SyntacticTreebankNode getLinkerParent(SyntacticTreebank tree, SyntacticTreebankNode lnkNode, int level) {
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
            text = getTreeFullText(tree, node);
        } catch (Exception ex) {
            Logger.getLogger(SyntacticAnalysisManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text.trim();
    }
    
    private static String getTreeFullText(SyntacticTreebank tree, SyntacticTreebankNode node) throws Exception {
        String text = "";
        
        if (node.getType() == 2) {
            text = node.getWord() + " ";
        }
        else {
            for (int nodeId : tree.getChildrenIdsOf(node)) {
                text += getTreeFullText(tree, tree.getNode(nodeId));
            }
        }
        
        return text;
    }
    
}
