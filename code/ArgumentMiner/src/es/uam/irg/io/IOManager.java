/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.io;

import es.uam.irg.nlp.am.arguments.ArgumentLinkerManager;
import es.uam.irg.nlp.am.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
    
    /**
     * 
     * @param lang
     * @param verbose
     * @return 
     */
    public static ArgumentLinkerManager readLinkerTaxonomy(String lang, boolean verbose) {
        ArgumentLinkerManager linkers = new ArgumentLinkerManager();
        int linkerIndex = -1;
        
        if (LANG_EN.equals(lang))
            linkerIndex = 3;
        else if (LANG_ES.equals(lang))
            linkerIndex = 4;
        
        if (linkerIndex > -1) {
            try {
                // Get the file
                File csvFile = new File(TAXONOMY_FILEPATH);
                
                // Check if the specified file exists or not
                if (csvFile.exists()) {
                    BufferedReader reader = new BufferedReader( new FileReader(csvFile));
                    String row;
                    String category;
                    String subCategory;
                    String relationType;
                    String linker;
                    
                    reader.readLine();
                    while ((row = reader.readLine()) != null) {
                        String[] data = row.split(",");

                        if (data.length == 5) {
                            category = data[0];
                            subCategory = data[1];
                            relationType = data[2];
                            linker = data[linkerIndex];
                            linkers.addLinker(category, subCategory, relationType, linker);
                        }
                    }
                    
                    reader.close();
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (verbose) {
            System.out.println(">> List of argument linkers: " + linkers.getNLinkers());
            for (int i = 0; i < linkers.getNLinkers(); i++) {
                System.out.format("Linker -> %s \n", linkers.getLinker(i).getString());
            }
        }
        
        return linkers;
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
        
        } catch (FileNotFoundException ex) {
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
