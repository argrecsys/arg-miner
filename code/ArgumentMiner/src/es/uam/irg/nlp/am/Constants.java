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
