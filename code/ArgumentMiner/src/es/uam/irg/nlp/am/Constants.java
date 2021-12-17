/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am;

/**
 *
 * @author ansegura
 */
public interface Constants {

    // Public system constants
    public static final String LANG_EN = "en";
    public static final String LANG_ES = "es";
    public static final String LEXICON_FILEPATH = "Resources/dataset/argument_lexicon_{}.csv";
    public static final String MDB_SETUP_FILEPATH = "Resources/config/mdb_setup.yaml";
    public static final String MONGO_DB = "MONGO_DB";
    public static final String MSQL_SETUP_FILEPATH = "Resources/config/msql_setup.yaml";
    public static final String MYSQL_DB = "MYSQL_DB";
    public static final String NGRAMS_DELIMITER = "-";
    public static final String OUTPUT_FOLDER = "../../results/";
    public static final String ARGUMENTS_FILEPATH = OUTPUT_FOLDER + "arguments.json";
    public static final String RECOMMENDATIONS_FILEPATH = OUTPUT_FOLDER + "recommendations_{}.xml";
    public static final String SPANISH_PROPERTIES = "Resources/config/StanfordCoreNLP-spanish.properties";
    public static final String STOPWORDS_FILEPATH = "Resources/stopwords/{}.txt";

}
