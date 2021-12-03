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
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import es.uam.irg.nlp.am.Constants;
import es.uam.irg.nlp.corenlp.syntax.SyntacticAnalysisManager;
import es.uam.irg.nlp.corenlp.syntax.SyntacticallyAnalyzedSentence;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebank;
import es.uam.irg.nlp.corenlp.syntax.treebank.SyntacticTreebankNode;
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
    private static final HashSet<String> ENTITY_TYPE = new HashSet(
            Arrays.asList("PERSON", "LOCATION", "ORGANIZATION", "MISC", "CITY", "STATE_OR_PROVINCE", "COUNTRY", "TITLE"));
    private static final String PREMISE = "premise";

    // Class members
    private final String language;
    private final List<ArgumentLinker> lexicon;
    private PrintWriter out;
    private Properties props;
    private final HashSet<String> stopwords;
    private final TreeAnalyzer ta;

    /**
     * Class constructor.
     *
     * @param lang
     * @param lnkManager
     * @param stopwords
     */
    public ArgumentEngine(String lang, ArgumentLinkerManager lnkManager, HashSet<String> stopwords) {
        this.language = lang;
        this.lexicon = lnkManager.getLexicon(false);
        this.stopwords = stopwords;
        this.ta = new TreeAnalyzer(this.lexicon);
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
     * @param userID
     * @param commentID
     * @param parentID
     * @param docTitle
     * @param docText
     * @return
     */
    public List<Argument> extract(int docKey, int userID, int commentID, int parentID, String docTitle, String docText) {
        System.out.format("Task Annotate - Id: %s, Comment Id: %s, Proposal: %s\n", docKey, commentID, docText);
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
            String sentenceID = docKey + (commentID > -1 ? "-" + commentID : "") + "-" + (i + 1);

            // 5. Get current sentence
            CoreMap sentence = sentences.get(i);
            String sentenceText = sentence.toString();
            System.out.format("[%s]: %s\n", sentenceID, sentenceText);

            // 6. Get named entity recognition (NER) in document
            Map<String, String> entities = getNamedEntities(pipeline, sentenceText);
            List<String> entityList = new ArrayList<>(entities.keySet());

            // 7. Apply parts of speech (POS) to identify list of NOUNs and VERBs in document
            List<CoreLabel> tokens = getPartsOfSpeechTokens(pipeline, sentenceText, Arrays.asList("NOUN", "VERB"));
            List<String> nounList = new ArrayList<>();
            List<String> verbList = new ArrayList<>();

            tokens.forEach(token -> {
                if (token.tag().equals("NOUN")) {
                    if (!stopwords.contains(token.word())) {
                        nounList.add(token.word());
                    }
                } else if (token.tag().equals("VERB")) {
                    verbList.add(token.word());
                }
            });
            System.out.println("Noun list: " + nounList.toString());
            System.out.println("Verb list: " + verbList.toString());

            // 8. Get constituency tree
            System.out.println("Calculate constituency tree:");
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            tree.pennPrint(out);

            // 9. Apply arguments mining (AM)
            List<Argument> arguments = mineArguments(sentenceID, userID, commentID, parentID, sentenceText, tree, entityList, nounList, verbList);

            // 10. Save arguments
            if (arguments.size() > 0) {
                arguments.forEach(arg -> {
                    arg.setMajorClaim(majorClaim);
                    result.add(arg);
                    System.out.println(arg.getString());
                });

            } else {
                System.err.format("Sentence %s of phrase %s has no argument\n", (i + 1), docKey);
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
        } else if (statType.equals(PREMISE)) {
            newStatement = StringUtils.cleanText(statement, StringUtils.CLEAN_BOTH);

            if (linker != null) {
                newStatement = newStatement.replaceFirst(linker.linker, "").trim();
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
        text = text.trim();

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
        List<String> nounList = new ArrayList<>();
        List<String> entityList = new ArrayList<>();

        if (text.length() > 0) {

            // Get nouns (from POS) in document title
            List<CoreLabel> tokens = getPartsOfSpeechTokens(pipeline, text, Arrays.asList("NOUN"));
            tokens.forEach(token -> {
                nounList.add(token.word());
            });

            // Get named entity recognition (NER) in document title
            Map<String, String> entities = getNamedEntities(pipeline, text);
            entityList.addAll(entities.keySet());
        }

        return new Sentence(text, nounList, entityList);
    }

    /**
     * Apply Named Entity Recognition task.
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
                if (!this.stopwords.contains(em.text().toLowerCase()) || em.entityType().equals("ORGANIZATION") || (em.entityType().equals("MISC") && em.text().length() > 2)) {
                    entities.put(em.text(), em.entityType());
                }
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
        } else {
            tokens = new ArrayList<>(document.tokens());
        }

        return tokens;
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
     * Extracts arguments in the form of claim+linker+premise from textual
     * content.
     *
     * @param sentenceID
     * @param userID
     * @param commentID
     * @param parentID
     * @param sentenceText
     * @param tree
     * @param entityList
     * @param nounList
     * @param verbList
     * @return
     */
    private List<Argument> mineArguments(String sentenceID, int userID, int commentID, int parentID, String sentenceText,
            Tree tree, List<String> entityList, List<String> nounList, List<String> verbList) {
        List<Argument> arguments = new ArrayList<>();
        Set<String> patterns = new ArraySet<>();

        try {
            // Creating syntactic treebank
            String treeDescription = tree.skipRoot().pennString();
            SyntacticTreebank treebank = new SyntacticTreebank(treeDescription, true);
            SyntacticallyAnalyzedSentence analyzedSentence = new SyntacticallyAnalyzedSentence(sentenceText, treebank);
            SyntacticTreebank analyzedTree = analyzedSentence.getTreebank();
            String lnkText;

            // Breadth-first search
            SyntacticTreebankNode currNode;
            SyntacticTreebankNode parent;
            Queue<SyntacticTreebankNode> queue = new LinkedList<>();
            currNode = analyzedTree.getRootNode();
            queue.add(currNode);

            while (!queue.isEmpty()) {
                currNode = queue.remove();

                // Checking if the text has or starts with a linker
                lnkText = SyntacticAnalysisManager.getLinkerNodeText(treebank, currNode);
                ArgumentLinker linker = ta.textHasLinker(lnkText);

                if (linker != null) {
                    parent = SyntacticAnalysisManager.getLinkerParentNode(analyzedTree, currNode);
                    System.out.println(" - Linker: " + linker.getString());
                    System.out.println("   " + currNode.toString());
                    System.out.println(" - Parent: " + parent.toString());

                    // Create sentence pattern
                    String sentPattern = SyntacticAnalysisManager.createSentencePattern(analyzedTree, parent, currNode);

                    // If the argument pattern is valid...
                    System.out.println(" - Pattern: " + sentPattern);
                    if (SyntacticAnalysisManager.checkArgumentPattern(sentPattern) && !patterns.contains(sentPattern)) {
                        System.out.println(" + Valid pattern!");
                        patterns.add(sentPattern);

                        // Reconstructing sentences (claim and premise)
                        String claim = "";
                        String premise = "";

                        for (Integer childId : analyzedTree.getChildrenIdsOf(parent)) {
                            SyntacticTreebankNode child = analyzedTree.getNode(childId);
                            System.out.println("   " + child.toString());

                            String currText = SyntacticAnalysisManager.getTreeText(treebank, child);
                            if (!"".equals(premise) || currText.startsWith(linker.linker)) {
                                premise += currText + " ";
                            } else {
                                claim += currText + " ";
                            }
                        }

                        // Creating new argument
                        if (!StringUtils.isEmpty(claim) && !StringUtils.isEmpty(premise)) {
                            claim = cleanStatement(CLAIM, claim, null);
                            premise = cleanStatement(PREMISE, premise, linker);

                            // identify main verb
                            String mainVerb = identifyMainVerb(claim, verbList);
                            if (mainVerb == null) {
                                mainVerb = identifyMainVerb(premise, verbList);
                            }

                            // Create argument object
                            String argumentID = sentenceID + "-" + (arguments.size() + 1);
                            Sentence sentClaim = createArgumentativeSentence(claim, nounList, entityList);
                            Sentence sentPremise = createArgumentativeSentence(premise, nounList, entityList);
                            arguments.add(new Argument(argumentID, userID, commentID, parentID, sentenceText, sentClaim, sentPremise, mainVerb, linker, sentPattern, treeDescription));
                        } else {

                            System.out.println(" - Error extracting claim and/or premise!");
                        }

                    } else {
                        System.out.println(" - Invalid pattern!");
                    }
                }

                for (Integer childId : analyzedTree.getChildrenIdsOf(currNode)) {
                    queue.add(analyzedTree.getNode(childId));
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        // System.out.println("Arguments number: " + arguments.size());
        return arguments;
    }

    /**
     *
     */
    private void setProperties() {
        this.props = new Properties();

        try {
            if (language.equals(LANG_EN)) {
                this.props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
            } else if (language.equals(LANG_ES)) {
                this.props.load(new FileInputStream(Constants.SPANISH_PROPERTIES));
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
