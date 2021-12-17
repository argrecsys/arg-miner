/**
 * Copyright 2017
 * Ivan Cantador
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
package es.uam.irg.nlp.corenlp.syntax.pos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POSTags
 *
 * Manages the part-of-speech tags defined in the Penn Treebank Project
 * http://www.surdeanu.info/mihai/teaching/ista555-fall13/readings/PennTreebankConstituents.html
 * http://repository.upenn.edu/cgi/viewcontent.cgi?article=1603&context=cis_reports
 *
 * @author Ivan Cantador, ivan.cantador@uam.es
 * @version 1.0 - 16/03/2017
 */
public abstract class POSTags {

    public static final String CC = "CC";       // Coordinating conjunction
    public static final String CD = "CD";       // Cardinal number
    public static final String DT = "DT";       // Determiner
    public static final String EX = "EX";       // Existential there
    public static final String FW = "FW";       // Foreign word
    public static final String IN = "IN";       // Preposition or subordinating conjunction
    public static final String JJ = "JJ";       // Adjective
    public static final String JJR = "JJR";     // Adjective, comparative
    public static final String JJS = "JJS";     // Adjective, superlative
    public static final String LS = "LS";       // List item marker
    public static final String MD = "MD";       // Modal
    public static final String NN = "NN";       // Noun, singular or mass
    public static final String NNS = "NNS";     // Noun, plural
    public static final String NNP = "NNP";     // Proper noun, singular
    public static final String NNPS = "NNPS";   // Proper noun, plural
    public static final String PDT = "PDT";     // Predeterminer
    public static final String POS = "POS";     // Possessive ending
    public static final String PRP = "PRP";     // Personal pronoun
    public static final String PRP$ = "PRP$";   // Possessive pronoun
    public static final String RB = "RB";       // Adverb
    public static final String RBR = "RBR";     // Adverb, comparative
    public static final String RBS = "RBS";     // Adverb, superlative
    public static final String RP = "RP";       // Particle
    public static final String SYM = "SYM";     // Symbol
    public static final String TO = "TO";       // to
    public static final String UH = "UH";       // Interjection
    public static final String VB = "VB";       // Verb, base form
    public static final String VBD = "VBD";     // Verb, past tense
    public static final String VBG = "VBG";     // Verb, gerund or present participle
    public static final String VBN = "VBN";     // Verb, past participle
    public static final String VBP = "VBP";     // Verb, non-3rd person singular present
    public static final String VBZ = "VBZ";     // Verb, 3rd person singular present
    public static final String WDT = "WDT";     // Wh-determiner
    public static final String WP = "WP";       // Wh-pronoun
    public static final String WP$ = "WP$";     // Possessive wh-pronoun
    public static final String WRB = "WRB";     // Wh-adverb
    // -----
    public static final String TYPE_NOUN = "noun";
    public static final String TYPE_PRONOUN = "pronoun";
    public static final String TYPE_DETERMINER = "determiner";
    public static final String TYPE_ADJECTIVE = "adjective";
    public static final String TYPE_VERB = "verb";
    public static final String TYPE_ADVERB = "adverb";
    public static final String TYPE_CONNECT = "connect";
    public static final String TYPE_GENITIVE = "genitive";
    public static final String TYPE_PREPOSITION = "preposition";
    public static final String TYPE_INTERJECTION = "interjection";
    public static final String TYPE_PUNCTUATION = "punctuation";
    // -----
    private static final Map<String, String> TAG_NAMES;
    private static final Map<String, String> TAG_TYPES;
    private static final List<String> tagNames;
    private static final List<String> tagTypes;

