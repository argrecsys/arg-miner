/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.recsys;

import es.uam.irg.decidemadrid.db.MongoDbManager;
import es.uam.irg.nlp.am.Constants;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.utils.FunctionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import org.w3c.dom.DOMException;

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
            Map<String, List<Argument>> recommendations = getRecommendations(arguments, aspects, minAspectOccur);
            System.out.println(">> Total recommended topics: " + recommendations.size());
            
            result = saveRecommendations(topic, recommendations);
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
        Set<String> nouns;
        int count;
        
        for (Argument argument : arguments) {
            nouns = argument.getNounsSet();
            listAspects.addAll(nouns);
            System.out.println(argument.sentenceID + ": " + nouns.toString());
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
    private Map<String, List<Argument>> getRecommendations(List<Argument> arguments, Map<String, Integer> aspects, int minAspectOccur) {
        Map<String, List<Argument>> result = new HashMap<>();
        Map<String, List<Argument>> recommendations = new HashMap<>();
        
        // Local variables
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
            if (recommendation.getValue().size() >= minAspectOccur) {
                result.put(recommendation.getKey(), recommendation.getValue());
                System.out.println(recommendation.getKey() + ", " + recommendation.getValue().size());
            }
        }
        
        return result;
    }

    /**
     * 
     * @param recommendations
     * @return 
     */
    private boolean saveRecommendations(String topic, Map<String, List<Argument>> recommendations) {
        boolean result = false;
        String filename = Constants.RECOMMENDATIONS_FILEPATH.replace("{}", topic);
        
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.newDocument();

            // Toot element
            Element rootElement = doc.createElement("recommendations");
            doc.appendChild(rootElement);
            
            // Topic element
            Element nTopic = doc.createElement("topic");
            Attr attr = doc.createAttribute("value");
            attr.setValue(topic);
            nTopic.setAttributeNode(attr);
            rootElement.appendChild(nTopic);
            
            for (Map.Entry<String, List<Argument>> entry : recommendations.entrySet()) {
                
                // Topic element
                Element nAspect = doc.createElement("aspect");
                Attr nAttr = doc.createAttribute("value");
                nAttr.setValue(entry.getKey());
                nAspect.setAttributeNode(nAttr);
                nTopic.appendChild(nAspect);
                
                for (Argument argument : entry.getValue()) {
                    
                    // Argument element
                    Element nArgu = doc.createElement("argument");
                    Attr attrType = doc.createAttribute("id");
                    attrType.setValue(argument.sentenceID);
                    nArgu.setAttributeNode(attrType);
                    nAspect.appendChild(nArgu);
                    
                    Element nClaim = doc.createElement("claim");
                    nClaim.appendChild(doc.createTextNode(argument.claim.text));
                    nArgu.appendChild(nClaim);
                    
                    Element nLinker = doc.createElement("connector");
                    nLinker.appendChild(doc.createTextNode(argument.linker.linker));
                    nArgu.appendChild(nLinker);
                    
                    Attr lnkCategory = doc.createAttribute("category");
                    lnkCategory.setValue(argument.linker.category.toLowerCase());
                    nLinker.setAttributeNode(lnkCategory);
                    
                    Attr lnkSubcategory = doc.createAttribute("subcategory");
                    lnkSubcategory.setValue(argument.linker.subCategory.toLowerCase());
                    nLinker.setAttributeNode(lnkSubcategory);
                    
                    Attr lnkFunction = doc.createAttribute("function");
                    lnkFunction.setValue(argument.linker.relationType);
                    nLinker.setAttributeNode(lnkFunction);
                    
                    Element nPremise = doc.createElement("premise");
                    nPremise.appendChild(doc.createTextNode(argument.premise.text));
                    nArgu.appendChild(nPremise);
                }
            }

            // Write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult stream = new StreamResult(new File(filename));
            transformer.transform(source, stream);
            result = true;
            
        } catch (ParserConfigurationException | TransformerException | DOMException e) {
            System.err.println(e.getMessage());
        }
        
        return result;
    }
    
}
