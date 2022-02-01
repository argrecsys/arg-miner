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

import org.bson.Document;
import org.json.JSONObject;

/**
 * Syntactic argument pattern class.
 */
public class ArgumentPattern {

    // Class members
    private final int depth;
    private final String fullPattern;
    private final String pattern;

    /**
     * Empty constructor.
     */
    public ArgumentPattern() {
        this.pattern = "";
        this.depth = 0;
        this.fullPattern = "";
    }

    /**
     * Regular constructor.
     *
     * @param pattern
     * @param depth
     */
    public ArgumentPattern(String pattern, int depth) {
        this.pattern = pattern;
        this.depth = depth;
        this.fullPattern = "[" + depth + "]-" + pattern;
    }

    /**
     * Alternative constructor.
     *
     * @param doc
     */
    public ArgumentPattern(Document doc) {
        this.pattern = doc.getString("pattern");
        this.depth = doc.getInteger("depth");
        this.fullPattern = "[" + depth + "]-" + pattern;
    }

    /**
     *
     * @param argPattern
     * @return
     */
    public boolean equals(ArgumentPattern argPattern) {
        return this.fullPattern.equals(argPattern.fullPattern);
    }

    /**
     *
     * @return
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     *
     * @return
     */
    public Document getDocument() {
        Document doc = new Document();
        doc.append("pattern", this.pattern)
                .append("depth", this.depth);

        return doc;
    }

    /**
     *
     * @return
     */
    public String getFullPattern() {
        return this.fullPattern;
    }

    /**
     *
     * @return
     */
    public JSONObject getJSON() {
        JSONObject json = new JSONObject();
        json.put("pattern", this.pattern);
        json.put("depth", this.depth);

        return json;
    }

    /**
     *
     * @return
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return this.fullPattern;
    }

}
