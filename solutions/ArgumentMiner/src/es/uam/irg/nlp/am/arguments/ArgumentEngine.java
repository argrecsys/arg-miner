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
    private ArgumentLinkerManager lnkManager;
    private PrintWriter out;
    private Properties props;
    
    /**
     * Class constructor.
     * 
     * @param lang
     * @param lnkManager 
     */
    public ArgumentEngine(String lang, ArgumentLinkerManager lnkManager) {
        this.language = lang;
        this.lnkManager = lnkManager;
        this.out = new PrintWriter(System.out);
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
     * @param docKey
     * @param docTitle
     * @param docText
     * @return 
     */
    public List<Argument> annotate(int docKey, String docTitle, String docText) {
        System.out.format("Task Annotate - Id: %s, Proposal: %s\n", docKey, docText);
        List<Argument> result = new ArrayList<>();
        
        // 1. Annotate entire document with Stanford CoreNLP
        StanfordCoreNLP pipeline = new StanfordCoreNLP(this.props);
        Annotation annotation = new Annotation(docText);
        pipeline.annotate(annotation);
        
        // 2. Get named entity recognition in document
        Map<String, String> entities = getNamedEntities(pipeline, docTitle);
        System.out.println("Show Named Entity Recognition: " + entities.size());
        for (Map.Entry<String, String> entry : entities.entrySet()) {
            System.out.println(entry.getKey()+ ": " + entry.getValue());
        }
        
        // 3. Get sentences from the document
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println("N sentences: " + sentences.size());
        
        for (int i = 0; i < sentences.size(); i++) {
            
            // 4. Get current sentence
            String sentenceID = docKey + "-" + (i + 1);
            CoreMap sentence = sentences.get(i);
            String sentenceText = sentence.toString();
            System.out.format("[%s]: %s\n", sentenceID, sentenceText);
            
            // 5. Get main sentence linker
            ArgumentLinker linker = getSentenceLinker(sentenceText);
            
            if (linker != null) {
                System.out.println("Sentence linker: " + linker.getString());
            
                // 6. Apply parts of speech (POS) to identify list of NOUNs and VERBs
                CoreDocument document = pipeline.processToCoreDocument(sentenceText);
                List<String> nounList = new ArrayList<>();
                List<String> verbList = new ArrayList<>();

                document.tokens().forEach((CoreLabel token) -> {
                    // System.out.println(String.format("%s [%s]", token.word(), token.tag()));
                    if (token.tag().equals("NOUN")) {
                        nounList.add(token.word());
                    }
                    else if (token.tag().equals("VERB")) {
                        verbList.add(token.word());
                    }
                });
                System.out.println("Noun list: " + nounList.toString());
                System.out.println("Verb list: " + verbList.toString());

                // 7. Get constituency tree
                System.out.println("Calculate constituency tree:");
                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                tree.pennPrint(out);

                // 8. Get syntagma list
                List<Syntagma> syntagmaList = getSyntagmaList(tree);
                System.out.println("Syntagma list: " + syntagmaList.size());

                // 9. Apply arguments mining (AM)
                Argument arg = getArgument(sentenceID, sentenceText, linker, syntagmaList, verbList);
                
                if (arg.isValid()) {
                    arg.setEntityList(entities);
                    arg.setNounList(nounList);
                    System.out.println(arg.getString());
                    result.add(arg);
                }
                else {
                    System.err.format("Sentence %s of phrase %s has no argument\n", (i + 1), docKey);
                }
            }
            else {
                System.err.format("Sentence %s of phrase %s has no linker\n", (i + 1), docKey);
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param sentenceID
     * @param sentenceText
     * @param linker
     * @param syntagmaList
     * @param verbList
     * @return 
     */
    private Argument getArgument(String sentenceID, String sentenceText, ArgumentLinker linker, List<Syntagma> syntagmaList, List<String> verbList) {
        Argument arg = new Argument(sentenceID, sentenceText);
        
        // Temporary variables
        String approach;
        String premise = null;
        String claim = null;
        String mainVerb = null;
        
        /*
            Approach 1: claim + linker + premise
        */
        approach = "A1:C+L+P";
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
                    int endIx = syntagma.text.indexOf(premise);
                    claim = syntagma.text.substring(0, endIx).trim();
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
                
                // Create argument object
                arg = new Argument(sentenceID, sentenceText, premise, claim, mainVerb, approach, linker);
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
     * @param sentenceText
     * @return 
     */
    private ArgumentLinker getSentenceLinker(String sentenceText) {
        ArgumentLinker linker = null;
        
        for (int i = 0; i < lnkManager.getNLinkers(); i++) {
            linker = lnkManager.getLinker(i);
            if (sentenceText.contains(linker.linker)) {
                return linker;
            }
        }
        
        return null;
    }
    
    /**
     *
     * @param tree
     * @return
     */
    private List<Syntagma> getSyntagmaList(Tree tree) {
        List<Syntagma> syntagmaList = new ArrayList<>();
        getSyntagmaList(tree, 0, syntagmaList);
        return syntagmaList;
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
                this.props.load(new FileInputStream(Constants.SPANISH_PROPERTIES));
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
