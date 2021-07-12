/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.utils;

import java.util.Arrays;

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
    
}
