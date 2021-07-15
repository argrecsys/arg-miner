/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.recsys;

import es.uam.irg.decidemadrid.db.MongoDbManager;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.utils.FunctionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author ansegura
 */
public class ArguRecSys {
    
    private int minAspectOccur;
    private String topic;
    
    /**
     * Class constructor.
     * 
     * @param topic 
     */
    public ArguRecSys(String topic, int minAspectOccur) {
        this.topic = topic;
        this.minAspectOccur = minAspectOccur;
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
            Map<String, Integer> aspects = getFreqAspects(arguments);
            System.out.println(aspects);
            
            // Save arguments
            result = saveRecommendations(arguments, aspects, minAspectOccur);
            if (result) {
                System.out.println(">> Recommendations saved correctly.");
            }
            else {
                System.err.println(">> An unexpected error occurred while saving the recommendations.");
            }
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
    
    /**
     * 
     * @param arguments
     * @return 
     */
    private Map<String, Integer> getFreqAspects(List<Argument> arguments) {
        Map<String, Integer> aspects = new HashMap<>();
        List<String> listAspects = new ArrayList<>();
        int count;
        
        for (Argument argument : arguments) {
            listAspects.addAll(argument.getNounsSet());
        }
        
        for (String value : listAspects) {
            count = aspects.containsKey(value) ? aspects.get(value) : 0;
            aspects.put(value, count + 1);
        }
        
        return FunctionUtils.sortMapByValue(aspects);
    }
    
    /**
     * 
     * @param arguments
     * @param aspects
     * @param minAspectOccur
     * @return 
     */
    private boolean saveRecommendations(List<Argument> arguments, Map<String, Integer> aspects, int minAspectOccur) {
        boolean result = false;
        
        // Local variables
        Map<String, List<Argument>> recommendations = new HashMap<>();
        String aspect;
        List<String> argUsed = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            aspect = entry.getKey();

            if (!aspect.equals(this.topic)) {
                for (Argument argument : arguments) {
                    if (!argUsed.contains(argument.sentenceID)) {
                        if (argument.getNounsSet().contains(aspect)) {
                            List<Argument> arguList = recommendations.containsKey(aspect) ? recommendations.get(aspect) : new ArrayList<>();
                            arguList.add(argument);
                            recommendations.put(aspect, arguList);
                            argUsed.add(argument.sentenceID);
                        }
                    }
                }
            }
        }
        
        System.out.println(argUsed);
        for (Map.Entry<String, List<Argument>> recommendation : recommendations.entrySet()) {
            if (recommendation.getValue().size() > minAspectOccur) {
                System.out.println(recommendation.getKey() + ", " + recommendation.getValue().size());
            }
        }
        System.out.println(recommendations);
        
        return result;
    }
}
