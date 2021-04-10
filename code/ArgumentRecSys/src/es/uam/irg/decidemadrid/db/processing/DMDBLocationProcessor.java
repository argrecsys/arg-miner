package es.uam.irg.decidemadrid.db.processing;

import es.uam.irg.db.MySQLDBConnector;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMLocation;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class DMDBLocationProcessor {

    public MySQLDBConnector db;

    private Map<Integer, String> districtIds;
    private Map<Integer, String> neighborhoodIds;

    private Map<String, List<String>> streetDistricts;
    private Map<String, List<String>> streetNeighborhoods;

    private Map<String, List<String>> poiDistricts;
    private Map<String, List<String>> poiNeighborhoods;

    private Map<String, List<String>> districts;
    private Map<String, List<String>> neighborhoods;

    private Map<String, String> neighborhoodDistrict;

    private Map<Integer, List<String>> tags;
    private Map<String, List<Integer>> tagIds;

    private Map<Integer, List<DMLocation>> proposalLocations;

    private Map<Integer, String> districtsUnsorted;

    public DMDBLocationProcessor() throws Exception {
        this(DMDBManager.DB_SERVER, DMDBManager.DB_NAME, DMDBManager.DB_USERNAME, DMDBManager.DB_USERPASSWORD);
    }

    public DMDBLocationProcessor(String dbServer, String dbName, String dbUserName, String dbUserPassword) throws Exception {
        this.db = new MySQLDBConnector();
        this.db.connect(dbServer, dbName, dbUserName, dbUserPassword);

        this.districtsUnsorted = new HashMap<>();
        this.districtsUnsorted.put(1, "Fuencarral-El Pardo");
        this.districtsUnsorted.put(2, "Moncloa-Aravaca");
        this.districtsUnsorted.put(3, "Tetuán");
        this.districtsUnsorted.put(4, "Chamberí");
        this.districtsUnsorted.put(5, "Centro");
        this.districtsUnsorted.put(6, "Latina");
        this.districtsUnsorted.put(7, "Carabanchel");
        this.districtsUnsorted.put(8, "Arganzuela");
        this.districtsUnsorted.put(9, "Usera");
        this.districtsUnsorted.put(10, "Villaverde");
        this.districtsUnsorted.put(11, "Chamartín");
        this.districtsUnsorted.put(12, "Salamanca");
        this.districtsUnsorted.put(13, "Retiro");
        this.districtsUnsorted.put(14, "Puente de Vallecas");
        this.districtsUnsorted.put(15, "Villa de Vallecas");
        this.districtsUnsorted.put(16, "Hortaleza");
        this.districtsUnsorted.put(17, "Barajas");
        this.districtsUnsorted.put(18, "Ciudad Lineal");
        this.districtsUnsorted.put(19, "Moratalaz");
        this.districtsUnsorted.put(20, "San Blas-Canillejas");
        this.districtsUnsorted.put(21, "Vicálvaro");

        this.neighborhoodDistrict = new HashMap<>();
        String query = "SELECT geo_neighborhoods.name AS neighborhood, geo_districts.name AS district "
                + "FROM geo_neighborhoods, geo_districts "
                + "WHERE districtId = geo_districts.id";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String neighborhood = rs.getString("neighborhood");
            String district = rs.getString("district");
            this.neighborhoodDistrict.put(neighborhood, district);
        }
        rs.close();

        System.out.print("Loading district ids... ");
        this.loadDistrictIds();
        System.out.println(" [OK]");

        System.out.print("Loading neighborhood ids... ");
        this.loadNeighborhoodIds();
        System.out.println(" [OK]");

        System.out.print("Loading districts... ");
        this.loadDistricts();
        System.out.println(" [OK]");

        System.out.print("Loading neighborhoods... ");
        this.loadNeighborhoods();
        System.out.println(" [OK]");

        System.out.print("Loading streets... ");
        this.loadStreets();
        System.out.println(" [OK]");

        System.out.print("Loading POIs... ");
        this.loadPOIs();
        System.out.println(" [OK]");

        System.out.print("Loading tags... ");
        this.loadTags();
        System.out.println(" [OK]");
    }

    @Override
    public void finalize() {
        this.db.disconnect();
    }

    private void loadTags() throws Exception {
        this.tags = new HashMap<>();
        this.tagIds = new HashMap<>();

        String query = "SELECT * FROM tags";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String value = rs.getString("name").toLowerCase().trim();

            value = value.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
            value = value.replace("\"", "").replace("`", "").replace("´", "");
            value = value.replace("¡", "").replace("!", "").replace("¿", "").replace("?", "");
            value = value.replace("(", ",").replace("[", ",").replace("{", ",").replace(" - ", ",");
            value = value.replace(")", "").replace("]", "").replace("}", "");
            value = value.replace("...", "");

            value = value.replace("  ", " ").trim();

            StringTokenizer tokenizer = new StringTokenizer(value, ",.;:#");
            while (tokenizer.hasMoreTokens()) {
                String tag = tokenizer.nextToken().trim();
                if (tag.isEmpty()) {
                    continue;
                }
                if (!this.tags.containsKey(id)) {
                    this.tags.put(id, new ArrayList<>());
                }
                if (!this.tags.get(id).contains(tag)) {
                    this.tags.get(id).add(tag);
                }
                if (!this.tagIds.containsKey(tag)) {
                    this.tagIds.put(tag, new ArrayList<>());
                }
                if (!this.tagIds.get(tag).contains(id)) {
                    this.tagIds.get(tag).add(id);
                }
            }
        }
        rs.close();
    }

    private void loadDistrictIds() throws Exception {
        this.districtIds = new HashMap<>();
        String query = "SELECT * FROM geo_districts";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            this.districtIds.put(id, name);
        }
        rs.close();
    }

    private void loadNeighborhoodIds() throws Exception {
        this.neighborhoodIds = new HashMap<>();
        String query = "SELECT * FROM geo_neighborhoods";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            this.neighborhoodIds.put(id, name);
        }
        rs.close();
    }

    private void loadDistricts() throws Exception {
        this.districts = new HashMap<>();
        String query = "SELECT tag, NAME  "
                + "FROM geo_districts, geo_district_tags "
                + "WHERE geo_districts.id = geo_district_tags.id;";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String tag = rs.getString("tag");
            String district = rs.getString("name");
            if (!this.districts.containsKey(tag)) {
                this.districts.put(tag, new ArrayList<>());
            }
            this.districts.get(tag).add(district);
        }
        rs.close();
    }

    private void loadNeighborhoods() throws Exception {
        this.neighborhoods = new HashMap<>();
        String query = "SELECT tag, NAME  "
                + "FROM geo_neighborhoods, geo_neighborhood_tags "
                + "WHERE geo_neighborhoods.id = geo_neighborhood_tags.id;";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String tag = rs.getString("tag");
            String district = rs.getString("name");
            if (!this.neighborhoods.containsKey(tag)) {
                this.neighborhoods.put(tag, new ArrayList<>());
            }
            this.neighborhoods.get(tag).add(district);
        }
        rs.close();
    }

    private void loadStreets() throws Exception {
        this.streetDistricts = new HashMap<>();
        this.streetNeighborhoods = new HashMap<>();
        String query = "SELECT * FROM geo_streets";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String street = rs.getString("street");
            String district = this.districtIds.get(rs.getInt("district"));
            String neighborhood = this.neighborhoodIds.get(rs.getInt("neighborhood"));

            if (!this.streetDistricts.containsKey(street)) {
                this.streetDistricts.put(street, new ArrayList<>());
            }
            if (!this.streetDistricts.get(street).contains(district)) {
                this.streetDistricts.get(street).add(district);
            }

            if (!this.streetNeighborhoods.containsKey(street)) {
                this.streetNeighborhoods.put(street, new ArrayList<>());
            }
            if (!this.streetNeighborhoods.get(street).contains(neighborhood)) {
                this.streetNeighborhoods.get(street).add(neighborhood);
            }
        }
        rs.close();
    }

    private void loadPOIs() throws Exception {
        this.poiDistricts = new HashMap<>();
        this.poiNeighborhoods = new HashMap<>();

        String query = "SELECT * FROM geo_pois";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String poi = rs.getString("poi");
            String district = this.districtIds.get(rs.getInt("district"));
            String neighborhood = this.neighborhoodIds.get(rs.getInt("neighborhood"));

            if (!this.poiDistricts.containsKey(poi)) {
                this.poiDistricts.put(poi, new ArrayList<>());
            }
            poiDistricts.get(poi).add(district);

            if (!this.poiNeighborhoods.containsKey(poi)) {
                this.poiNeighborhoods.put(poi, new ArrayList<>());
            }
            poiNeighborhoods.get(poi).add(neighborhood);
        }
        rs.close();
    }

    public void processStreets(String file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, ";");
            tokenizer.nextToken();
            String type1 = tokenizer.nextToken().toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
            String type2 = tokenizer.nextToken().toLowerCase();
            String type = (type1 + " " + type2).trim();
            String name = tokenizer.nextToken().toLowerCase();
            String street = (type + " " + name).trim();
            if (street.contains(" a-") || street.contains(" m-") || street.contains(" r-")) {
                street = name;
            }

            tokenizer.nextToken();
            String token = tokenizer.nextToken().trim();
            if (token.isEmpty()) {
                continue;
            }
            int districtId = Integer.valueOf(token);
            int neighborhoodId = districtId * 10 + Integer.valueOf(tokenizer.nextToken().trim());
            //String district = this.districtIds.get(districtId);
            //String neighborhood = this.neighborhoodIds.get(neighborhoodId);

            String query = "INSERT INTO geo_streets VALUES(\"" + street + "\"," + districtId + "," + neighborhoodId + ")";
            try {
                this.db.executeInsert(query, false, null);
            } catch (Exception e) {
            }
        }
        reader.close();
    }

    public void findPOIs(String file) throws Exception {
        List<String> tokens1 = new ArrayList<>();
        tokens1.add("");
        tokens1.add("autovía ");
        tokens1.add("calle ");
        tokens1.add("carretera ");
        tokens1.add("avenida ");
        tokens1.add("ronda ");
        tokens1.add("paseo ");
        tokens1.add("camino ");
        tokens1.add("plaza ");
        tokens1.add("aeropuerto ");
        tokens1.add("pasaje ");
        tokens1.add("puente ");
        tokens1.add("travesía ");
        tokens1.add("glorieta ");
        tokens1.add("plazuela ");
        tokens1.add("callejón ");
        tokens1.add("costanilla ");
        tokens1.add("pista ");
        tokens1.add("jardín ");
        tokens1.add("arroyo ");
        tokens1.add("cuesta ");
        tokens1.add("particular ");
        tokens1.add("acceso ");
        tokens1.add("paso elevado ");
        tokens1.add("polígono ");
        tokens1.add("poblado de absorción ");
        tokens1.add("trasera ");
        tokens1.add("bulevar ");
        tokens1.add("escalinata ");
        tokens1.add("colonia ");
        tokens1.add("senda ");
        tokens1.add("pasadizo ");
        tokens1.add("autopista ");
        tokens1.add("cañada ");
        tokens1.add("carrera ");
        tokens1.add("galería ");

        List<String> tokens2 = new ArrayList<>();
        tokens2.add("");
        tokens2.add("de ");
        tokens2.add("de ");
        tokens2.add("del ");
        tokens2.add("de las ");
        tokens2.add("de los ");
        tokens2.add("al ");
        tokens2.add("a la ");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, ";");
            String poi = tokenizer.nextToken();
            String street = tokenizer.nextToken();

            String t = "";
            boolean found = false;
            for (String t1 : tokens1) {
                for (String t2 : tokens2) {
                    t = t1 + t2 + street;
                    if (this.streetDistricts.containsKey(t)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (found) {
                System.out.println(poi + "\t" + street);
            }
        }
        reader.close();
    }

    public void processPOIs(String file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, ";");
            String poi = tokenizer.nextToken();
            while (tokenizer.hasMoreTokens()) {
                int neighborhood = Integer.valueOf(tokenizer.nextToken());
                int district = neighborhood / 10;

                String query = "INSERT INTO geo_pois VALUES(\"" + poi + "\"," + district + "," + neighborhood + ")";
                this.db.executeInsert(query, false, null);
            }
        }
        reader.close();
    }

    public void processProposalDistricts() throws Exception {
        // 1. EXTRACTING LOCATIONS FROM GEOZONE
        String query = "SELECT id, geozone_id FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");

            int district = rs.getInt("geozone_id");
            String d = district > 0 ? this.districtsUnsorted.get(district) : "";
            if (!d.isEmpty()) {
                DMLocation location = new DMLocation(d, null, null, null);
                if (!proposalLocations.get(id).contains(location)) {
                    proposalLocations.get(id).add(location);
                }

                if (debug) {
                    System.out.println(id + "\tGEO\t" + d);
                }
            }
        }

        // 2. EXTRACTING LOCATIONS FROM PROPOSAL TAGS
        query = "SELECT proposals.id, tag_id "
                + "FROM taggings, proposals "
                + "WHERE taggings.taggable_type = 'Proposal' AND taggings.taggable_id = proposals.id";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
