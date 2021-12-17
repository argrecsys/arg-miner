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
package es.uam.irg.nlp.corenlp.syntax.pos;

/**
 * TaggedWord
 *
 * Stores a pair [word, POS tag] obtained from a sentence syntactic analysis.
 *
 * @author Ivan Cantador, ivan.cantador@uam.es
 * @version 1.0 - 16/03/2017
 */
public class TaggedWord {

    private String word;
    private String tag;

    public TaggedWord(String word, String tag) throws Exception {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Null or empty word");
        }
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Null or empty tag");
        }

        if (!POSTags.isValidTag(tag)) {
            throw new IllegalArgumentException("Non valid tag");
        }

        this.word = word.trim();
        this.tag = tag.trim();
    }

    public String getWord() {
        return this.word;
    }

    public String getTag() {
        return this.tag;
    }

    public String toString() {
        return this.getTag() + "/" + this.getWord();
    }
}
