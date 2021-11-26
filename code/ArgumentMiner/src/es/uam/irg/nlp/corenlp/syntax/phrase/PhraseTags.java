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
package es.uam.irg.nlp.corenlp.syntax.phrase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PhraseTags
 *
 * Manages the phrase tags defined in the Penn Treebank Project
 * http://www.surdeanu.info/mihai/teaching/ista555-fall13/readings/PennTreebankConstituents.html
 * http://repository.upenn.edu/cgi/viewcontent.cgi?article=1603&context=cis_reports
 *
 * @author Ivan Cantador, ivan.cantador@uam.es
 * @version 1.0 - 16/03/2017
 */
public abstract class PhraseTags {
    // Clause Level
    public static final String S = "S";             // Simple declarative clause, i.e. one that is not introduced by a (possible empty) subordinating conjunction or a wh-word and that does not exhibit subject-verb inversion.
    public static final String SBAR = "SBAR";       // Clause introduced by a (possibly empty) subordinating conjunction.
    public static final String SBARQ = "SBARQ";     // Direct question introduced by a wh-word or a wh-phrase. Indirect questions and relative clauses should be bracketed as SBAR, not SBARQ.
    public static final String SINV = "SINV";       // Inverted declarative sentence, i.e. one in which the subject follows the tensed verb or modal.
    public static final String SQ = "SQ";           // Inverted yes/no question, or main clause of a wh-question, following the wh-phrase in SBARQ.
    // Phrase Level
    public static final String ADJP = "ADJP";       // Adjective Phrase.
    public static final String ADVP = "ADVP";       // Adverb Phrase.
    public static final String CONJP = "CONJP";     // Conjunction Phrase.
    public static final String FRAG = "FRAG";       // Fragment.
    public static final String INTJ = "INTJ";       // Interjection. Corresponds approximately to the part-of-speech tag UH.
    public static final String LST = "LST";         // List marker. Includes surrounding punctuation.
    public static final String NAC = "NAC";         // Not a Constituent; used to show the scope of certain prenominal modifiers within an NP.
    public static final String NP = "NP";           // Noun Phrase.
    public static final String NX = "NX";           // Used within certain complex NPs to mark the head of the NP. Corresponds very roughly to N-bar level but used quite differently.
    public static final String PP = "PP";           // Prepositional Phrase.
    public static final String PRN = "PRN";         // Parenthetical.
    public static final String PRT = "PRT";         // Particle. Category for words that should be tagged RP.
    public static final String QP = "QP";           // Quantifier Phrase (i.e. complex measure/amount phrase); used within NP.
    public static final String RRC = "RRC";         // Reduced Relative Clause.
    public static final String UCP = "UCP";         // Unlike Coordinated Phrase.
    public static final String VP = "VP";           // Verb Phrase.
    public static final String WHADJP = "WHADJP";   // Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot.
    public static final String WHADVP = "WHADVP";   // Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing a wh-adverb such as how or why.
    public static final String WHNP = "WHNP";       // Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing some wh-word, e.g. who, which book, whose daughter, none of which, or how many leopards.
    public static final String WHPP = "WHPP";       // Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase (such as of which or by whose authority) that either introduces a PP gap or is contained by a WHNP.
    public static final String X = "X";             // Unknown, uncertain, or unbracketable. X is often used for bracketing typos and in bracketing the...the-constructions.
    // -----
    public static final String TYPE_CLAUSE = "clause";
    public static final String TYPE_QUESTION_CLAUSE = "question clause";
    public static final String TYPE_NOUN_PHRASE = "noun phrase";
    public static final String TYPE_PREPOSITIONAL_PHRASE = "prepositional phrase";
    public static final String TYPE_ADJECTIVE_PHRASE = "adjetive phrase";
    public static final String TYPE_VERB_PHRASE = "verb phrase";
    public static final String TYPE_ADVERB_PHRASE = "adverb phrase";
    public static final String TYPE_CONNECT_PHRASE = "connect phrase";
    public static final String TYPE_INTERJECTION_PHRASE = "interjection phrase";
    public static final String TYPE_QUESTION_PHRASE = "question phrase";
    // -----
    private static final Map<String, String> TAG_NAMES;
    private static final Map<String, String> TAG_TYPES;
    private static final List<String> tagNames;
    private static final List<String> tagTypes;

