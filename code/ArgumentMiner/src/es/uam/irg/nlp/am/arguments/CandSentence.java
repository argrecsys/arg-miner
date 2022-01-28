/**
 * Copyright 2022
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

/**
 * Candidate sentence container class.
 */
public class CandSentence {

    private boolean simple;
    private String text;

    public CandSentence() {
        this("", true);
    }

    public CandSentence(String text, boolean simple) {
        this.text = text;
        this.simple = simple;
    }

    public String getText() {
        return this.text;
    }

    public boolean isSimple() {
        return this.simple;
    }
}
