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
package es.uam.irg.nlp.corenlp.syntax;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.*;
import es.uam.irg.nlp.am.Constants;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import es.uam.irg.nlp.corenlp.syntax.treebank.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SyntacticAnalyzer
 *
 * Performs a syntactic analysis on the sentences of a given text, storing their
 * syntactic treebanks, and their nouns and their complements and dependences.
 *
 * @author Ivan Cantador, ivan.cantador@uam.es
 * @version 1.0 - 16/03/2017
 */
public class SyntacticAnalyzer {

    private static StanfordCoreNLP pipeline;

    static {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(Constants.SPANISH_PROPERTIES));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SyntacticAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SyntacticAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        pipeline = new StanfordCoreNLP(props);
    }

    public static List<SyntacticallyAnalyzedSentence> analyzeSentences(String text) throws Exception {
        List<SyntacticallyAnalyzedSentence> _sentences = new ArrayList<>();

        text = text.replace("...", ".");
        text = text.replace(".", " . ");
        text = text.replace(" .", ".");
        text = text.replace("  ", " ");

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> analyzedSentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (analyzedSentences != null && !analyzedSentences.isEmpty()) {
            for (CoreMap analyzedSentence : analyzedSentences) {
                String sentence = analyzedSentence.toString();

                Tree tree = analyzedSentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                String treeDescription = tree.skipRoot().pennString();
                SyntacticTreebank treebank = new SyntacticTreebank(treeDescription, true);

                SyntacticallyAnalyzedSentence _sentence = new SyntacticallyAnalyzedSentence(sentence, treebank);
                _sentences.add(_sentence);
            }
        }
        return _sentences;
    }

    public static void main(String[] args) {
        try {
            System.setErr(new PrintStream(new OutputStream() {  // this is to hide the meesages from CORE NLP
                public void write(int b) {
                }
            }));

            String text = "Tu mismo lo dices; el incremento de ciclistas hace que haya mas siniestros en general. Hay una necesidad real de crear una red de vias ciclistas porque todo apunta a que siga incrementando. El tramo que digo si existe, buscalo en street view, son mas de 300 metros de subida y los coches que vayan detras de ti, supuestamente no te pueden adelantar por los 2 carriles que se suman porque hay una linea continua que se lo impide hasta el semaforo. Has visto el nuevo carril bici en la casa de campo, no esta en la acera se separa del trafico y ocupa muy poco. En la cuesta de San Vicente se podria unificar carril bus con trafico en ese tramo, por ejemplo. En Madrid no hay casi rotondas, la mayoria son glorietas, los ciclistas respetarian semaforos igual que los coches. Me cuesta creer que utilices la bici por Madrid y estes en contra de esta medida, si visitas otras ciudades como Barcelona te das cuenta que aqui los ciclistas estamos vendidos.";
            List<SyntacticallyAnalyzedSentence> analyzedSentences = SyntacticAnalyzer.analyzeSentences(text);
            for (SyntacticallyAnalyzedSentence analyzedSentence : analyzedSentences) {
                SyntacticTreebank tree = analyzedSentence.getTreebank();
                System.out.println(tree.getChildrenIdsOf(tree.getRootNode()));
                //System.out.println(analyzedSentence.getAnalysisData());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
