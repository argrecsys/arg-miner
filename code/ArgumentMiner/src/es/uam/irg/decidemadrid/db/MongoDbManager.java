/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.decidemadrid.db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author ansegura
 */
public class MongoDbManager {
    
    // Public constants
    public static final String DB_NAME = "decide_madrid_2019_09";
    public static final int DB_PORT = 27017; 
    public static final String DB_SERVER = "localhost";
    public static final String DB_COLLECTION = "annotations";
    
    // Private connector object
    private MongoDatabase db;
    private MongoClient mongoClient;
    private String collName;
    
    /**
     * Manager constructor.
     */
    public MongoDbManager() {
        this(DB_SERVER , DB_PORT, DB_NAME, DB_COLLECTION);
    }
    
    /**
     * 
     * @param client
     * @param port
     * @param database 
     * @param collection 
     */
    public MongoDbManager(String client, int port, String database, String collection) {
        try {
            this.mongoClient = new MongoClient(client , port);
            this.db = mongoClient.getDatabase(database);
            this.collName = collection;
        }
        catch (Exception ex) {
            this.mongoClient = null;
            this.db = null;
            this.collName = null;
        }
    }
    
    /**
     *
     * @param topic
     * @return
     */
    public List<Document> getDocumentsByTopic(String topic) {
        List<Document> docs = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = db.getCollection(collName);
            
            for (Document doc : collection.find(Filters.text(topic))) {
                docs.add(doc);
            }
        }
        catch (Exception ex) {
            System.err.println("MongoDB error: " + ex.getMessage());
        }
        
        return docs;
    }
    
    /**
     * 
     * @param doc
     * @param filter
     * @param options
     * @return 
     */
    public boolean upsertDocument(Document doc, Bson filter, UpdateOptions options) {
        boolean result = false;
        
        try {
            MongoCollection<Document> collection = db.getCollection(collName);
            Bson update = new Document("$set", doc);
            collection.updateOne(filter, update, options);
            result = true;
        }
        catch (Exception ex) {
            System.err.println("MongoDB error: " + ex.getMessage());
        }
        
        return result;
    }
    
    /**
     * 
     * @param docs
     * @param filters
     * @param options
     * @return 
     */
    public boolean upsertDocuments(List<Document> docs, List<Bson> filters, UpdateOptions options) {
        boolean result = true;
        
        if (docs.size() == filters.size()) {
            for (int i = 0; i < docs.size(); i++) {
                Document doc = docs.get(i);
                Bson filter = filters.get(i);
                result &= upsertDocument(doc, filter, options);
            }
        }
        
        return result;
    }
    
}
