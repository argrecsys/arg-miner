/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import es.uam.irg.nlp.am.Constants;
import es.uam.irg.utils.StringUtils;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ansegura
 */
public class ArgumentEngine implements Constants {
    
    // Class members
    private String language;
    private String nlpPath;
    private PrintWriter out;
    private Properties props;
    
    /**
     * Class constructor
     * @param lang 
     */
    public ArgumentEngine(String lang) {
        this.out = new PrintWriter(System.out);
        this.language = lang;
        this.nlpPath = System.getenv("CORENLP_HOME");
        
        setProperties();
    }
    
    /**
     * 
     * @param key
     * @param text 
     */
    public void analyze(int key, String text) {
        System.out.format("Task Analyze - Id: %s, Proposal: %s\n", key, text);
        
        StanfordCoreNLP pipeline = new StanfordCoreNLP(this.props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        // This prints out the results of sentence analysis to file(s) in good formats
        pipeline.prettyPrint(annotation, this.out);
    }
    
    /**
     * 
     * @param key
     * @param text
     * @param linker
     * @return 
     */
    public List<Argument> annotate(int key, String title, String text, ArgumentLinker linker) {
        System.out.format("Task Annotate - Id: %s, Proposal: %s\n", key, text);
        List<Argument> result = new ArrayList<>();
        
        // NLP objects
        StanfordCoreNLP pipeline = new StanfordCoreNLP(this.props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        
        // Get named entity recognition
        Map<String, String> entities = getNamedEntities(pipeline, title);
        System.out.println("Show Named Entity Recognition: " + entities.size());
        for (Map.Entry<String, String> entry : entities.entrySet()) {
            System.out.println(entry.getKey()+ ": " + entry.getValue());
        }
        
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println("N sentences: " + sentences.size());
        
        for (int i = 0; i < sentences.size(); i++) {
            
            // 1. Get current sentence
            String sentenceID = key + "-" + (i + 1);
            CoreMap sentence = sentences.get(i);
            System.out.format("[%s]: %s \n", sentenceID, sentence.toString());
            
            // 2. Display Parts of Speech
            CoreDocument document = pipeline.processToCoreDocument(sentence.toString());
            List<String> verbList = new ArrayList<>();
            document.tokens().forEach((CoreLabel token) -> {
                // System.out.println(String.format("%s [%s]", token.word(), token.tag()));
                if (token.tag().equals("VERB")) {
                    verbList.add(token.word());
                }
            });
            System.out.println("Verb list: " + verbList.toString());
            
            // 3. Get constituency tree
            System.out.println("Show constituency tree:");
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            tree.pennPrint(out);
            
            // 4. Get get syntagma list
            List<Syntagma> syntagmaList = getSyntagmaList(tree);
            System.out.println("Syntagma list: " + syntagmaList.size());

            // 5. Arguments Mining
            Argument arg = getArgument(sentenceID, sentence.toString(), syntagmaList, verbList, linker);
            if (arg.isValid()) {
                arg.setEntityList(entities);
                System.out.println(arg.getString());
                result.add(arg);
            }
            else {
                System.out.format("Sentence %s of phrase %s has no argument\n", (i + 1), key);
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param tree
     * @return 
     */
    public List<Syntagma> getSyntagmaList(Tree tree) {
        List<Syntagma> syntagmaList = new ArrayList<>();
        getSyntagmaList(tree, 0, syntagmaList);
        return syntagmaList;
    }
    
    /**
     * 
     * @param sentence
     * @param syntagmaList
     * @param verbList
     * @param linker
     * @return 
     */
    private Argument getArgument(String sentenceID, String sentence, List<Syntagma> syntagmaList, List<String> verbList, ArgumentLinker linker) {
        Argument arg = new Argument(sentenceID, sentence);
        
        // Temporary variables
        String premise = null;
        String claim = null;
        String mainVerb = null;
        String relationType = linker.relationType;
        
        /*
            Approach 1: claim + linker + premise
        */
        int minDepth = Integer.MAX_VALUE;
        
        for (Syntagma syntagma : syntagmaList) {
            if (syntagma.text.split(" ")[0].equals(linker.linker)) {
                System.out.format("%s - %s\n", syntagma.getString(), linker.getString());
                if (syntagma.depth < minDepth) {
                    minDepth = syntagma.depth;
                }
            }
            else {
                System.out.format("%s\n", syntagma.getString());
            }
        }
        
        if (minDepth < Integer.MAX_VALUE) {
            for (Syntagma syntagma : syntagmaList) {
                if (syntagma.depth == minDepth) {
                    if (syntagma.text.split(" ")[0].equals(linker.linker)) {
                        premise = syntagma.text;
                    }
                }
                else if (syntagma.depth == minDepth - 1 && premise != null && syntagma.text.contains(premise)) {
                    claim = syntagma.text.replace(premise, "").trim();
                    if (claim.endsWith(".")) {
                        claim = claim.replace(".", "").trim();
                    }
                }
            }
            
            if (!StringUtils.isEmpty(premise) && !StringUtils.isEmpty(claim)) {
                
                // Identify main verb
                for (int i = 0; i < verbList.size() && mainVerb == null; i++) {
                    String verb = verbList.get(i);
                    if (claim.contains(verb)) {
                        mainVerb = verb;
                    }
                }
                
                arg = new Argument(sentenceID, sentence, premise, claim, mainVerb, relationType, "A1:C+L+P");
            }
        }
        
        return arg;
    }
    
    /**
     * Apply Named Entity Recognition.
     * 
     * @param pipeline
     * @param text
     * @return 
     */
    private Map<String, String> getNamedEntities(StanfordCoreNLP pipeline, String text) {
        Map<String, String> entities = new HashMap<>();
        List<String>  entityType = Arrays.asList("PERSON", "ORGANIZATION", "LOCATION");
        CoreDocument document = pipeline.processToCoreDocument(text);
        
        for (CoreEntityMention em : document.entityMentions()) {
            if (entityType.contains(em.entityType())) {
                entities.put(em.text(), em.entityType());
            }
        }
        
        return entities;
    }
    
    /**
     * 
     * @param tree
     * @param depth
     * @param syntagmaList
     * @return 
     */
    private String getSyntagmaList(Tree tree, int depth, List<Syntagma> syntagmaList) {
        String text = "";
        
        if (tree.numChildren() == 0) {
            text = tree.value() + " ";
        }
        else {
            for (Tree node : tree.children()) {
                text += getSyntagmaList(node, depth + 1, syntagmaList);
            }
            if (text.split(" ").length > 1)
                syntagmaList.add( new Syntagma(text.trim(), tree.value(), depth));
        }
        
        return text;
    }
    
    /**
     *
     */
    private void setProperties() {
        this.props = new Properties();
        
        try {
            if (language.equals(LANG_EN)) {
                this.props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
            }
            else if (language.equals(LANG_ES)) {
                this.props.load(new FileInputStream(nlpPath + "/StanfordCoreNLP-spanish.properties"));
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
