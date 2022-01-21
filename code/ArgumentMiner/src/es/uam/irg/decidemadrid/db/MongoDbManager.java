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
package es.uam.irg.decidemadrid.db;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import es.uam.irg.recsys.ArguRecSys;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
        this(DB_SERVER, DB_PORT, DB_NAME, DB_COLLECTION);
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
            this.mongoClient = new MongoClient(client, port);
            this.db = mongoClient.getDatabase(database);
            this.collName = collection;
        } catch (Exception ex) {
            this.mongoClient = null;
            this.db = null;
            this.collName = null;
        }
    }

    /**
     *
     * @param topic
     * @param customProposalIds
     * @return
     */
    public List<Document> getDocumentsByFilter(String topic, Integer[] customProposalIds) {
        List<Document> docs = new ArrayList<>();
        int proposalID;

        try {
            Set<Integer> setIds = new HashSet(Arrays.asList(customProposalIds));

            // Query documents
            MongoCollection<Document> collection = db.getCollection(collName);
            FindIterable<Document> cursor = null;
            if (!topic.equals(ArguRecSys.NO_TOPIC)) {
                cursor = collection.find(Filters.text(topic));
            } else {
                cursor = collection.find();
            }

            for (Iterator<Document> it = cursor.iterator(); it.hasNext();) {
                Document doc = it.next();
                proposalID = (int) doc.get("proposalID");
                if (setIds.isEmpty() || setIds.contains(proposalID)) {
                    docs.add(doc);
                }
            }

        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
