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
package es.uam.irg.nlp.am.arguments;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
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
 * Class of the argument extractor engine.
 */
public class ArgumentEngine {

    // Class constants
    public static final String LANG_EN = "en";
    public static final String LANG_ES = "es";
    private static final String CLAIM = "claim";
    private static final HashSet<String> ENTITY_TYPE = new HashSet(
            Arrays.asList("PERSON", "LOCATION", "ORGANIZATION", "MISC", "CITY", "STATE_OR_PROVINCE", "COUNTRY", "TITLE"));
    private static final String PREMISE = "premise";
    private static final String SPANISH_PROPERTIES = "Resources/config/StanfordCoreNLP-spanish.properties";

    // Class members
    private final HashSet<String> invalidLinkers;
    private final String language;
    private final PrintWriter out;
    private final TreeAnalyzer parser;
    private StanfordCoreNLP pipeline;
    private final HashSet<String> stopwords;

    /**
     * Class constructor.
     *
     * @param lang
     * @param lexicon
     * @param invalidLinkers
     * @param stopwords
     */
    public ArgumentEngine(String lang, List<ArgumentLinker> lexicon, HashSet<String> invalidLinkers, HashSet<String> stopwords) {
        this.language = lang;
        this.invalidLinkers = invalidLinkers;
        this.stopwords = stopwords;
        this.parser = new TreeAnalyzer(lexicon);
        this.out = new PrintWriter(System.out);
        createPipeline();
    }

    /**
     *
     * @param docKey
     * @param docText
     */
    public void analyze(int docKey, String docText) {
        System.out.format("Task Analyze - Id: %s, Proposal: %s\n", docKey, docText);

        // Annotate entire document with Stanford CoreNLP
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

        // 1. Create major claim object
        Sentence majorClaim = createMajorClaim(docTitle);

        // 2. Get candidate (simple and compound) sentences from the document
        List<CandSentence> sentences = getCandidateSentences(docText, false);
        System.out.println("N candidate sentences: " + sentences.size());

        // 3. For each item..
        for (int i = 0; i < sentences.size(); i++) {
            String sentID = docKey + "-" + (commentID > -1 ? commentID : 0) + "-" + (i + 1);

            // 4. Get candidate sentence
            CandSentence candidate = sentences.get(i);
            String sentText = candidate.getText();
            boolean sentSimple = candidate.isSimple();
            System.out.format("[%s]: %s\n", sentID, sentText);

            // 5. Get named entity recognition (NER) in document
            Map<String, String> entities = getNamedEntities(sentText);
            HashSet<String> entityList = new HashSet<>(entities.keySet());

            // 6. Apply parts of speech (POS) to identify list of NOUNs and VERBs in document
            HashSet<String> validPOS = new HashSet<>(Arrays.asList("NOUN", "VERB"));
            List<CoreLabel> tokens = getPartsOfSpeechTokens(sentText, validPOS);
            HashSet<String> nounList = new HashSet<>();
            HashSet<String> verbList = new HashSet<>();

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

            // 7. Get constituency tree
            System.out.println("Calculate constituency tree:");
            Tree tree = getConstituencyTree(sentText);
            tree.pennPrint(out);

            // 8. Apply arguments mining (AM)
            List<Argument> arguments = mineArguments(sentID, userID, commentID, parentID, sentText, sentSimple, tree, entityList, nounList, verbList);

            // 9. Save arguments
            if (arguments.size() > 0) {
                arguments.forEach(arg -> {
                    arg.setMajorClaim(majorClaim);
                    result.add(arg);
                    System.out.println(arg.toString());
                });

            } else {
                System.err.format("Sentence %s of phrase %s has no argument\n", (i + 1), docKey);
            }
        }

        return result;
    }

    /**
     * Creates the constituency parse tree of a sentence.
     *
     * @param text
     * @return
     */
    public Tree getConstituencyTree(String text) {
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        CoreMap sentence = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        return tree;
    }

