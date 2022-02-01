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
package es.uam.irg.nlp.am.arguments;

import org.bson.Document;
import org.json.JSONObject;

/**
 * Argument linker class.
 */
public class ArgumentLinker {

    // Class members
    public String category;
    public String linker;
    public int nTokens;
    public String relationType;
    public String subCategory;
    private String spLinker;

    /**
     * Regular constructor.
     *
     * @param category
     * @param subCategory
     * @param relationType
     * @param linker
     */
    public ArgumentLinker(String category, String subCategory, String relationType, String linker) {
        this.category = category;
        this.subCategory = subCategory;
        this.relationType = relationType;
        this.linker = linker;
        this.nTokens = linker.split(" ").length;
        this.spLinker = linker.replace(" ", TreeAnalyzer.NGRAMS_DELIMITER);
    }

    /**
     * Alternative constructor.
     *
     * @param doc
     */
    public ArgumentLinker(Document doc) {
        this.category = doc.getString("category");
        this.subCategory = doc.getString("subCategory");
        this.relationType = doc.getString("relationType");
        this.linker = doc.getString("linker");
        this.nTokens = linker.split(" ").length;
        this.spLinker = linker.replace(" ", TreeAnalyzer.NGRAMS_DELIMITER);
    }

    /**
     *
     * @param lnk
     * @return
     */
    public boolean equals(ArgumentLinker lnk) {
        return this.linker.equals(lnk.linker);
    }

    /**
     *
     * @return
     */
    public Document getDocument() {
        Document doc = new Document();
        doc.append("linker", this.linker)
                .append("category", this.category)
                .append("subCategory", this.subCategory)
                .append("relationType", this.relationType);

        return doc;
    }

    /**
     *
     * @return
     */
    public JSONObject getJSON() {
        JSONObject json = new JSONObject();
        json.put("linker", this.linker);
        json.put("category", this.category);
        json.put("subCategory", this.subCategory);
        json.put("relationType", this.relationType);

        return json;
    }

    /**
     *
     * @param nGram
     * @return
     */
    public boolean isEquals(String nGram) {
        return this.spLinker.equals(nGram);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("%s > %s > [%s] %s (%s)", this.category, this.subCategory, this.nTokens, this.linker, this.relationType);
    }

}
