/**
 * Copyright 2017
 * Ivan Cantador
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
package es.uam.irg.nlp.corenlp.syntax.treebank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import es.uam.irg.nlp.corenlp.syntax.phrase.PhraseTags;
import es.uam.irg.nlp.corenlp.syntax.pos.POSTags;
import java.util.Collections;

/**
 * SyntacticTreebank
 *
 * Stores a treebank obtained from the syntactic analysis of a sentence.
 *
 * @author Ivan Cantador, ivan.cantador@uam.es
 * @version 1.0 - 16/03/2017
 */
public class SyntacticTreebank {

    private Map<Integer, SyntacticTreebankNode> nodes;
    private List<Integer> parents;

    public SyntacticTreebank(String pennTreebankDescripion, boolean compactFormat) throws Exception {
        this.nodes = new HashMap<>();
        this.parents = new ArrayList<>();

        String s = pennTreebankDescripion.replace("\n", " ").replace("\t", " ");
        while (s.contains("  ")) {
            s = s.replace("  ", " ");
        }
        s = s.trim();

        this.process(s, 0, -1);
        if (compactFormat) {
            this.compact(this.getRootNode());
            this.updateLevels(this.getRootNode());
        }
    }

    public void process(String s, int level, int parentId) throws Exception {
        s = s.trim();
        s = s.substring(1, s.length() - 1); // removing first and last parenthesis

        int index = s.indexOf("(");
        if (index < 0) {    // word
            index = s.indexOf(" ");
            String posTag = s.substring(0, index);
            String word = s.substring(index + 1);
            int nodeId = this.nextNodeId();
            SyntacticTreebankNode node = new SyntacticTreebankNode(nodeId, level, posTag, word);
            this.addNode(node, parentId);
        } else {            // phrase
            index = s.indexOf(" ");
            String phraseTag = s.substring(0, index);
            int nodeId = this.nextNodeId();
            SyntacticTreebankNode node = new SyntacticTreebankNode(nodeId, level, phraseTag);
            this.addNode(node, parentId);
            s = s.substring(index);

            index = s.indexOf("(");
            s = s.substring(index);

            int balance = 0;
            String token = "";
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '(') {
                    token += c;
                    balance++;
                } else if (c == ')') {
                    token += c;
                    balance--;
                    if (balance == 0) {
                        this.process(token, level + 1, nodeId);
                        token = "";
                    }
                } else {
                    token += c;
                }
            }
        }
    }

    public int nextNodeId() {
        return this.getNumNodes();
    }

    public Map<Integer, SyntacticTreebankNode> getNodes() {
        return this.nodes;
    }

    public SyntacticTreebankNode getRootNode() {
        if (this.nodes.containsKey(0)) {
            return this.nodes.get(0);
        }
        return null;
    }

    public int getNumNodes() {
        return this.nodes.size();
    }

    public int addNode(SyntacticTreebankNode node, int parentId) throws Exception {
        if (node == null) {
            throw new IllegalArgumentException("Null node");
        }
        if (parentId < -1 || parentId >= this.getNumNodes() || (this.getNumNodes() == 0 && parentId != -1)) {
            throw new IllegalArgumentException("Non valid parent node " + parentId + " (number of  nodes = " + this.getNumNodes() + ")");
        }

        int nodeId = this.nodes.size();
        this.nodes.put(nodeId, node);
        this.parents.add(parentId);

        return nodeId;
    }

    public SyntacticTreebankNode getNode(int nodeId) {
        if (nodeId < 0 || nodeId >= this.getNumNodes()) {
            throw new IllegalArgumentException("Non valid node " + nodeId + " (number of  nodes = " + this.getNumNodes() + ")");
        }
        return this.nodes.get(nodeId);
    }

    public int getParentIdOf(int nodeId) {
        if (nodeId < 0 || nodeId >= this.getNumNodes()) {
            throw new IllegalArgumentException("Non valid node " + nodeId + " (number of  nodes = " + this.getNumNodes() + ")");
        }
        int parentId = this.parents.get(nodeId);
        return parentId;
    }

    public int getParentIdOf(SyntacticTreebankNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Null node");
        }
        return this.getParentIdOf(node.getId());
    }

    public SyntacticTreebankNode getParentNodetOf(int nodeId) throws Exception {
        int parentId = this.getParentIdOf(nodeId);
        return this.getNode(parentId);
    }

    public SyntacticTreebankNode getParentNodetOf(SyntacticTreebankNode node) throws Exception {
        if (node == null) {
            throw new IllegalArgumentException("Null node");
        }
        return this.getParentNodetOf(node.getId());
    }

    public List<Integer> getChildrenIdsOf(int nodeId) throws Exception {
        if (nodeId < 0 || nodeId >= this.getNumNodes()) {
            throw new IllegalArgumentException("Non valid node " + nodeId + " (number of  nodes = " + this.getNumNodes() + ")");
        }
        List<Integer> childrenIds = new ArrayList<>();
        for (int i = 0; i < this.parents.size(); i++) {
            if (this.parents.get(i) == nodeId) {
                childrenIds.add(i);
            }
        }
        Collections.sort(childrenIds);
        return childrenIds;
    }

    public List<Integer> getChildrenIdsOf(SyntacticTreebankNode node) throws Exception {
        if (node == null) {
            throw new IllegalArgumentException("Null node");
        }
        return this.getChildrenIdsOf(node.getId());
    }

    public List<SyntacticTreebankNode> getChildrenNodesOf(int nodeId) throws Exception {
        List<Integer> childrenIds = this.getChildrenIdsOf(nodeId);
        List<SyntacticTreebankNode> childrenNodes = new ArrayList<>();
        for (int childId : childrenIds) {
            childrenNodes.add(this.getNode(childId));
        }
        return childrenNodes;
    }

    public List<SyntacticTreebankNode> getChildrenNodesOf(SyntacticTreebankNode node) throws Exception {
        if (node == null) {
            throw new IllegalArgumentException("Null node");
        }
        return this.getChildrenNodesOf(node.getId());
    }

    private void compact(SyntacticTreebankNode node) throws Exception {
        String nodeTag = node.getTag();
        if (nodeTag != null && POSTags.isValidTag(nodeTag)) {
            String nodeType = POSTags.getTypeOfTag(nodeTag);
            if (nodeType == null || nodeType.equals(POSTags.TYPE_PUNCTUATION)) {
                this.parents.set(node.getId(), -2);
            }
        }

        boolean hasBeenCompacted = false;
        for (SyntacticTreebankNode child : this.getChildrenNodesOf(node)) {
            if (PhraseTags.compactedTags(node.getTag(), child.getTag())) {
                int nodeId = node.getId();
                for (SyntacticTreebankNode grandChild : this.getChildrenNodesOf(child)) {
                    int grandChildId = grandChild.getId();
                    this.parents.set(grandChildId, nodeId);
                }
                this.parents.set(child.getId(), -3);
                hasBeenCompacted = true;
            }
        }
        if (hasBeenCompacted) {
            this.compact(node);
        }
        for (SyntacticTreebankNode child : this.getChildrenNodesOf(node)) {
            this.compact(child);
        }
    }

    private void updateLevels(SyntacticTreebankNode node) throws Exception {
        for (SyntacticTreebankNode child : this.getChildrenNodesOf(node)) {
            child.setLevel(node.getLevel() + 1);
            this.updateLevels(child);
        }
    }

    public void removeNode(int nodeId) throws Exception {
        if (nodeId <= 0) {
            return;
        }

        int parentId = this.parents.get(nodeId);
        List<Integer> childrenIds = this.getChildrenIdsOf(nodeId);
        for (int childId : childrenIds) {
            this.parents.set(childId, parentId);
        }
        this.parents.set(nodeId, -4);
        this.updateLevels(this.getRootNode());
    }

    public void removeNode(SyntacticTreebankNode node) throws Exception {
        if (node == null) {
            throw new IllegalArgumentException("Null node");
        }
        this.removeNode(node.getId());
    }

    @Override
    public String toString() {
        try {
            if (this.getNumNodes() > 0) {
                return this._toString(0);
            }
        } catch (Exception e) {
        }

        return null;
    }

    private String _toString(int nodeId) throws Exception {
        SyntacticTreebankNode node = this.getNode(nodeId);
        int nodeLevel = node.getLevel();
        // int nodeType = node.getType();
        String nodeTag = node.getTag();
        String nodeWord = node.getWord();

        String s = "";
        for (int i = 0; i < nodeLevel; i++) {
            s += "\t";
        }
        s += nodeId + ":" + nodeTag + (nodeWord != null ? "/" + nodeWord : "") + "\n";

        for (int childId : this.getChildrenIdsOf(nodeId)) {
            s += this._toString(childId);
        }

        return s;
    }
}
