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
     * @param filter
     * @param doc
     * @param options
     * @return 
     */
    public boolean upsertDocument(String collName, Bson filter, Document doc, UpdateOptions options) {
        boolean result = false;
        
        try {
            MongoCollection<Document> collAnnotations = db.getCollection(collName);
            Bson update =  new Document("$set", doc);
            collAnnotations.updateOne(filter, update, options);
            result = true;
        }
        catch (Exception ex) {
            System.err.println("MongoDB error: " + ex.getMessage());
        }
        
        return result;
    }
    
}
