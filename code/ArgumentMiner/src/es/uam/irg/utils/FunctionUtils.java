/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.utils;

import es.uam.irg.io.IOManager;
import es.uam.irg.nlp.am.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ansegura
 */
public class FunctionUtils {

    /**
     *
     * @param <T>
     * @param array
     * @param delimiter
     * @return
     */
    public static <T> String arrayToString(T[] array, String delimiter) {
        String result = "";

        if (array != null && array.length > 0) {
            StringBuilder sb = new StringBuilder();

            for (T item : array) {
                sb.append(item.toString()).append(delimiter);
            }

            result = sb.deleteCharAt(sb.length() - 1).toString();
        }

        return result;
    }

    /**
     *
     * @param array
     * @return
     */
    public static List<String> createListFromText(String array) {
        array = array.replace("[", "").replace("]", "");
        return new ArrayList<>(Arrays.asList(array.split(",")));
    }

    /**
     *
     * @param dbType
     * @return
     */
    public static Map<String, Object> getDatabaseConfiguration(String dbType) {
        Map<String, Object> setup = null;

        if (dbType.equals(Constants.MYSQL_DB)) {
            setup = IOManager.readYamlFile(Constants.MSQL_SETUP_FILEPATH);
        } else if (dbType.equals(Constants.MONGO_DB)) {
            setup = IOManager.readYamlFile(Constants.MDB_SETUP_FILEPATH);
        }

        return setup;
    }

    /**
     *
     * @param <T>
     * @param array
     * @param startIx
     * @param endIndex
     * @return
     * @throws java.lang.Exception
     */
    public static <T> T[] getSubArray(T[] array, int startIx, int endIndex) throws Exception {
        T[] newArray = null;

        if (startIx >= 0 && endIndex <= array.length) {
            newArray = Arrays.copyOfRange(array, startIx, endIndex);
        }

        return newArray;
    }

    /**
     *
     * @param map
     * @return
     */
    public static Map<String, Integer> sortMapByValue(Map<String, Integer> map) {
        LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

        // Use Comparator.reverseOrder() for reverse ordering
        map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        return reverseSortedMap;
    }

}
