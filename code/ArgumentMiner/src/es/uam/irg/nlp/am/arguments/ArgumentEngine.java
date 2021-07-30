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
import es.uam.irg.utils.FunctionUtils;
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

    // Class constants
    private static final String CLAIM = "claim";
    private static final List<String> ENTITY_TYPE = Arrays.asList("PERSON", "ORGANIZATION", "LOCATION");
    private static final String PREMISE = "premise";
    
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
     * @param docKey
     * @param docText 
     */
    public void analyze(int docKey, String docText) {
        System.out.format("Task Analyze - Id: %s, Proposal: %s\n", docKey, docText);
        
        // Annotate entire document with Stanford CoreNLP
        StanfordCoreNLP pipeline = new StanfordCoreNLP(this.props);
        Annotation annotation = new Annotation(docText);
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
        
        // 2. Get sentences from the document
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println("N sentences: " + sentences.size());
        
        // 3. Create major claim object
        Sentence majorClaim = createMajorClaim(pipeline, docTitle);
        
        // 4. For each item..
        for (int i = 0; i < sentences.size(); i++) {
            
            // 5. Get current sentence
            String sentenceID = docKey + "-" + (i + 1);
            CoreMap sentence = sentences.get(i);
            String sentenceText = sentence.toString();
            System.out.format("[%s]: %s\n", sentenceID, sentenceText);
            
            // 6. Get main sentence linker
            ArgumentLinker linker = getSentenceLinker(sentenceText);
            
            if (linker != null) {
                System.out.println("Sentence linker: " + linker.getString());
            
                // 7. Get named entity recognition (NER) in document
                Map<String, String> entities = getNamedEntities(pipeline, sentenceText);
                List<String> entityList = new ArrayList<>(entities.keySet());
                
                // 8. Apply parts of speech (POS) to identify list of NOUNs and VERBs in document
                List<CoreLabel> tokens = getPartsOfSpeechTokens(pipeline, sentenceText, Arrays.asList("NOUN", "VERB"));
                List<String> nounList = new ArrayList<>();
                List<String> verbList = new ArrayList<>();

                for (CoreLabel token : tokens) {
                    //System.out.println(String.format("%s [%s]", token.word(), token.tag()));
                    if (token.tag().equals("NOUN")) {
                        nounList.add(token.word());
                    }
                    else if (token.tag().equals("VERB")) {
                        verbList.add(token.word());
                    }
                }
                System.out.println("Noun list: " + nounList.toString());
                System.out.println("Verb list: " + verbList.toString());

                // 9. Get constituency tree
                System.out.println("Calculate constituency tree:");
                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                tree.pennPrint(out);

                // 10. Get syntagma list
                List<Syntagma> syntagmaList = getSyntagmaList(tree);
                System.out.println("Syntagma list: " + syntagmaList.size());

                // 11. Apply arguments mining (AM)
                Argument arg = mineArgument(sentenceID, sentenceText, linker, syntagmaList, entityList, nounList, verbList);
                
                // 12. Save argument
                if (arg.isValid()) {
                    arg.setMajorClaim(majorClaim);
                    result.add(arg);
                    System.out.println(arg.getString());
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
     * @param premise
     * @param premise0
     * @param linker
     * @return 
     */
    private String cleanStatement(String statType, String statement, ArgumentLinker linker) {
        String newStatement = "";
        String latestToken;
        
        if (statType.equals(CLAIM)) {
            newStatement = StringUtils.cleanText(statement, StringUtils.CLEAN_RIGHT);
            latestToken = StringUtils.getLastToken(newStatement, " ");
            
            if (latestToken.equals("y") || latestToken.equals("o")) {
                newStatement = newStatement.substring(0, newStatement.length() - 1);
                newStatement = StringUtils.cleanText(newStatement, StringUtils.CLEAN_RIGHT);
            }
        }
        else if (statType.equals(PREMISE)) {
            newStatement = StringUtils.cleanText(statement, StringUtils.CLEAN_BOTH);
            
            if (linker != null) {
                newStatement = newStatement.replace(linker.linker, "").trim();
            }
        }
        
        return newStatement;
    }
    
    /**
     * 
     * @param text
     * @param nouns
     * @param entities
     * @return 
     */
    private Sentence createArgumentativeSentence(String text, List<String> nouns, List<String> entities) {
        
        List<String> sentNouns = new ArrayList<>();
        for (String noun : nouns) {
            if (text.contains(noun)) {
                sentNouns.add(noun);
            }
        }
        
        List<String> sentEntities = new ArrayList<>();
        for (String entity : entities) {
            if (text.contains(entity)) {
                sentEntities.add(entity);
            }
        }
        
        return new Sentence(text, sentNouns, sentEntities);
    }
    
    /**
     * 
     * @param pipeline
     * @param text
     * @return 
     */
    private Sentence createMajorClaim(StanfordCoreNLP pipeline, String text) {
        text = StringUtils.cleanText(text, StringUtils.CLEAN_RIGHT);
        
        // Get nouns (from POS) in document title
        List<CoreLabel> tokens = getPartsOfSpeechTokens(pipeline, text, Arrays.asList("NOUN"));
        List<String> nounList = new ArrayList<>();
        tokens.forEach(token -> {
            nounList.add(token.word());
        });

        // Get named entity recognition (NER) in document title
        Map<String, String> entities = getNamedEntities(pipeline, text);
        List<String> entityList = new ArrayList<>(entities.keySet());
        
        return new Sentence(text, nounList, entityList);
    }
    
    /**
     *
     * @param tokens
     * @param nTokens
     * @return
     */
    private String getNGram(String text, int nTokens) {
        String nGram = "";
        
        try {
            String newText = StringUtils.cleanText(text, StringUtils.CLEAN_BOTH);
            String[] tokens = newText.split(" ");
            
            if (tokens.length > 0) {
                String[] subList = FunctionUtils.getSubArray(tokens, 0, nTokens);
                nGram = FunctionUtils.arrayToString(subList, Constants.NGRAMS_DELIMITER);
            }
        }
        catch (Exception ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nGram;
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
        CoreDocument document = pipeline.processToCoreDocument(text);
        
        for (CoreEntityMention em : document.entityMentions()) {
            if (ENTITY_TYPE.contains(em.entityType())) {
                entities.put(em.text(), em.entityType());
            }
        }
        
        return entities;
    }
    
    /**
     * 
     * @param pipeline
     * @param sentenceText
     * @return 
     */
    private List<CoreLabel> getPartsOfSpeechTokens(StanfordCoreNLP pipeline, String sentenceText, List<String> labels) {
        List<CoreLabel> tokens;
        CoreDocument document = pipeline.processToCoreDocument(sentenceText);
        
        if (labels != null) {
            tokens = new ArrayList<>();
            for (CoreLabel token : document.tokens()) {
                if (labels.contains(token.tag())) {
                    tokens.add(token);
                }
            }
        }
        else {
            tokens = new ArrayList<>(document.tokens());
        }
        
        return tokens;
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
     * Identify main verb.
     * 
     * @param sentence
     * @param verbList
     * @return 
     */
    private String identifyMainVerb(String sentence, List<String> verbList) {
        String mainVerb = null;
        
        for (int i = 0; i < verbList.size(); i++) {
            mainVerb = verbList.get(i);
            if (sentence.contains(mainVerb)) {
                return mainVerb;
            }
        }
        
        return null;
    }
    
    /**
     * 
     * @param sentenceID
     * @param sentenceText
     * @param linker
     * @param syntagmaList
     * @param entityList
     * @param nounList
     * @param verbList
     * @return 
     */
    private Argument mineArgument(String sentenceID, String sentenceText, ArgumentLinker linker, List<Syntagma> syntagmaList, List<String> entityList, List<String> nounList, List<String> verbList) {
        Argument arg = new Argument(sentenceID, sentenceText);
        
        // Temporary variables
        String approach;
        String premise;
        String claim;
        String mainVerb = null;
        int minDepth;
        
        /*
            Approach 1: claim + linker + premise
        */
        approach = "A1 -> C+L+P";
        premise = null;
        claim = null;

        for (Syntagma syntagma : syntagmaList) {
            System.out.println(syntagma.getString());
            
            if (syntagma.depth == 2) {

                if (claim == null) {
                    claim = cleanStatement(CLAIM, syntagma.text, null);
                    mainVerb = identifyMainVerb(claim, verbList);
                }
                else if (premise == null) {
                    premise = cleanStatement(PREMISE, syntagma.text, null);
                    if (mainVerb == null) {
                        mainVerb = identifyMainVerb(premise, verbList);
                    }
                }
            }
            else if (syntagma.depth == 1 && premise != null) {
                String nGram = syntagma.text.replace(claim, "").replace(premise, "");
                nGram = StringUtils.cleanText(nGram, StringUtils.CLEAN_BOTH).replace(" ", Constants.NGRAMS_DELIMITER);
                
                if (linker.isEquals(nGram)) {
                    
                    // Create argument object
                    Sentence sentClaim = createArgumentativeSentence(claim, nounList, entityList);
                    Sentence sentPremise = createArgumentativeSentence(premise, nounList, entityList);
                    arg = new Argument(sentenceID, sentenceText, sentClaim, sentPremise, mainVerb, linker, approach);
                    break;
                }
            }
        }
        
        if (!arg.isValid()) {
            /*
                Approach 2: claim + (linker + premise)
            */
            approach = "A2 -> C+(L+P)";
            premise = null;
            claim = null;
            minDepth = Integer.MAX_VALUE;
            
            for (Syntagma syntagma : syntagmaList) {
                String nGram = getNGram(syntagma.text, linker.nTokens);

                if (linker.isEquals(nGram)) {
                    if (syntagma.depth < minDepth) {
                        minDepth = syntagma.depth;
                        premise = syntagma.text;
                    }
                }
            }
            
            if (premise != null) {
                
                if (!verbList.isEmpty()) {
                    for (Syntagma syntagma : syntagmaList) {
                        if (syntagma.depth < minDepth && syntagma.text.contains(premise)) {
                            int endIx = syntagma.text.indexOf(premise);
                            claim = syntagma.text.substring(0, endIx).trim();
                            mainVerb = identifyMainVerb(claim, verbList);

                            if (mainVerb != null) {
                                break;
                            }
                            else {
                                claim = null;
                            }
                        }
                    }
                }
                
                if (claim == null) {
                    
                    if (!verbList.isEmpty()) {
                        mainVerb = identifyMainVerb(premise, verbList);
                    }
                    
                    if (verbList.isEmpty() || mainVerb != null) {
                        for (Syntagma syntagma : syntagmaList) {
                            if (syntagma.depth == minDepth && !syntagma.text.equals(premise)) {
                                claim = syntagma.text;
                                break;
                            }
                        }
                    }
                }

                if (!StringUtils.isEmpty(premise) && !StringUtils.isEmpty(claim)) {
                    claim = cleanStatement(CLAIM, claim, null);
                    premise = cleanStatement(PREMISE, premise, linker);
                    
                    // Create argument object
                    Sentence sentClaim = createArgumentativeSentence(claim, nounList, entityList);
                    Sentence sentPremise = createArgumentativeSentence(premise, nounList, entityList);
                    arg = new Argument(sentenceID, sentenceText, sentClaim, sentPremise, mainVerb, linker, approach);
                }
            }
        }
        
        return arg;
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
