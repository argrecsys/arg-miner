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
    public static final String MONGO_DB = "MONGO_DB";
    public static final String MYSQL_DB = "MYSQL_DB";
    public static final String NGRAMS_DELIMITER = "-";
    public static final String OUTPUT_FOLDER = "../../results/";
    public static final String ARGUMENTS_FILEPATH = OUTPUT_FOLDER + "arguments.json";
    public static final String RESOURCES_FOLDER = "Resources/";
    public static final String MSQL_SETUP_FILEPATH = RESOURCES_FOLDER + "config/msql_setup.yaml";
    public static final String MDB_SETUP_FILEPATH = RESOURCES_FOLDER + "config/mdb_setup.yaml";
    public static final String PARAMS_FILEPATH = RESOURCES_FOLDER + "config/params.json";
    public static final String LEXICON_FILEPATH = RESOURCES_FOLDER + "dataset/argument_lexicon_{}.csv";
    public static final String SPANISH_PROPERTIES = RESOURCES_FOLDER + "config/StanfordCoreNLP-spanish.properties";
    public static final String STOPWORDS_FILEPATH = RESOURCES_FOLDER + "stopwords/{}.txt";

}
