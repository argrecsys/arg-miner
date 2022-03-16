/**
 * Copyright 2022
 * Iván Cantador
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
package es.uam.irg.nlp.textproc;

import es.uam.irg.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Natural language processing class.
 */
public class TextProcessor {

    // Class contants
    public static final String CLEAN_BOTH = "both";
    public static final String CLEAN_RIGHT = "right";
    private static final List<String> SYMBOLS = new ArrayList<>(List.of(".", ",", ":", ";", "'", "_", "/", "\\", "|", "¿", "?", "¡", "!", "(", ")", "[", "]", "{", "}", "<", ">", "+", "*", "#", "@", "%", "&", "º", "·", "~", "»", "“", "”"));
    private static final List<String> UPPERCASE_ALLOWED = new ArrayList<>(List.of("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "PP", "PSOE", "PODEMOS", "IU", "VPPB", "LGTB", "DVD", "CD", "CIF", "DDHH", "PAU", "3G", "4G", "5G", "3G4G", "GEA21", "LPH", "PAS", "PDI", "NPI", "CVP", "UBER", "INE", "PAUS", "AZCA", "NASA", "VPPL", "ABG", "IBI", "ZP", "IVA", "REE", "IC", "EB4CTV", "OCDE", "VTCS", "CSIF", "EQUO", "BCN", "EMV", "EMVS", "LRHL", "CCEE", "RRHH", "AEAT", "BOE", "BOCM", "WC", "GEA", "CSIC", "CVP", "CNMV", "MIT", "LAU", "URM", "VALDECAM", "VSC", "JMD", "CRTM", "PGOU", "SDDR", "ESO", "LOU", "PGE", "CNT", "IRPF", "IIVTNU", "ONO", "IES", "CO2", "VMP", "EU", "ONU", "OTAN", "BIC", "CEOE", "COPE", "DNI", "MM", "CES", "UAM", "UPM", "UCM", "URJC", "UC3M", "UAH", "OMS", "EMF", "TSJ", "WIFI", "RAE", "CNT", "FB", "POU", "APA", "O2", "AAVV", "ERTE", "ERTES", "CEM", "XXX", "UGT", "SIDA", "CCOO", "TV", "BBVA", "BANKIA", "BICIMADRID", "BICIMAD", "CCAA", "CAM", "UK", "VOX", "ONG", "RTVE", "A1", "A2", "A3", "A4", "A5", "A6", "M607", "A-1", "A-2", "A-3", "A-4", "A-5", "A-6", "M-607", "USA", "EEUU", "EE.UU", "SOS", "AM", "FM", "SER", "DGT", "APR", "PMR", "PVP", "ICADE", "UME", "IFEMA", "ADIF", "RENFE", "VTC", "EMT", "ITV", "ADN", "AIRBNB", "M30", "M40", "M50", "M-30", "M-40", "M-50"));

    /**
     *
     * @param text
     * @param direction
     * @return
     */
    public static String cleanText(String text, String direction) {
        String newText = rightCleanText(text);
        if (direction.equals(CLEAN_BOTH)) {
            newText = StringUtils.reverse(rightCleanText(StringUtils.reverse(newText)));
        }
        return newText.trim();
    }

    /**
     *
     * @param text
     * @return
     */
    public static String process(String text) {
        return processForSpanish(text, new ArrayList<>());
    }

    /**
     *
     * @param text
     * @param locations
     * @return
     */
    public static String process(String text, List<String> locations) {
        return processForSpanish(text, locations);
    }

    /**
     * Processes the text (data quality) for Spanish.
     *
     * @param text
     * @param locations
     * @return
     */
    private static String processForSpanish(String text, List<String> locations) {
        String t = "";

        StringTokenizer tokenizer = new StringTokenizer(text.trim(), " ()");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (!token.startsWith("http") && !token.startsWith("www")) {
                if (StringUtils.isAllInUppercase(token)) {
                    token = toLowerCase2(token, locations);
                }
                t += token + " ";
            } else {
                if (token.endsWith(".")) {
                    t += ".";
                } else if (token.endsWith(",")) {
                    t += ",";
                } else if (token.endsWith(":")) {
                    t += ":";
                } else if (token.endsWith(";")) {
                    t += ";";
                }
                t += " ";
            }
        }
        t = t.trim();

        if (t.length() < 2) {
            return t.toUpperCase();
        }

        t = Character.toUpperCase(t.charAt(0)) + t.substring(1);
        t = t + ".";

        t = t.replace("'", "");
        t = t.replace("`", "");
        t = t.replace("´", "");
        t = t.replace("“", "");
        t = t.replace("”", "");
        t = t.replace("*", "");

        t = t.replace("à", "á");
        t = t.replace("è", "é");
        t = t.replace("ì", "í");
        t = t.replace("ò", "ó");
        t = t.replace("ù", "ú");

        t = t.replace("À", "Á");
        t = t.replace("È", "É");
        t = t.replace("Ì", "Í");
        t = t.replace("Ò", "Ó");
        t = t.replace("Ù", "Ú");

        t = t.replace(" d ", " de ");
        t = t.replace(" q ", " que ");
        t = t.replace(" x ", " por ");
        t = t.replace(" k ", " que ");
        t = t.replace(" xa ", " para ");
        t = t.replace(" xq ", " porque ");
        t = t.replace(" pq ", " porque ");
        t = t.replace(" porq ", " porque ");
        t = t.replace(" yq eu ", " y que ");
        t = t.replace(" de el ", " del ");
        t = t.replace(" kk ", " caca ");

        t = t.replace(" D ", " de ");
        t = t.replace(" Q ", " que ");
        t = t.replace(" X ", " por ");
        t = t.replace(" K ", " que ");
        t = t.replace(" XA ", " para ");
        t = t.replace(" XQ ", " porque ");
        t = t.replace(" PQ ", " porque ");
        t = t.replace(" PORQ ", " porque ");
        t = t.replace(" Porqu ", " porque ");
        t = t.replace(" YQ EU ", " y que ");
        t = t.replace(" DE EL ", " del ");
        t = t.replace(" KK ", " caca ");

        t = t.replace("madrid", "Madrid");
        t = t.replace("etc", "etc.");
        t = t.replace("ongs.?", "ONGs?");
        t = t.replace(" ong", " ONG");
        t = t.replace("Si,", " Sí,");
        t = t.replace("A mi ", "A mí "); // a bit risky
        t = t.replace("Para mi ", "Para mí "); // a bit risky
        t = t.replace("Mas ", "Más ");
        t = t.replace("¿porque", "¿Por qué");
        t = t.replace("¿Porque", "¿Por qué");
        t = t.replace("mí parte", "mi parte");
        t = t.replace("Esta mal", "Está mal");
        t = t.replace("Esta bien", "Está bien");
        t = t.replace("esta mal", "está mal");
        t = t.replace("esta bien", "está bien");
        t = t.replace("Estan mal", "Están mal");
        t = t.replace("Estan bien", "Están bien");
        t = t.replace("estan mal", "están mal");
        t = t.replace("estan bien", "están bien");

        for (String l : locations) {
            String l2 = l;
            t = t.replace(l2, l);
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("..", ".");
        replacements.put(",,", ",");
        replacements.put("::", ":");
        replacements.put(";;", ";");
        replacements.put("??", "?");
        replacements.put("¿¿", "¿");
        replacements.put("!!", "!");
        replacements.put("¡¡", "¡");
        for (String key : replacements.keySet()) {
            while (t.contains(key)) {
                String value = replacements.get(key);
                t = t.replace(key, value);
            }
        }

        replacements = new HashMap<>();
        replacements.put(" .", ".");
        replacements.put(" ,", ",");
        replacements.put(" :", ":");
        replacements.put(" ;", ";");
        replacements.put(" ?", "?");
        replacements.put(" !", "!");

        replacements.put("¿ ", "¿");
        replacements.put("¡ ", "¡");

        replacements.put("(", " (");
        replacements.put(")", ") ");
        replacements.put("[", " [");
        replacements.put("]", "] ");
        replacements.put("{", " {");
        replacements.put("}", "} ");

        for (String key : replacements.keySet()) {
            String value = replacements.get(key);
            t = t.replace(key, value);
        }

        String t2 = "";
        for (int i = 0; i < t.length(); i++) {
            if ((t.charAt(i) == '.' || t.charAt(i) == ',' || t.charAt(i) == ':' || t.charAt(i) == ';')
                    && i + 1 < t.length()
                    && !(Character.isDigit(t.charAt(i + 1)))) {
                t2 += t.charAt(i);
                t2 += " ";
            } else {
                t2 += t.charAt(i);
            }
        }

        String t3 = "";
        for (int i = 0; i < t2.length(); i++) {
            if (t2.charAt(i) == '?' || t2.charAt(i) == '!') {
                t3 += t2.charAt(i);
                t3 += " ";
            } else {
                t3 += t2.charAt(i);
            }
        }

        replacements = new HashMap<>();
        replacements.put("  ", " ");
        replacements.put("? .", "?.");
        replacements.put("! .", "!.");
        for (String key : replacements.keySet()) {
            while (t3.contains(key)) {
                String value = replacements.get(key);
                t3 = t3.replace(key, value);
            }
        }

        t3 = t3.trim();

        // To uppercase the characters that follow a dot
        String t4 = "";
        for (int i = 0; i < t3.length(); i++) {
            if (t3.charAt(i) == '.' && i + 2 < t3.length()) {
                t4 += t3.charAt(i);
                t4 += t3.charAt(i + 1);
                t4 += Character.toUpperCase(t3.charAt(i + 2));
                i += 2;
            } else {
                t4 += t3.charAt(i);
            }
        }

        // Question and exclamation sentences
        t4 = t4.replace("¿que ", "¿Qué ");
        t4 = t4.replace("¿cual ", "¿Cuál ");
        t4 = t4.replace("¿cuales ", "¿Cuáles ");
        t4 = t4.replace("¿quien ", "¿Quien ");
        t4 = t4.replace("¿quienes ", "¿Quiénes ");
        t4 = t4.replace("¿donde ", "¿Dónde ");
        t4 = t4.replace("¿cuando ", "¿Cuándo ");
        t4 = t4.replace("¿como ", "¿Cómo ");
        t4 = t4.replace("¿cuanto ", "¿Cuánto ");
        t4 = t4.replace("¿cuantos ", "¿Cuántos ");

        t4 = t4.replace("¿Que ", "¿Qué ");
        t4 = t4.replace("¿Cual ", "¿Cuál ");
        t4 = t4.replace("¿Cuales ", "¿Cuáles ");
        t4 = t4.replace("¿Quien ", "¿Quien ");
        t4 = t4.replace("¿Quienes ", "¿Quiénes ");
        t4 = t4.replace("¿Donde ", "¿Dónde ");
        t4 = t4.replace("¿Cuando ", "¿Cuándo ");
        t4 = t4.replace("¿Como ", "¿Cómo ");
        t4 = t4.replace("¿Cuanto ", "¿Cuánto ");
        t4 = t4.replace("¿Cuantos ", "¿Cuántos ");

        Map<String, String> r1 = new HashMap<>();
        r1.put("de", "De");
        r1.put("en", "En");
        r1.put("para", "Para");
        r1.put("por", "Por");
        r1.put("sobre", "Sobre");
        r1.put("De", "De");
        r1.put("En", "En");
        r1.put("Para", "Para");
        r1.put("Por", "Por");
        r1.put("Sobre", "Sobre");

        Map<String, String> r2 = new HashMap<>();
        r2.put("que", "qué");
        r2.put("cual", "cuál");
        r2.put("cuales", "cuáles");
        r2.put("quien", "quién");
        r2.put("quienes", "quiénes");
        r2.put("donde", "dónde");
        r2.put("cuando", "cuándo");
        r2.put("como", "cómo");
        r2.put("cuanto", "cuánto");
        r2.put("cuantos", "cuántos");

        for (String w1 : r1.keySet()) {
            for (String w2 : r2.keySet()) {
                t4 = t4.replace("¿" + w1 + " " + w2 + " ", "¿" + r1.get(w1) + " " + r2.get(w2) + " ");
                t4 = t4.replace("¡" + w1 + " " + w2 + " ", "¡" + r1.get(w1) + " " + r2.get(w2) + " ");
            }
        }

        // Mispellings in frequent words
        String f[] = {" ", ".", ",", ":", ";", "?", "!", ")", "}", "]", "-", "_"};
        for (String w : f) {
            t4 = t4.replace(" arbol" + w, " árbol" + w);
            t4 = t4.replace(" arboles" + w, " árboles" + w);
            t4 = t4.replace(" autobus" + w, " autobús" + w);
            t4 = t4.replace(" bus" + w, " autobús" + w);
            t4 = t4.replace(" buses" + w, " autobues" + w);
            t4 = t4.replace(" bici" + w, " bicicleta" + w);
            t4 = t4.replace(" bicis" + w, " bicicletas" + w);
            t4 = t4.replace(" dia" + w, " día" + w);
            t4 = t4.replace(" dias" + w, " días" + w);
            t4 = t4.replace(" educacion" + w, " educación" + w);
            t4 = t4.replace(" gestion" + w, " gestión" + w);
            t4 = t4.replace(" numero" + w, " número" + w);
            t4 = t4.replace(" numeros" + w, " números" + w);
            t4 = t4.replace(" pais" + w, " páis" + w);
            t4 = t4.replace(" paises" + w, " países" + w);
            t4 = t4.replace(" razon" + w, " razón" + w);
            t4 = t4.replace(" rio" + w, " río" + w);
            t4 = t4.replace(" rios" + w, " ríos" + w);
            t4 = t4.replace(" solucion" + w, " solución" + w);
            t4 = t4.replace(" trafico" + w, " tráfico" + w);
            t4 = t4.replace(" vehiculo" + w, " vehículo" + w);
            t4 = t4.replace(" vehiculos" + w, " vehículos" + w);
            t4 = t4.replace(" via" + w, " vía" + w);
            t4 = t4.replace(" vias" + w, " vías" + w);

            t4 = t4.replace(" facil" + w, " fácil" + w);
            t4 = t4.replace(" faciles" + w, " fáciles" + w);
            t4 = t4.replace(" dificil" + w, " difícil" + w);
            t4 = t4.replace(" dificiles" + w, " difíciles" + w);
            t4 = t4.replace(" publica" + w, " pública" + w);  // a bit risky
            t4 = t4.replace(" publicas" + w, " públicas" + w);  // a bit risky
            t4 = t4.replace(" publico" + w, " público" + w);  // a bit risky
            t4 = t4.replace(" publicos" + w, " públicos" + w);  // a bit risky
            t4 = t4.replace(" ultimo" + w, " último" + w);
            t4 = t4.replace(" ultimos" + w, " últimos" + w);
            t4 = t4.replace(" unico" + w, " único" + w);
            t4 = t4.replace(" unicos" + w, " únicos" + w);

            t4 = t4.replace(" ahi" + w, " ahí" + w);
            t4 = t4.replace(" aqui" + w, " aquí" + w);
            t4 = t4.replace(" alli" + w, " allí" + w);
            t4 = t4.replace(" aca" + w, " acá" + w);
            t4 = t4.replace(" alla" + w, " allá" + w);

            t4 = t4.replace(" asi" + w, " así" + w);

            t4 = t4.replace(" algun" + w, " algún" + w);
            t4 = t4.replace(" ésto" + w, " esto" + w);
            t4 = t4.replace(" éstos" + w, " estos" + w);
            t4 = t4.replace(" mas" + w, " más" + w);  // a bit risky
            t4 = t4.replace(" demas" + w, " demás" + w);
            t4 = t4.replace(" segun" + w, " según" + w);

            //t4 = t4.replace(" esta" + w, " está" + w); // no
            t4 = t4.replace(" estan" + w, " están" + w);
            t4 = t4.replace(" habia" + w, " había" + w);
            t4 = t4.replace(" habian" + w, " habían" + w);
            t4 = t4.replace(" habra" + w, " habrá" + w);
            t4 = t4.replace(" habran" + w, " habrán" + w);
            t4 = t4.replace(" habria" + w, " habría" + w);
            t4 = t4.replace(" habrian" + w, " habrían" + w);
            t4 = t4.replace(" hara" + w, " hará" + w);
            t4 = t4.replace(" haran" + w, " harán" + w);
            t4 = t4.replace(" haria" + w, " haría" + w);
            t4 = t4.replace(" harian" + w, " harían" + w);
            t4 = t4.replace(" debera" + w, " deberá" + w);
            t4 = t4.replace(" deberan" + w, " deberán" + w);
            t4 = t4.replace(" deberia" + w, " debería" + w);
            t4 = t4.replace(" deberian" + w, " deberían" + w);
            t4 = t4.replace(" podia" + w, " podía" + w);
            t4 = t4.replace(" podian" + w, " podían" + w);
            t4 = t4.replace(" podra" + w, " podrá" + w);
            t4 = t4.replace(" podran" + w, " podrán" + w);
            t4 = t4.replace(" podria" + w, " podría" + w);
            t4 = t4.replace(" podrian" + w, " podrían" + w);
            t4 = t4.replace(" sera" + w, " será" + w);
            t4 = t4.replace(" seran" + w, " serán" + w);
            t4 = t4.replace(" seria" + w, " sería" + w); // risky
            t4 = t4.replace(" serian" + w, " serían" + w);
            t4 = t4.replace(" estaria" + w, " estaría" + w);
            t4 = t4.replace(" estarian" + w, " estarían" + w);
            t4 = t4.replace(" gustaria" + w, " gustaría" + w);
            t4 = t4.replace(" gustarian" + w, " gustarían" + w);

            t4 = t4.replace("ion" + w, "ión" + w); // risky
            t4 = t4.replace("ereis" + w, "eréis" + w);
        }

        t4 = t4.replace("ariamos", "aríamos");
        t4 = t4.replace("ariais", "aríais");
        t4 = t4.replace("arian", "arían");
        t4 = t4.replace("aramos", "áramos");
        t4 = t4.replace("asemos", "ásemos");
        t4 = t4.replace("aremos", "áremos");

        t4 = t4.replace("eriamos", "eríamos");
        t4 = t4.replace("eriais", "eríais");
        t4 = t4.replace("erian", "erían");
        t4 = t4.replace("ieramos", "iéramos");
        t4 = t4.replace("iesemos", "iésemos");
        t4 = t4.replace("ieremos", "iéremos");

        t4 = t4.replace("iriamos", "iríamos");
        t4 = t4.replace("iriais", "iríais");
        t4 = t4.replace("irian", "irían");
        t4 = t4.replace("ieramos", "iéramos");
        t4 = t4.replace("iesemos", "iésemos");
        t4 = t4.replace("ieremos", "iéremos");

        t4 = t4.replace("andolo", "ándolo");
        t4 = t4.replace("andola", "ándola");
        t4 = t4.replace("andolos", "ándolos");
        t4 = t4.replace("andolas", "ándolas");

        t4 = t4.replace("endolo", "éndolo");
        t4 = t4.replace("endola", "éndola");
        t4 = t4.replace("endolos", "éndolos");
        t4 = t4.replace("endolas", "éndolas");

        t4 = t4.replace("porqué", "por qué"); // risky        

        t4 = t4.replace("Ahi ", "Ahí ");
        t4 = t4.replace("Aqui ", "Aquí ");
        t4 = t4.replace("Alli ", "Allí ");
        t4 = t4.replace("Aca ", "Acá ");
        t4 = t4.replace("Alla ", "Allá ");

        t4 = t4.replace("Asi ", "Así ");

        t4 = t4.replace("Algun ", "Algún ");
        t4 = t4.replace("Segun ", "Según ");

        t4 = t4.replace("Ésto ", "Esto ");

        t4 = t4.replace("ademas", "además");
        t4 = t4.replace("Ademas", "Además");
        t4 = t4.replace("despues", "después");
        t4 = t4.replace("Despues", "Después");
        t4 = t4.replace("jamas", "jamás");
        t4 = t4.replace("Jamas", "Jamás");
        t4 = t4.replace("mayoria", "mayoría");
        t4 = t4.replace("Mayoria", "Mayoría");
        t4 = t4.replace("ningun", "ningún");
        t4 = t4.replace("Ningun", "Ningún");
        t4 = t4.replace("tambien", "también");
        t4 = t4.replace("Tambien", "También");
        t4 = t4.replace("ojala", "ojalá");
        t4 = t4.replace("Ojala", "Ojalá");

        t4 = t4.replace(" m-", " M-");

        t4 = t4.replace("Ayto.", "ayuntamiento");
        t4 = t4.replace("ayto.", "ayuntamiento");
        t4 = t4.replace("Vd.", "usted");
        t4 = t4.replace("vd.", "usted");
        t4 = t4.replace("Ayto ", "ayuntamiento ");
        t4 = t4.replace("ayto ", "ayuntamiento ");
        t4 = t4.replace("Vd ", "usted ");
        t4 = t4.replace("vd ", "usted ");

        t4 = t4 + ".";
        t4 = t4.replace("..", ".");

        t4 = t4.trim();

        return t4;
    }

    /**
     *
     * @param text
     * @return
     */
    private static String rightCleanText(String text) {
        String newText = text.trim();
        newText = newText.replaceAll("\\.+$", "");
        newText = newText.replaceAll("\\,+$", "");
        newText = newText.replaceAll("\\!+$", "");
        return newText;
    }

    /**
     *
     * @param word
     * @return
     */
    private static String toLowerCase2(String word, List<String> locations) {

        word = word.trim();

        for (String t : SYMBOLS) {
            word = word.replace(t, "");
        }
        for (String t : UPPERCASE_ALLOWED) {
            if (word.equals(t)) {
                return word;
            }
        }

        String s = "";
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            s += Character.toLowerCase(c);
        }

        for (String l : locations) {
            String l2 = l.toLowerCase();
            if (s.equals(l2)) {
                s = l;
            }
        }

        return s;
    }

}
