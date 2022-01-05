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

import es.uam.irg.utils.FunctionUtils;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.json.JSONObject;

/**
 *
 * @author ansegura
 */
public class Sentence {

    public List<String> entities;
    public List<String> nouns;
    public String text;

    public Sentence() {
        this("", new ArrayList<>(), new ArrayList<>());
    }

    public Sentence(String text, List<String> nouns, List<String> entities) {
        this.text = text;
        this.nouns = nouns;
        this.entities = entities;
    }

    public Sentence(Document doc) {
        this.text = doc.getString("text");
        this.nouns = FunctionUtils.createListFromText(doc.getString("nouns"));
        this.entities = FunctionUtils.createListFromText(doc.getString("entities"));
    }

    public Document getDocument() {
        Document doc = new Document();
        doc.append("text", this.text)
                .append("nouns", this.nouns.toString())
                .append("entities", this.entities.toString());

        return doc;
    }

    public JSONObject getJSON() {
        JSONObject json = new JSONObject();
        json.put("text", this.text);
        json.put("nouns", this.nouns.toString());
        json.put("entities", this.entities.toString());

        return json;
    }

}
