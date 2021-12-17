/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import es.uam.irg.nlp.am.Constants;
import org.bson.Document;
import org.json.JSONObject;

/**
 *
 * @author ansegura
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
     * Empty constructor.
     */
    public ArgumentLinker() {
        this("", "", "", "");
    }

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
        this.spLinker = linker.replace(" ", Constants.NGRAMS_DELIMITER);
    }

    /**
     *
     * @param doc
     */
    public ArgumentLinker(Document doc) {
        this.category = doc.getString("category");
        this.subCategory = doc.getString("subCategory");
        this.relationType = doc.getString("relationType");
        this.linker = doc.getString("linker");
        this.nTokens = linker.split(" ").length;
        this.spLinker = linker.replace(" ", Constants.NGRAMS_DELIMITER);
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
     * @return
     */
    @Override
    public String toString() {
        return String.format("%s > %s > [%s] %s (%s)", this.category, this.subCategory, this.nTokens, this.linker, this.relationType);
    }

    /**
     *
     * @param nGram
     * @return
     */
    public boolean isEquals(String nGram) {
        return this.spLinker.equals(nGram);
    }

}
