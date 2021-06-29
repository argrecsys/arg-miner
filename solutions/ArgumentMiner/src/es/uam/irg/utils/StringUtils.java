package es.uam.irg.utils;

import java.util.Arrays;
import java.util.LinkedList;

public class StringUtils {
    
    // Public contants
    public static final String EMPTY = "";
    
    // Private contants
    private static final String PLAIN_ASCII
            = "AaEeIiOoUu" // grave
            + "AaEeIiOoUuYy" // acute
            + "AaEeIiOoUuYy" // circumflex
            + "AaEeIiOoUuYy" // tilde
            + "AaEeIiOoUuYy" // umlaut
            + "Aa" // ring
            + "Cc" // cedilla
            ;
    private static final String UNI_CODE
            = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9" // grave
            + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD" // acute
            + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" // circumflex
            + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" // tilde
            + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" // umlaut
            + "\u00C5\u00E5" // ring
            + "\u00C7\u00E7" // cedilla
            ;

    /**
     * Converts a non ASCII string to an ASCII string.
     *
     * @param s - the string which characters are going to be converted
     *
     * @return a string which all its characters are ASCII
     */
    public static String toASCII(String s) {
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            int pos = UNI_CODE.indexOf(c);
            if (pos > -1) {
                sb.append(PLAIN_ASCII.charAt(pos));
            } else if ((int) c < 256) {
                sb.append(c);
            } else {
                //sb.append("?");
            }
        }
        return sb.toString();
    }

    /**
     * Computes the Levensthein distance between two given strings.
     *
     * @param s - the first string
     * @param t - the second string
     *
     * @return the Levensthein distance between s and t
     */
    public static int levenshteinDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1    
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt(i - 1);

            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);

                // Step 5
                if (s_i == t_j) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6
                d[i][j] = minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
            }
        }

        // Step 7
        return d[n][m];
    }

    public static String splitCamelCaseString(String s) {
        LinkedList<String> tokens = _splitCamelCaseString(s);
        String s2 = "";
        for (String token : tokens) {
            s2 += token.toLowerCase() + " ";
        }
        s2 = s2.trim();
        return s2;
    }
    
    /**
     * <p>Left pad a String with spaces (' ').</p>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @return left padded String or original String if no padding is necessary, {@code null} if null String input
     */
    public static String leftPad(String str, int size) {
        return leftPad(str, size, ' ');
    }
    
    /**
     * Left pad a String with spaces (' ').
     * 
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padChar  the character to pad with
     * @return left padded String or original String if no padding is necessary, {@code null} if null String input
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        return repeat(padChar, size).concat(str);
    }
    
    /**
     * 
     * @param a
     * @param b
     * @param c
     * @return 
     */
    private static int minimum(int a, int b, int c) {
        int mi;

        mi = a;
        if (b < mi) {
            mi = b;
        }
        if (c < mi) {
            mi = c;
        }

        return mi;
    }
    
    /**
     * Accept a string, like aCamelString
     * @param s
     * @return a list containing strings, in this case, [a, Camel, String]
     */
    private static LinkedList<String> _splitCamelCaseString(String s) {
        LinkedList<String> result = new LinkedList<>();
        for (String w : s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            result.add(w);
        }
        return result;
    }
    
    /**
     * Returns padding using the specified delimiter repeated to a given length.
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat(char ch, int repeat) {
        if (repeat <= 0) {
            return EMPTY;
        }
        char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }

}