//            if (!proposalLocations.get(id).isEmpty()) {
//                continue;
//            }

            int tagId = rs.getInt("tag_id");
            if (!this.tags.containsKey(tagId)) {
                continue;
            }
            List<String> _tags = this.tags.get(tagId);
            for (String tag : _tags) {
                if (this.districts.containsKey(tag)) {
                    List<String> _districts = this.districts.get(tag);
                    for (String d : _districts) {
                        DMLocation location = new DMLocation(d, null, null, tag);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tTAG\t" + d + "\t[" + tag + "]");
                            }
                        }
                    }
                }
            }
        }
        rs.close();

        // 3. EXTRACTING LOCATIONS FROM PROPOSAL TITLES
        List<String> _tags = new ArrayList<>(this.districts.keySet());
        Collections.sort(_tags);

        query = "SELECT id, title FROM proposals";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
//            if (!proposalLocations.get(id).isEmpty()) {
//                continue;
//            }

            String title = rs.getString("title");

            title = title.toLowerCase();
            title = title.replace("bº", "barrio ");
            title = title.replace("pº", "paseo ");
            title = title.replace("c/", "calle ");
            title = title.replace("ctra", "carretera ");
            title = title.replace("pza", "plaza ");
            title = title.replace("avda", "avenida ");
            title = title.replace("avd", "avenida ");
            title = title.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace(".", " ").replace(",", " ").replace(":", " ").replace(";", " ").replace("-", " ").replace("/", " ");
            title = title.replace("\"", " ").replace("'", " ").replace("(", " ").replace(")", " ").replace("[", " ").replace("]", " ").replace("{", " ").replace("}", " ").replace("_", " ").trim();
            title = title.replace("  ", " ").trim();
            title = " " + title + " ";

            for (int t = _tags.size() - 1; t >= 0; t--) {
                String tag = _tags.get(t);
                if (title.contains(" " + tag + " ")) {
                    List<String> ds = this.districts.get(tag);
                    for (String d : ds) {
                        DMLocation location = new DMLocation(d, null, null, tag);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tTIT\t" + d + "\t[" + tag + "]\t" + title);
                            }
                        }
                    }
                }
            }
        }
        rs.close();
    }

    public void processProposalNeighborhoods() throws Exception {
        // 1. EXTRACTING LOCATIONS FROM PROPOSAL TAGS
        String query = "SELECT proposals.id, tag_id "
                + "FROM taggings, proposals "
                + "WHERE taggings.taggable_type = 'Proposal' AND taggings.taggable_id = proposals.id";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");

            int tagId = rs.getInt("tag_id");
            if (!tags.containsKey(tagId)) {
                continue;
            }

            List<String> _tags = tags.get(tagId);
            for (String tag : _tags) {
                if (this.neighborhoods.containsKey(tag)) {
                    List<String> _neighborhoods = this.neighborhoods.get(tag);
                    for (String n : _neighborhoods) {
                        String d = this.neighborhoodDistrict.get(n);

                        DMLocation location = new DMLocation(d, n, null, tag);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tTAG\t" + n + "\t[" + tag + "]");
                            }
                        }
                    }
                }
            }
        }
        rs.close();

        // 2. EXTRACTING LOCATIONS FROM PROPOSAL TITLES
        List<String> _tags = new ArrayList<>(this.neighborhoods.keySet());
        Collections.sort(_tags);

        query = "SELECT id, title FROM proposals";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");

            title = title.toLowerCase();
            title = title.replace("bº", "barrio ");
            title = title.replace("pº", "paseo ");
            title = title.replace("c/", "calle ");
            title = title.replace("ctra", "carretera ");
            title = title.replace("pza", "plaza ");
            title = title.replace("avda", "avenida ");
            title = title.replace("avd", "avenida ");
            title = title.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace(".", " ").replace(",", " ").replace(":", " ").replace(";", " ").replace("-", " ").replace("/", " ");
            title = title.replace("\"", " ").replace("'", " ").replace("(", " ").replace(")", " ").replace("[", " ").replace("]", " ").replace("{", " ").replace("}", " ").replace("_", " ").trim();
            title = title.replace("  ", " ").trim();
            title = " " + title + " ";

            for (int t = _tags.size() - 1; t >= 0; t--) {
                String tag = _tags.get(t);
                if (title.contains(" " + tag + " ") && (!tag.equals("goya") || (tag.equals("goya") && !title.contains("premio")))) {
                    List<String> list = this.neighborhoods.get(tag);
                    for (String n : list) {
                        String d = this.neighborhoodDistrict.get(n);

                        DMLocation location = new DMLocation(d, n, null, tag);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tTIT\t" + n + "\t[" + tag + "]\t" + title);
                            }
                        }
                    }
                }
            }
        }
        rs.close();
    }

    public void processProposalStreets() throws Exception {
        // 1. EXTRACTING LOCATIONS FROM PROPOSAL TAGS
        String query = "SELECT proposals.id, tag_id "
                + "FROM taggings, proposals "
                + "WHERE taggings.taggable_type = 'Proposal' AND taggings.taggable_id = proposals.id";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");

            int tagId = rs.getInt("tag_id");
            if (!tags.containsKey(tagId)) {
                continue;
            }

            List<String> _tags = tags.get(tagId);
            for (String tag : _tags) {
                if (this.streetDistricts.containsKey(tag)) {
                    List<String> tagDistricts = this.streetDistricts.get(tag);
                    List<String> tagNeighborhoods = this.streetNeighborhoods.get(tag);

                    for (int i = 0; i < tagDistricts.size(); i++) {
                        String d = tagDistricts.get(i);
                        String n = tagNeighborhoods.get(i);

                        DMLocation location = new DMLocation(d, n, tag, tag);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tSTR\t" + d + "\t[" + tag + "]");
                                System.out.println(id + "\tSTR\t" + n + "\t[" + tag + "]");
                            }
                        }
                    }
                }
            }
        }
        rs.close();

        // 2. EXTRACTING LOCATIONS FROM PROPOSAL TITLES
        List<String> _streets = new ArrayList<>(this.streetDistricts.keySet());
        Collections.sort(_streets);

        query = "SELECT id, title FROM proposals";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");

            String title = rs.getString("title");

            title = title.toLowerCase();
            title = title.replace("bº", "barrio ");
            title = title.replace("pº", "paseo ");
            title = title.replace("c/", "calle ");
            title = title.replace("ctra", "carretera ");
            title = title.replace("pza", "plaza ");
            title = title.replace("avda", "avenida ");
            title = title.replace("avd", "avenida ");
            title = title.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace(".", " ").replace(",", " ").replace(":", " ").replace(";", " ").replace("-", " ").replace("/", " ");
            title = title.replace("\"", " ").replace("'", " ").replace("(", " ").replace(")", " ").replace("[", " ").replace("]", " ").replace("{", " ").replace("}", " ").replace("_", " ").trim();
            title = title.replace("  ", " ").trim();
            title = " " + title + " ";

            for (int s = _streets.size() - 1; s >= 0; s--) {
                String street = _streets.get(s);
                if (title.contains(" " + street + " ")) {
                    List<String> list1 = this.streetDistricts.get(street);
                    List<String> list2 = this.streetNeighborhoods.get(street);

                    for (int i = 0; i < list1.size(); i++) {
                        String d = list1.get(i);
                        String n = list2.get(i);

                        DMLocation location = new DMLocation(d, n, street, null);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tSTR\t" + d + "\t[" + title + "]\t" + street);
                                System.out.println(id + "\tSTR\t" + n + "\t[" + title + "]\t" + street);
                            }
                        }
                    }
                }
            }
        }

        rs.close();
    }

    public void processProposalPOIs() throws Exception {
        // 1. EXTRACTING LOCATIONS FROM PROPOSAL TAGS
        String query = "SELECT proposals.id, tag_id "
                + "FROM taggings, proposals "
                + "WHERE taggings.taggable_type = 'Proposal' AND taggings.taggable_id = proposals.id";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");

            int tagId = rs.getInt("tag_id");
            if (!tags.containsKey(tagId)) {
                continue;
            }

            List<String> _tags = tags.get(tagId);
            for (String tag : _tags) {
                if (this.poiDistricts.containsKey(tag)) {
                    List<String> tagDistricts = this.poiDistricts.get(tag);
                    List<String> tagNeighborhoods = this.poiNeighborhoods.get(tag);

                    for (int i = 0; i < tagDistricts.size(); i++) {
                        String d = tagDistricts.get(i);
                        String n = tagNeighborhoods.get(i);

                        DMLocation location = new DMLocation(d, n, tag, tag);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tPOI\t" + d + "\t[" + tag + "]");
                                System.out.println(id + "\tPOI\t" + n + "\t[" + tag + "]");
                            }
                        }
                    }
                }
            }
        }
        rs.close();

        // 2. EXTRACTING LOCATIONS FROM PROPOSAL TITLES
        List<String> _pois = new ArrayList<>(this.poiDistricts.keySet());
        Collections.sort(_pois);

        query = "SELECT id, title FROM proposals";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");

            String title = rs.getString("title");

            title = title.toLowerCase();
            title = title.replace("bº", "barrio ");
            title = title.replace("pº", "paseo ");
            title = title.replace("c/", "calle ");
            title = title.replace("ctra", "carretera ");
            title = title.replace("pza", "plaza ");
            title = title.replace("avda", "avenida ");
            title = title.replace("avd", "avenida ");
            title = title.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace(".", " ").replace(",", " ").replace(":", " ").replace(";", " ").replace("-", " ").replace("/", " ");
            title = title.replace("\"", " ").replace("'", " ").replace("(", " ").replace(")", " ").replace("[", " ").replace("]", " ").replace("{", " ").replace("}", " ").replace("_", " ").trim();
            title = title.replace("  ", " ").trim();
            title = " " + title + " ";

            for (int s = _pois.size() - 1; s >= 0; s--) {
                String poi = _pois.get(s);
                if (title.contains(" " + poi + " ")) {
                    List<String> list1 = this.poiDistricts.get(poi);
                    List<String> list2 = this.poiNeighborhoods.get(poi);

                    for (int i = 0; i < list1.size(); i++) {
                        String d = list1.get(i);
                        String n = list2.get(i);

                        DMLocation location = new DMLocation(d, n, poi, null);
                        if (!proposalLocations.get(id).contains(location)) {
                            proposalLocations.get(id).add(location);

                            if (debug) {
                                System.out.println(id + "\tPOI\t" + d + "\t[" + title + "]\t" + poi);
                                System.out.println(id + "\tPOI\t" + n + "\t[" + title + "]\t" + poi);
                            }
                        }
                    }
                }
            }
        }

        rs.close();
    }

    public void processProposalLocations() throws Exception {
        this.proposalLocations = new HashMap<>();

        String query = "SELECT id FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            this.proposalLocations.put(id, new ArrayList<>());
        }
        rs.close();

        System.out.print("Processing proposal districts... ");
        this.processProposalDistricts();
        System.out.println("[OK]");

        System.out.print("Processing proposal neighborhoods... ");
        this.processProposalNeighborhoods();
        System.out.println("[OK]");

        System.out.print("Processing proposal streets... ");
        this.processProposalStreets();
        System.out.println("[OK]");

        System.out.print("Processing proposal POIs... ");
        this.processProposalPOIs();
        System.out.println("[OK]");

        System.out.print("Inserting proposal locations... ");
        List<Integer> ids = new ArrayList<>(this.proposalLocations.keySet());
        Collections.sort(ids);
        for (int id : ids) {
            List<DMLocation> locations = this.proposalLocations.get(id);
            for (DMLocation location : locations) {
                String district = location.getDistrict();
                String neighborhood = location.getNeighborhood();
                String street = location.getLocation();
                String tag = location.getTag();

                query = "INSERT INTO proposal_locations VALUES(" + id + ",\"" + district + "\",\"" + neighborhood + "\",\"" + street + "\",\"" + tag + "\")";
                this.db.executeInsert(query, false, null);
            }
        }
        System.out.println("[OK]");
    }

    private boolean debug = false;

    public static void main(String[] args) {
        try {
            DMDBLocationProcessor processor = new DMDBLocationProcessor();
            //processor.processStreets("./data_kmi/VialesVigentesDistritosBarrios_20190318.csv"); // to be executed once
            //processor.findPOIs("./data_kmi/turismo_v1_es.txt"); // to be executed once
            //processor.processPOIs("./data_kmi/pois.txt"); // to be executed once
            processor.processProposalLocations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
