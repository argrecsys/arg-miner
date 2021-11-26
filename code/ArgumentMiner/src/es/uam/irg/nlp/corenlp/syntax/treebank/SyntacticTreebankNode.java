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

/**
 * SyntacticTreebank
 *
 * Stores a node of a treebank obtained from the syntactic analysis of a
 * sentence.
 *
 * @author Ivan Cantador, ivan.cantador@uam.es
 * @version 1.0 - 16/03/2017
 */
public class SyntacticTreebankNode {

    public static final int NODE_PHRASE = 1;
    public static final int NODE_WORD = 2;
    private int type;
    private int id;
    private int level;
    private String tag;
    private String word;

    public SyntacticTreebankNode(int id, int level, String posTag, String word) throws Exception {
        this.id = id;
        this.level = level;
        this.type = NODE_WORD;
        this.tag = posTag.trim();
        this.word = word.trim();
    }

    public SyntacticTreebankNode(int id, int level, String phraseTag) throws Exception {
        this.id = id;
        this.level = level;
        this.type = NODE_PHRASE;
        this.tag = phraseTag.trim();
        this.word = null;
    }

    public int getId() {
        return this.id;
    }

    public int getLevel() {
        return this.level;
    }

    public String getTag() {
        return this.tag;
    }

    public String getWord() {
        return this.word;
    }

    public int getType() {
        return this.type;
    }

    void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "{" + "id=" + id + ", level=" + level + ", tag=" + tag + ", word=" + (word == null ? "" : word) + ", type=" + type + "}";
    }

}
