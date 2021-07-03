/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import es.uam.irg.nlp.am.Constants;
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
        System.out.format("Aanalyze >> Id: %s, Proposal: %s\n", key, text);
        
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
     */
    public void annotate(int key, String text, ArgumentLinker linker) {
        System.out.format("Annotate >> Id: %s, Proposal: %s\n", key, text);
        
        StanfordCoreNLP pipeline = new StanfordCoreNLP(this.props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println("N sentences: " + sentences.size());
        
        for (int i = 0; i < sentences.size(); i++) {
            
            // 1. Get current sentence
            CoreMap sentence = sentences.get(i);
            System.out.format("[%s]: %s \n", (i + 1), sentence.toString());
            
            // 2. Display Parts of Speech
            CoreDocument document = pipeline.processToCoreDocument(sentence.toString());
            List<String> verbList = new ArrayList<>();
            document.tokens().forEach((CoreLabel token) -> {
                System.out.println(String.format("%s [%s]", token.word(), token.tag()));
                if (token.tag().equals("VERB")) {
                    verbList.add(token.word());
                }
            });
            System.out.println(verbList.toString());
            
            // 3. Get constituency tree
            System.out.println("Show constituency tree:");
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            tree.pennPrint(out);
            
            // 4. Get get syntagma list
            List<Syntagma> syntagmaList = getSyntagmaList(tree);
            System.out.println("Show syntagma list: " + syntagmaList.size());
            syntagmaList.forEach((Syntagma syntagma) -> {
                if (syntagma.text.split(" ")[0].equals(linker.linker)) {
                    System.out.format("%s - %s \n", syntagma.getString(), linker.getString());
                }
                else {
                    System.out.println(syntagma.getString());
                }
            });

            // 5. Arguments Mining
            
        }
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
