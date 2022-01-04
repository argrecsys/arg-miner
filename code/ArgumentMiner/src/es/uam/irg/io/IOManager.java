/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.io;

import es.uam.irg.nlp.am.arguments.ArgumentLinkerManager;
import es.uam.irg.nlp.am.Constants;
import es.uam.irg.nlp.am.arguments.ArgumentLinker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author ansegura
 */
public class IOManager implements Constants {

    private static final HashSet<String> INVALID_LINKERS = new HashSet(Arrays.asList("e", "ni", "o", "y"));
    private static final HashSet<String> VALID_LINKERS = new HashSet();

    /**
     *
     * @param lang
     * @param verbose
     * @return
     */
    public static ArgumentLinkerManager readLinkerTaxonomy(String lang, boolean verbose) {
        ArgumentLinkerManager linkers = new ArgumentLinkerManager();
        String lexiconFilepath = LEXICON_FILEPATH.replace("{}", lang);

        try {
            // Get the file
            File csvFile = new File(lexiconFilepath);

            // Check if the specified file exists or not
            if (csvFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                String row;
                String category;
                String subCategory;
                String relationType;
                String linker;

                reader.readLine();
                while ((row = reader.readLine()) != null) {
                    String[] data = row.split(",");

                    if (data.length == 6) {
                        category = data[2];
                        subCategory = data[3];
                        relationType = data[4];
                        linker = data[5];

                        // If the linker is a valid one and also not invalid... then add it
                        if ((VALID_LINKERS.isEmpty() || VALID_LINKERS.contains(linker)) && (!INVALID_LINKERS.contains(linker))) {
                            linkers.addLinker(category, subCategory, relationType, linker);
                        }
                    }
                }

                reader.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (verbose) {
            System.out.println(">> Taxonomy:");
            Map<String, Map<String, List<ArgumentLinker>>> taxonomy = linkers.getTaxonomy();
            taxonomy.entrySet().forEach(entry -> {
                System.out.println(entry.getKey());
                for (Map.Entry<String, List<ArgumentLinker>> subentry : entry.getValue().entrySet()) {
                    System.out.println("  " + subentry.getKey());
                    List<ArgumentLinker> items = subentry.getValue();

                    for (int i = 0; i < items.size(); i++) {
                        System.out.println("    " + items.get(i).linker);
                    }
                }
            });

            List<ArgumentLinker> lexicon = linkers.getLexicon(true);
            System.out.println(">> Lexicon: " + lexicon.size());
            for (int i = 0; i < lexicon.size(); i++) {
                System.out.format("Linker -> %s \n", lexicon.get(i).toString());
            }
        }

        return linkers;
    }

    /**
     *
     * @param lang
     * @param verbose
     * @return
     */
    public static HashSet<String> readStopwordList(String lang, boolean verbose) {
        HashSet<String> stopwords = new HashSet<>();
        String language = (lang.equals(LANG_EN) ? "english" : "spanish");
        String stopwordsFilepath = STOPWORDS_FILEPATH.replace("{}", language);

        try {
            // Get the file
            File txtFile = new File(stopwordsFilepath);

            // Check if the specified file exists or not
            if (txtFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(txtFile));
                String word;

                while ((word = reader.readLine()) != null) {
                    stopwords.add(word);
                }

                reader.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (verbose) {
            System.out.println(">> Stopwords: " + stopwords.size());
        }

        return stopwords;
    }

    /**
     *
     * @param filepath
     * @return
     */
    public static Map<String, Object> readYamlFile(String filepath) {
        Map<String, Object> data = null;

        try {
            // Get the file
            File yamlFile = new File(filepath);

            // Check if the specified file exists or not
            if (yamlFile.exists()) {
                InputStream inputStream = new FileInputStream(yamlFile);
                Yaml yaml = new Yaml();
                data = (Map<String, Object>) yaml.load(inputStream);
            }

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    /**
     *
     * @param source
     * @param filepath
     * @return
     */
    public static boolean saveDomToXML(DOMSource source, String filepath) {
        boolean result = false;

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult stream = new StreamResult(new File(filepath));
            transformer.transform(source, stream);
            result = true;

        } catch (TransformerException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    /**
     *
     * @param data
     * @param filepath
     * @return
     */
    public static boolean saveStringToJson(String data, String filepath) {
        boolean result = false;

        FileWriter writer;

        try {
            writer = new FileWriter(filepath);
            writer.write(data);
            writer.close();
            result = true;

        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
