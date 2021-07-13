/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.decidemadrid.db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author ansegura
 */
public class MongoDbManager {
    
    private MongoDatabase db;
    private MongoClient mongoClient;
    
    // Manager constructor
    public MongoDbManager() {
        try {
            this.mongoClient = new MongoClient("localhost" , 27017);
            this.db = mongoClient.getDatabase("decide_madrid_2019_09");
        }
        catch (Exception ex) {
            this.mongoClient = null;
            this.db = null;
        }
    }
    
    /**
     * 
     * @param collName
     * @param doc
     * @param filter
     * @param options
     * @return 
     */
    public boolean upsertDocument(String collName, Document doc, Bson filter, UpdateOptions options) {
        boolean result = false;
        
        try {
            MongoCollection<Document> collAnnotations = db.getCollection(collName);
            Bson update = new Document("$set", doc);
            collAnnotations.updateOne(filter, update, options);
            result = true;
        }
        catch (Exception ex) {
            System.err.println("MongoDB error: " + ex.getMessage());
        }
        
        return result;
    }
    
    /**
     * 
     * @param collName
     * @param docs
     * @param filters
     * @param options
     * @return 
     */
    public boolean upsertDocuments(String collName, List<Document> docs, List<Bson> filters, UpdateOptions options) {
        boolean result = true;
        
        if (docs.size() == filters.size()) {
            for (int i=0; i < docs.size(); i++) {
                Document doc = docs.get(i);
                Bson filter = filters.get(i);
                result &= upsertDocument(collName, doc, filter, options);
            }
        }
        
        return result;
    }
}
