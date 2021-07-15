/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.recsys;

import es.uam.irg.decidemadrid.db.MongoDbManager;
import es.uam.irg.nlp.am.arguments.Argument;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author ansegura
 */
public class ArguRecSys {
    
    private String topic;
    
    /**
     * Class constructor.
     * 
     * @param topic 
     */
    public ArguRecSys(String topic) {
        this.topic = topic;
    }
    
    /**
     * 
     * @return 
     */
    public boolean runRecSys() {
        boolean result = false;
        
        List<Argument> arguments = getArgumentsByTopic();
        System.out.println(">> Total arguments: " + arguments.size());
        
        if (!arguments.isEmpty()) {
            result = true;
        }
        
        return result;
    }
    
    /**
     * 
     * @return 
     */
    private List<Argument> getArgumentsByTopic() {
        List<Argument> arguments = new ArrayList<>();
        
        MongoDbManager manager = new MongoDbManager();
        List<Document> docs = manager.getDocumentsByTopic(this.topic);
        
        for (Document doc : docs) {
            arguments.add( new Argument(doc));
        }
        
        return arguments;
    }
}