    static {
        TAG_NAMES = new HashMap<>();

        TAG_NAMES.put(CC, "Coordinating conjunction");
        TAG_NAMES.put(CD, "Cardinal number");
        TAG_NAMES.put(DT, "Determiner");
        TAG_NAMES.put(EX, "Existential there");
        TAG_NAMES.put(FW, "Foreign word");
        TAG_NAMES.put(IN, "Preposition or subordinating conjunction");
        TAG_NAMES.put(JJ, "Adjective");
        TAG_NAMES.put(JJR, "Adjective, comparative");
        TAG_NAMES.put(JJS, "Adjective, superlative");
        TAG_NAMES.put(LS, "List item marker");
        TAG_NAMES.put(MD, "Modal");
        TAG_NAMES.put(NN, "Noun, singular or mass");
        TAG_NAMES.put(NNS, "Noun, plural");
        TAG_NAMES.put(NNP, "Proper noun, singular");
        TAG_NAMES.put(NNPS, "Proper noun, plural");
        TAG_NAMES.put(PDT, "Predeterminer");
        TAG_NAMES.put(POS, "Possessive ending");
        TAG_NAMES.put(PRP, "Personal pronoun");
        TAG_NAMES.put(PRP$, "Possessive pronoun");
        TAG_NAMES.put(RB, "Adverb");
        TAG_NAMES.put(RBR, "Adverb, comparative");
        TAG_NAMES.put(RBS, "Adverb, superlative");
        TAG_NAMES.put(RP, "Particle");
        TAG_NAMES.put(SYM, "Symbol");
        TAG_NAMES.put(TO, "to");
        TAG_NAMES.put(UH, "Interjection");
        TAG_NAMES.put(VB, "Verb, base form");
        TAG_NAMES.put(VBD, "Verb, past tense");
        TAG_NAMES.put(VBG, "Verb, gerund or present participle");
        TAG_NAMES.put(VBN, "Verb, past participle");
        TAG_NAMES.put(VBP, "Verb, non-3rd person singular present");
        TAG_NAMES.put(VBZ, "Verb, 3rd person singular present");
        TAG_NAMES.put(WDT, "Wh-determiner");
        TAG_NAMES.put(WP, "Wh-pronoun");
        TAG_NAMES.put(WP$, "Possessive wh-pronoun");
        TAG_NAMES.put(WRB, "Wh-adverb");
        TAG_NAMES.put("#", "");
        TAG_NAMES.put("$", "");
        TAG_NAMES.put(",", "");
        TAG_NAMES.put(".", "Sentence ending punctuation");
        TAG_NAMES.put(":", "Colon, semicolon or ellipse");
        TAG_NAMES.put("(", "Opening parenthesis");
        TAG_NAMES.put(")", "Closing parenthesis");
        TAG_NAMES.put("``", "Opening quote");
        TAG_NAMES.put("''", "Closing quote");

        tagNames = new ArrayList<>(TAG_NAMES.keySet());

        TAG_TYPES = new HashMap<>();

        TAG_TYPES.put(CC, TYPE_CONNECT);        // Coordinating conjunction
        TAG_TYPES.put(CD, TYPE_DETERMINER);     // Cardinal number
        TAG_TYPES.put(DT, TYPE_DETERMINER);     // Determiner
        TAG_TYPES.put(EX, TYPE_VERB);           // Existential there 
        TAG_TYPES.put(FW, null);                // Foreign word
        TAG_TYPES.put(IN, TYPE_CONNECT);        // Preposition or subordinating conjunction
        TAG_TYPES.put(JJ, TYPE_ADJECTIVE);	// Adjective
        TAG_TYPES.put(JJR, TYPE_ADJECTIVE);	// Adjective, comparative
        TAG_TYPES.put(JJS, TYPE_ADJECTIVE);	// Adjective, superlative
        TAG_TYPES.put(LS, null);                // List item marker
        TAG_TYPES.put(MD, TYPE_VERB);           // Modal: can cannot could couldn't dare may might must need ought shall should shouldn't will would
        TAG_TYPES.put(NN, TYPE_NOUN);           // Noun, singular or mass
        TAG_TYPES.put(NNP, TYPE_NOUN);          // Proper noun, singular
        TAG_TYPES.put(NNPS, TYPE_NOUN);         // Proper noun, plural
        TAG_TYPES.put(NNS, TYPE_NOUN);          // Noun, plural
        TAG_TYPES.put(PDT, TYPE_DETERMINER);    // Predeterminer: all both half many quite such sure this
        TAG_TYPES.put(POS, TYPE_GENITIVE);      // Possessive ending (genitive marker): ' or 's
        TAG_TYPES.put(PRP, TYPE_PRONOUN);       // Personal pronoun
        TAG_TYPES.put(PRP$, TYPE_PRONOUN);      // Possessive pronoun
        TAG_TYPES.put(RB, TYPE_ADVERB);         // Adverb
        TAG_TYPES.put(RBR, TYPE_ADVERB);	// Adverb, comparative
        TAG_TYPES.put(RBS, TYPE_ADVERB);        // Adverb, superlative
        TAG_TYPES.put(RP, TYPE_PREPOSITION);    // Particle: aboard about across along apart around aside at away back before behind...
        TAG_TYPES.put(SYM, null);               // Symbol: % & ' '' ''. ) ). * + ,. < = > @ A[fj] U.S U.S.S.R * ** ***
        TAG_TYPES.put(TO, TYPE_CONNECT);        // to
        TAG_TYPES.put(UH, TYPE_INTERJECTION);   // Interjection: Goodbye Goody Gosh Wow Jeepers Jee-sus Hubba Hey Kee-reist Oops amen...
        TAG_TYPES.put(VB, TYPE_VERB);           // Verb, base form
        TAG_TYPES.put(VBD, TYPE_VERB);          // Verb, past tense
        TAG_TYPES.put(VBG, TYPE_VERB);          // Verb, gerund or present participle
        TAG_TYPES.put(VBN, TYPE_VERB);          // Verb, past participle
        TAG_TYPES.put(VBP, TYPE_VERB);          // Verb, non-3rd person singular present
        TAG_TYPES.put(VBZ, TYPE_VERB);          // Verb, 3rd person singular present
        TAG_TYPES.put(WDT, TYPE_DETERMINER);    // Wh-determiner
        TAG_TYPES.put(WP, TYPE_PRONOUN);        // Wh-pronoun
        TAG_TYPES.put(WP$, TYPE_PRONOUN);       // Possessive wh-pronoun
        TAG_TYPES.put(WRB, TYPE_ADVERB);        // Wh-adverb
        TAG_TYPES.put("#", TYPE_PUNCTUATION);
        TAG_TYPES.put("$", TYPE_PUNCTUATION);
        TAG_TYPES.put(",", TYPE_PUNCTUATION);
        TAG_TYPES.put(".", TYPE_PUNCTUATION);  // used for all sentence-ending punctuation
        TAG_TYPES.put(":", TYPE_PUNCTUATION);  // used for colons, semicolons and ellipses
        TAG_TYPES.put("(", TYPE_PUNCTUATION);  // used for all forms of opening parenthesis
        TAG_TYPES.put(")", TYPE_PUNCTUATION);  // used for all forms of closing parenthesis
        TAG_TYPES.put("``", TYPE_PUNCTUATION); // used for all forms of opening quote
        TAG_TYPES.put("''", TYPE_PUNCTUATION); // used for all forms of closing quote

        Collection<String> values = TAG_TYPES.values();
        tagTypes = new ArrayList<>();
        for (String value : values) {
            if (value != null && !tagTypes.contains(value)) {
                tagTypes.add(value);
            }
        }
    }

    public static List<String> getTags() {
        return tagNames;
    }

    public static List<String> getTypes() {
        return tagTypes;
    }

    public static boolean isValidTag(String tag) {
        return TAG_NAMES.containsKey(tag);
    }

    public static String getNameOfTag(String tag) {
        if (TAG_NAMES.containsKey(tag)) {
            return TAG_NAMES.get(tag);
        }
        return null;
    }

    public static String getTypeOfTag(String tag) {
        if (TAG_TYPES.containsKey(tag)) {
            return TAG_TYPES.get(tag);
        }
        return null;
    }
}
