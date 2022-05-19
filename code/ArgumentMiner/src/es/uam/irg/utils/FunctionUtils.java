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
package es.uam.irg.utils;

import es.uam.irg.io.IOManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author ansegura
 */
public class FunctionUtils {

    // Class constants
    public static final String MONGO_DB = "MONGO_DB";
    public static final String MYSQL_DB = "MYSQL_DB";
    private static final String MSQL_SETUP_FILEPATH = "Resources/config/msql_setup.yaml";
    private static final String MDB_SETUP_FILEPATH = "Resources/config/mdb_setup.yaml";

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

        if (dbType.equals(MYSQL_DB)) {
            setup = IOManager.readYamlFile(MSQL_SETUP_FILEPATH);
        } else if (dbType.equals(MONGO_DB)) {
            setup = IOManager.readYamlFile(MDB_SETUP_FILEPATH);
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
     * @param <T>
     * @param map
     * @return
     */
    public static <T> List<T> listFromMapKeys(Map<T, ?> map) {
        List<T> list = new ArrayList<>(map.keySet());
        return list;
    }

    /**
     *
     * @param <T>
     * @param set
     * @return
     */
    public static <T> List<T> listFromSet(Set<T> set) {
        List<T> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    /**
     *
     * @param list
     * @param sep
     * @return
     */
    public static String listToString(List<String> list, String sep) {
        return list.stream().collect(Collectors.joining(sep + "," + sep, sep, sep));
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