    static {
        TAG_NAMES = new HashMap<>();

        TAG_NAMES.put(S, "Simple declarative clause");
        TAG_NAMES.put(SBAR, "Clause introduced by a (possibly empty) subordinating conjunction");
        TAG_NAMES.put(SBARQ, "Direct question introduced by a wh-word or a wh-phrase");
        TAG_NAMES.put(SINV, "Inverted declarative sentence");
        TAG_NAMES.put(SQ, "Inverted yes/no question, or main clause of a wh-question, following the wh-phrase in SBARQ");
        TAG_NAMES.put(ADJP, "Adjective Phrase");
        TAG_NAMES.put(ADVP, "Adverb Phrase");
        TAG_NAMES.put(CONJP, "Conjunction Phrase");
        TAG_NAMES.put(FRAG, "Fragment");
        TAG_NAMES.put(INTJ, "Interjection");
        TAG_NAMES.put(LST, "List marker");
        TAG_NAMES.put(NAC, "Not a Constituent; used to show the scope of certain prenominal modifiers within an NP");
        TAG_NAMES.put(NP, "Noun Phrase");
        TAG_NAMES.put(NX, "Used within certain complex NPs to mark the head of the NP");
        TAG_NAMES.put(PP, "Prepositional Phrase");
        TAG_NAMES.put(PRN, "Parenthetical");
        TAG_NAMES.put(PRT, "Particle. Category for words that should be tagged RP");
        TAG_NAMES.put(QP, "Quantifier Phrase (i.e. complex measure/amount phrase); used within NP");
        TAG_NAMES.put(RRC, "Reduced Relative Clause");
        TAG_NAMES.put(UCP, "Unlike Coordinated Phrase");
        TAG_NAMES.put(VP, "Verb Phrase");
        TAG_NAMES.put(WHADJP, "Wh-adjective Phrase");
        TAG_NAMES.put(WHADVP, "Wh-adverb Phrase");
        TAG_NAMES.put(WHNP, "Wh-noun Phrase");
        TAG_NAMES.put(WHPP, "Wh-prepositional Phrase");
        TAG_NAMES.put(X, "Unknown, uncertain, or unbracketable");

        tagNames = new ArrayList<>(TAG_NAMES.keySet());

        TAG_TYPES = new HashMap<>();

        TAG_TYPES.put(S, TYPE_CLAUSE);                  // Simple declarative clause
        TAG_TYPES.put(SBAR, TYPE_CLAUSE);               // Clause introduced by a (possibly empty) subordinating conjunction
        TAG_TYPES.put(SBARQ, TYPE_QUESTION_CLAUSE);     // Direct question introduced by a wh-word or a wh-phrase
        TAG_TYPES.put(SINV, TYPE_CLAUSE);               // Inverted declarative sentence
        TAG_TYPES.put(SQ, TYPE_QUESTION_CLAUSE);        // Inverted yes/no question, or main clause of a wh-question, following the wh-phrase in SBARQ
        TAG_TYPES.put(ADJP, TYPE_ADJECTIVE_PHRASE);     // Adjective Phrase
        TAG_TYPES.put(ADVP, TYPE_ADVERB_PHRASE);        // Adverb Phrase
        TAG_TYPES.put(CONJP, TYPE_CONNECT_PHRASE);      // Conjunction Phrase
        TAG_TYPES.put(FRAG, TYPE_CLAUSE);               // Fragment
        TAG_TYPES.put(INTJ, TYPE_INTERJECTION_PHRASE);  // Interjection
        TAG_TYPES.put(LST, null);                       // List marker
        TAG_TYPES.put(NAC, null);                       // Not a Constituent; used to show the scope of certain prenominal modifiers within an NP
        TAG_TYPES.put(NP, TYPE_NOUN_PHRASE);            // Noun Phrase
        TAG_TYPES.put(NX, TYPE_NOUN_PHRASE);            // Used within certain complex NPs to mark the head of the NP
        TAG_TYPES.put(PP, TYPE_PREPOSITIONAL_PHRASE);   // Prepositional Phrase
        TAG_TYPES.put(PRN, TYPE_CLAUSE);                // Parenthetical
        TAG_TYPES.put(PRT, null);                       // Particle. Category for words that should be tagged RP
        TAG_TYPES.put(QP, null);                        // Quantifier Phrase (i.e. complex measure/amount phrase); used within NP
        TAG_TYPES.put(RRC, TYPE_CLAUSE);                // Reduced Relative Clause
        TAG_TYPES.put(UCP, TYPE_CONNECT_PHRASE);        // Unlike Coordinated Phrase
        TAG_TYPES.put(VP, TYPE_VERB_PHRASE);            // Verb Phrase
        TAG_TYPES.put(WHADJP, TYPE_QUESTION_PHRASE);    // Wh-adjective Phrase
        TAG_TYPES.put(WHADVP, TYPE_QUESTION_PHRASE);    // Wh-adverb Phrase
        TAG_TYPES.put(WHNP, TYPE_QUESTION_PHRASE);      // Wh-noun Phrase
        TAG_TYPES.put(WHPP, TYPE_QUESTION_PHRASE);      // Wh-prepositional Phrase
        TAG_TYPES.put(X, null);                         // Unknown, uncertain, or unbracketable

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

    public static boolean compactedTags(String tag1, String tag2) {
        if (tag1 != null && tag2 != null) {
            if (tag1.equals(tag2)) {
                return true;
            }
            if (tag1.equals(NP) && tag2.equals(ADJP)) {
                return true;
            }
            if (tag1.equals(VP) && tag2.equals(ADJP)) {
                return true;
            }
            if (tag1.equals(SBAR) && tag2.equals(S)) {
                return true;
            }
            if (tag1.equals(VP) && tag2.equals(NP)) {  // for certain questions, e.g., Which companies did Steve Jobs found?
                return true;
            }
            if (tag1.equals(SBAR) && tag2.equals(WHNP)) {
                return true;
            }
            if (tag1.equals(FRAG) && tag2.equals(S)) {
                return true;
            }
            if (tag1.equals(PP) && tag2.equals(NP)) {
                return true;
            }
            // Questions
            if (tag1.equals(SQ) && tag2.equals(S)) {
                return true;
            }
            if (tag1.equals(SQ) && tag2.equals(SBAR)) {
                return true;
            }
            if (tag1.equals(SQ) && tag2.equals(VP)) {
                return true;
            }
            if (tag1.equals(SQ) && tag2.equals(PRN)) {
                return true;
            }
        }
        return false;
    }
}
