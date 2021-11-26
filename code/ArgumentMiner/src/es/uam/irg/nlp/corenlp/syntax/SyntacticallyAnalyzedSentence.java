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
package es.uam.irg.nlp.corenlp.syntax;

import es.uam.irg.nlp.corenlp.syntax.phrase.PhraseTags;
import es.uam.irg.nlp.corenlp.syntax.pos.POSTags;
import es.uam.irg.nlp.corenlp.syntax.pos.TaggedWord;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebank;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebankNode;
import java.util.ArrayList;
import java.util.List;

/**
 * SyntacticallyAnalyzedSentence
 *
 * Performs a syntactic analysis on a given sentence, storing its syntactic treebank, 
 * and its nouns and their complements and dependences.
 *
 * @author Ivan Cantador, ivan.cantador@uam.es
 * @version 1.0 - 16/03/2017
 */
public class SyntacticallyAnalyzedSentence {

    private String sentence;
    private SyntacticTreebank treebank;
    private SyntacticAnalysisData data;

    public SyntacticallyAnalyzedSentence(String sentence, SyntacticTreebank treebank) throws Exception {
        if (sentence == null || sentence.isEmpty()) {
            throw new IllegalArgumentException("Null or empty sentence");
        }
        if (treebank == null) {
            throw new IllegalArgumentException("Null treebank");
        }

        this.sentence = sentence;
        this.treebank = treebank;

        this.data = new SyntacticAnalysisData();
        this.generateAnalysisData(this.treebank.getRootNode(), -1);
    }

    public String getSentence() {
        return this.sentence;
    }

    public SyntacticTreebank getTreebank() {
        return this.treebank;
    }

    public SyntacticAnalysisData getAnalysisData() throws Exception {
        return this.data;
    }

    private void generateAnalysisData(SyntacticTreebankNode node, int parentId) throws Exception {
        List<SyntacticTreebankNode> children = this.treebank.getChildrenNodesOf(node);

        TaggedWord noun = null;
        int dependenceId = -1;
        int nounId = -1;
        List<TaggedWord> complements = new ArrayList<TaggedWord>();

        boolean foundConnect = false;
        for (SyntacticTreebankNode child : children) {
            String childWord = child.getWord();
            String childTag = child.getTag();
            String childType = POSTags.getTypeOfTag(childTag);

            if (childType != null) {
                if (childType.equals(POSTags.TYPE_NOUN)) {
                    if (noun != null) {
                        String _nounType = noun.getTag().equals(POSTags.NNP) || noun.getTag().equals(POSTags.NNPS) ? "NNP" : "NN";
                        String _childType = childTag.equals(POSTags.NNP) || childTag.equals(POSTags.NNPS) ? "NNP" : "NN";
                        if (!_nounType.equals(_childType)) {
                            nounId = this.data.addNoun(noun);
                            this.data.setLevel(nounId, node.getLevel());
                            for (TaggedWord complement : complements) {
                                this.data.addComplement(nounId, complement);
                            }
                            if (parentId != -1) {
                                this.data.addDependence(nounId, parentId);
                            }
                            if (foundConnect) {
                                parentId = -1;
                                foundConnect = false;
                            }
                            dependenceId = nounId;
                            noun = null;
                            complements = new ArrayList<TaggedWord>();
                        }
                    }

                    String word = noun == null ? childWord : noun.getWord() + " " + childWord;
                    String tag = childTag;
                    noun = new TaggedWord(word, tag);
                } else if (childType.equals(POSTags.TYPE_DETERMINER) || childType.equals(POSTags.TYPE_ADJECTIVE) || childType.equals(POSTags.TYPE_ADVERB)) {
                    String word = childWord;
                    String tag = childTag;
                    complements.add(new TaggedWord(word, tag));
                } else if (childType.equals(POSTags.TYPE_GENITIVE)) {
                    nounId = this.data.addNoun(noun);
                    this.data.setLevel(nounId, node.getLevel());
                    for (TaggedWord complement : complements) {
                        this.data.addComplement(nounId, complement);
                    }
                    if (parentId != -1) {
                        this.data.addDependence(nounId, parentId);
                    }
                    parentId = nounId;
                    noun = null;
                    complements = new ArrayList<TaggedWord>();
                } else if (childType.equals(POSTags.TYPE_CONNECT)) {
                    if (noun != null) {
                        foundConnect = true;
                    }
                }
            } else {
                if (noun != null) {
                    nounId = this.data.addNoun(noun);
                    this.data.setLevel(nounId, node.getLevel());
                    for (TaggedWord complement : complements) {
                        this.data.addComplement(nounId, complement);
                    }
                    if (parentId != -1) {
                        this.data.addDependence(nounId, parentId);
                    }
                    noun = null;
                    dependenceId = nounId;
                    complements = new ArrayList<TaggedWord>();
                }

                childType = PhraseTags.getTypeOfTag(childTag);
                if (childType != null && childType.equals(PhraseTags.TYPE_PREPOSITIONAL_PHRASE)) {
                    this.generateAnalysisData(child, dependenceId);
                } else {
                    this.generateAnalysisData(child, -1);
                }
            }
        }

        if (noun != null) {
            nounId = this.data.addNoun(noun);
            this.data.setLevel(nounId, node.getLevel());
            for (TaggedWord complement : complements) {
                this.data.addComplement(nounId, complement);
            }
            if (parentId != -1) {
                this.data.addDependence(nounId, parentId);
            }
        } else {
            if (!this.data.getNouns().isEmpty()) {
                if (!complements.isEmpty()) {
                    for (TaggedWord complement : complements) {
                        this.data.addComplement(0, complement);
                    }
                }
            }
        }
    }

    public String toString() {
        return this.getSentence();
    }
}
