/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import es.uam.irg.utils.Constants;
import es.uam.irg.utils.StringUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ansegura
 */
public class ArgumentEngine implements Constants {
    
    private Properties props;
    private PrintWriter out;
    private String language;
    private String nlpPath;
    
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
     */
    public void annotate(int key, String text) {
        System.out.format("Annotate >> Id: %s, Proposal: %s\n", key, text);
        
        StanfordCoreNLP pipeline = new StanfordCoreNLP(this.props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println("N sentences: " + sentences.size());
        
        for (int i = 0; i < sentences.size(); i++) {
            CoreMap sentence = sentences.get(i);
            System.out.format("[%s]: %s \n", (i + 1), sentence.toString());
            
            System.out.println("Show Constituency Tree:");
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            tree.pennPrint(out);
            
            System.out.println("Show Tree:");
            showTree(tree);
            
            Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());
            System.out.println("N Constituents: " + treeConstituents.size());
            
            // Check constituents
            for (Constituent constituent : treeConstituents) {
                if (constituent.label() != null && (constituent.label().toString().equals("VP") || constituent.label().toString().equals("NP"))) {
                    System.err.println("found constituent: " + constituent.toString());
                    System.err.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
                }
            }
            
        }
    }
    
    /**
     * 
     * @param tree 
     */
    public void showTree(Tree tree) {
        String text = showTree(tree, 0);
        System.out.println(text);
    }
    
    /**
     * 
     * @param tree
     * @param depth 
     */
    private String showTree(Tree tree, int depth) {
        String text = "";
        
        if (tree.numChildren() == 0) {
            text = tree.value() + " ";
        }
        else {
            for (Tree node : tree.children()) {
                text += showTree(node, depth + 1);
            }
            if (text.split(" ").length > 1)
                System.out.println("[" + tree.label() + "] " + text);
        }
        
        return text;
    }
    
}
