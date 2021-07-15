/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
