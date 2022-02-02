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
    private final String value;

    /**
     * Empty constructor.
     */
    public ArgumentPattern() {
        this.value = "";
        this.depth = 0;
        this.fullPattern = "";
    }

    /**
     * Regular constructor.
     *
     * @param value
     * @param depth
     */
    public ArgumentPattern(String value, int depth) {
        this.value = value;
        this.depth = depth;
        this.fullPattern = "[" + depth + "]-" + value;
    }

    /**
     * Alternative constructor.
     *
     * @param doc
     */
    public ArgumentPattern(Document doc) {
        this.value = doc.getString("value");
        this.depth = doc.getInteger("depth");
        this.fullPattern = "[" + depth + "]-" + value;
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
        doc.append("value", this.value)
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
        json.put("value", this.value);
        json.put("depth", this.depth);

        return json;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return this.value;
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
