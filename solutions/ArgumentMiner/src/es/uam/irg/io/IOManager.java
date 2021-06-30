/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.io;

import es.uam.irg.nlp.am.arguments.ArgumentLinker;
import es.uam.irg.nlp.am.arguments.ArgumentLinkerList;
import es.uam.irg.utils.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static ArgumentLinkerList readLinkerTaxonomy(String lang, boolean verbose) {
        ArgumentLinkerList linkers = new ArgumentLinkerList();
        File csvFile;
        int linkerIndex = -1;
        
        if ("en".equals(lang))
            linkerIndex = 3;
        else if ("es".equals(lang))
            linkerIndex = 4;
        
        if (linkerIndex > -1) {
            try {
                // Get the file
                csvFile = new File(TAXONOMY_FILEPATH);
                
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
        
        if (verbose && linkers != null) {
            System.out.println(">> List of argument linkers [" + linkers.getSize() + "]:");
            for (int i = 0; i < linkers.getSize(); i++) {
                ArgumentLinker linker = linkers.getLinker(i);
                System.out.format("Linker -> %s \n", linker.getString());
            }
        }
        
        return linkers;
    }
    
}