    /**
     *
     * @param statType
     * @param statement
     * @param linker
     * @return
     */
    private String cleanStatement(String statType, String statement, ArgumentLinker linker) {
        String newStatement = "";
        String latestToken;

        if (statType.equals(CLAIM)) {
            newStatement = StringUtils.cleanText(statement, StringUtils.CLEAN_RIGHT);
            latestToken = StringUtils.getLastToken(newStatement, " ");

            if (invalidLinkers.contains(latestToken)) {
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
    private Sentence createArgumentativeSentence(String text, HashSet<String> nouns, HashSet<String> entities) {
        text = text.trim();
        List<String> sentNouns = new ArrayList<>();
        List<String> sentEntities = new ArrayList<>();

        HashSet<String> words = new HashSet<>(Arrays.asList(text.split("\\W")));
        for (String noun : nouns) {
            if (words.contains(noun)) {
                sentNouns.add(noun);
            }
        }

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
    private Sentence createMajorClaim(String text) {
        text = StringUtils.cleanText(text, StringUtils.CLEAN_RIGHT);
        List<String> nounList = new ArrayList<>();
        List<String> entityList = new ArrayList<>();

        if (text.length() > 0) {

            // Get nouns (from POS) in document title
            HashSet<String> validPOS = new HashSet<>(Arrays.asList("NOUN"));
            List<CoreLabel> tokens = getPartsOfSpeechTokens(text, validPOS);
            tokens.forEach(token -> {
                nounList.add(token.word());
            });

            // Get named entity recognition (NER) in document title
            Map<String, String> entities = getNamedEntities(text);
            entityList.addAll(entities.keySet());
        }

        return new Sentence(text, nounList, entityList);
    }

    /**
     * Creates the Stanford CoreNLP pipeline according to the specified
     * language.
     */
    private void createPipeline() {
        Properties props = new Properties();

        try {
            if (language.equals(LANG_EN)) {
                props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
            } else if (language.equals(LANG_ES)) {
                props.load(new FileInputStream(SPANISH_PROPERTIES));
            }
            this.pipeline = new StanfordCoreNLP(props);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArgumentEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns a list of candidate sentences.
     *
     * @param text
     * @param onlySimpleSent
     * @return
     */
    private List<CandSentence> getCandidateSentences(String text, boolean onlySimpleSent) {
        List<CandSentence> candSentences = new ArrayList<>();

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        CandSentence candidate;

        // Storing simple sentences
        for (CoreMap sentence : sentences) {
            candidate = new CandSentence(sentence.toString(), true);
            candSentences.add(candidate);
        }

        // Storing complex sentences
        if (!onlySimpleSent) {
            String sentenceText;
            String prevSentenceText;
            String currSentenceText;
            ArgumentLinker linker;

            for (int i = 1; i < sentences.size(); i++) {
                currSentenceText = StringUtils.cleanText(sentences.get(i).toString(), StringUtils.CLEAN_BOTH);
                linker = parser.textHasLinker(currSentenceText);

                if (linker != null) {
                    prevSentenceText = StringUtils.cleanText(sentences.get(i - 1).toString(), StringUtils.CLEAN_RIGHT);
                    currSentenceText = StringUtils.firstChartToLowerCase(currSentenceText);
                    sentenceText = prevSentenceText + ", " + currSentenceText + ".";
                    candidate = new CandSentence(sentenceText, false);
                    candSentences.add(candidate);
                }
            }
        }

        return candSentences;
    }

    /**
     * Apply Named Entity Recognition task.
     *
     * @param text
     * @return
     */
    private Map<String, String> getNamedEntities(String text) {
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
     * @param text
     * @param labels
     * @return
     */
    private List<CoreLabel> getPartsOfSpeechTokens(String text, HashSet<String> labels) {
        List<CoreLabel> tokens = new ArrayList<>();
        CoreDocument document = pipeline.processToCoreDocument(text);

        for (CoreLabel token : document.tokens()) {
            if (labels.isEmpty() || labels.contains(token.tag())) {
                tokens.add(token);
            }
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
    private String identifyMainVerb(String sentence, HashSet<String> verbList) {
        HashSet<String> words = new HashSet<>(Arrays.asList(sentence.split("\\W")));

        for (String verb : verbList) {
            if (words.contains(verb)) {
                return verb;
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
     * @param sentenceSimple
     * @param tree
     * @param entityList
     * @param nounList
     * @param verbList
     * @return
     */
    private List<Argument> mineArguments(String sentenceID, int userID, int commentID, int parentID, String sentenceText,
            boolean sentenceSimple, Tree tree, HashSet<String> entityList, HashSet<String> nounList, HashSet<String> verbList) {
        List<Argument> arguments = new ArrayList<>();
        HashSet<String> patterns = new HashSet<>();

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
                ArgumentLinker linker = parser.textHasLinker(lnkText);

                if (linker != null) {
                    parent = SyntacticAnalysisManager.getLinkerParentNode(analyzedTree, currNode);
                    System.out.println(" - Linker: " + linker.toString());
                    System.out.println("   " + currNode.toString());
                    System.out.println(" - Parent: " + parent.toString());

                    // Create sentence pattern
                    String sentPattern = SyntacticAnalysisManager.createSentencePattern(analyzedTree, parent, currNode);

                    // If the argument pattern is valid...
                    System.out.println(" - Pattern: " + sentPattern);
                    if (SyntacticAnalysisManager.checkArgumentPattern(sentPattern) && !patterns.contains(sentPattern)) {
                        System.out.println(" + Valid pattern!");

                        // Reconstructing sentences (claim and premise)
                        String claim = "";
                        String premise = "";

                        for (Integer childId : analyzedTree.getChildrenIdsOf(parent)) {
                            SyntacticTreebankNode child = analyzedTree.getNode(childId);
                            System.out.println("   " + child.toString());

                            String currText = SyntacticAnalysisManager.getTreeText(treebank, child);
                            if (!"".equals(premise) || StringUtils.cleanText(currText, StringUtils.CLEAN_BOTH).startsWith(linker.linker)) {
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
                            arguments.add(new Argument(argumentID, userID, commentID, parentID, sentenceText, sentenceSimple, sentClaim, sentPremise, mainVerb, linker, sentPattern, treeDescription));
                            patterns.add(sentPattern);

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

}
