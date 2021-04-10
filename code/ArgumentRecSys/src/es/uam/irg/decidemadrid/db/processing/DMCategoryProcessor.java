package es.uam.irg.decidemadrid.db.processing;

import es.uam.irg.db.MySQLDBConnector;
import es.uam.irg.decidemadrid.db.DMDBManager;
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

public class DMCategoryProcessor {

    public MySQLDBConnector db;

    private Map<Integer, List<String>> tags;
    private Map<String, List<Integer>> tagIds;

    private Map<String, Integer> categories;
    private Map<String, Map<String, Double>> categoryTags;
    private Map<String, Map<String, Double>> tagCategories;

    public DMCategoryProcessor() throws Exception {
        this(DMDBManager.DB_SERVER, DMDBManager.DB_NAME, DMDBManager.DB_USERNAME, DMDBManager.DB_USERPASSWORD);
    }

    public DMCategoryProcessor(String dbServer, String dbName, String dbUserName, String dbUserPassword) throws Exception {
        this.db = new MySQLDBConnector();
        this.db.connect(dbServer, dbName, dbUserName, dbUserPassword);

        System.out.print("Loading tags... ");
        this.loadTags();
        System.out.println("[OK]");

        System.out.print("Loading categories... ");
        this.loadCategories();
        System.out.println("[OK]");

        System.out.print("Loading category tags... ");
        this.loadCategoryTags();
        System.out.println("[OK]");
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

    public void loadCategories() throws Exception {
        this.categories = new HashMap<>();

        String query = "SELECT * FROM cat_categories";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name").trim();
            this.categories.put(name, id);
        }
        rs.close();
    }

    public void loadCategoryTags() throws Exception {
        this.categoryTags = new HashMap<>();
        this.tagCategories = new HashMap<>();

        String query = "SELECT * FROM cat_category_tags_seeds";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String tag = rs.getString("tag").trim();
            String category = rs.getString("category").trim();
            double weight = rs.getDouble("weight");

            if (!this.categoryTags.containsKey(category)) {
                this.categoryTags.put(category, new HashMap<>());
            }
            this.categoryTags.get(category).put(tag, weight);
            if (!this.tagCategories.containsKey(tag)) {
                this.tagCategories.put(tag, new HashMap<>());
            }
            this.tagCategories.get(tag).put(category, weight);
        }
        rs.close();
    }

    public void processProposalTags() throws Exception {
        Map<Integer, List<String>> proposalTags = new HashMap<>();

        String query = "SELECT proposals.id, tag_id "
                + "FROM taggings, proposals "
                + "WHERE taggings.taggable_type = 'Proposal' AND taggings.taggable_id = proposals.id";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");

            int tagId = rs.getInt("tag_id");
            if (!this.tags.containsKey(tagId)) {
                continue;
            }

            List<String> _tags = this.tags.get(tagId);
            for (String tag : _tags) {
                if (!proposalTags.containsKey(id)) {
                    proposalTags.put(id, new ArrayList<>());
                }
                if (!proposalTags.get(id).contains(tag)) {
                    proposalTags.get(id).add(tag);
                }
            }
        }
        rs.close();

        List<Integer> ids = new ArrayList<>(proposalTags.keySet());
        Collections.sort(ids);
        for (int id : ids) {
            List<String> list = proposalTags.get(id);
            for (String tag : list) {
                if (tag.length() > 48) {
                    System.out.println("Not inserted: " + tag);
                    continue;
                }
                query = "INSERT INTO proposal_tags VALUES(" + id + ",\"" + tag + "\")";
                this.db.executeInsert(query, false, null);
            }
        }
    }

    public void processProposalCategories() throws Exception {
        Map<Integer, Map<String, Double>> proposalCategories = new HashMap<>();

        // 1. EXTRACTING CATEGORIES FROM TAGS
        String query = "SELECT * FROM proposal_tags";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String tag = rs.getString("tag");

            if (!this.tagCategories.containsKey(tag)) {
                continue;
            }

            Map<String, Double> _categories = this.tagCategories.get(tag);
            for (String category : _categories.keySet()) {
                double weight = _categories.get(category);

                if (!proposalCategories.containsKey(id)) {
                    proposalCategories.put(id, new HashMap<>());
                }
                if (!proposalCategories.get(id).containsKey(category)) {
                    proposalCategories.get(id).put(category, 0.0);
                }

                weight += proposalCategories.get(id).get(category);
                proposalCategories.get(id).put(category, weight);
            }
        }
        rs.close();

        // 2. EXTRACTING CATEGORIES FROM TITLES
        List<String> _tags = new ArrayList<>(this.tagCategories.keySet());
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

            String lastTag = null;
            for (int t = _tags.size() - 1; t >= 0; t--) {
                String tag = _tags.get(t);
                if (title.contains(" " + tag + " ")) {

                    if (lastTag != null && lastTag.contains(tag)) {
                        //System.out.println(lastTag + " -> " + tag);
                        //continue;
                    }
                    lastTag = tag;

                    Map<String, Double> _categories = this.tagCategories.get(tag);
                    for (String category : _categories.keySet()) {
                        double weight = _categories.get(category);

                        if (!proposalCategories.containsKey(id)) {
                            proposalCategories.put(id, new HashMap<>());
                        }
                        if (!proposalCategories.get(id).containsKey(category)) {
                            proposalCategories.get(id).put(category, 0.0);
                        }

                        weight += proposalCategories.get(id).get(category);
                        proposalCategories.get(id).put(category, weight);
                    }
                }
            }
        }
        rs.close();

        Map<Integer, Double> totalWeights = new HashMap<>();
        for (int id : proposalCategories.keySet()) {
            Map<String, Double> _categories = proposalCategories.get(id);
            double totalWeight = 0;
            for (String category : _categories.keySet()) {
                double weight = _categories.get(category);
                totalWeight += weight;
            }
            totalWeights.put(id, totalWeight);
        }

        for (int id : proposalCategories.keySet()) {
            double totalWeight = totalWeights.get(id);

            Map<String, Double> _categories = proposalCategories.get(id);
            List<String> __categories = new ArrayList<>(_categories.keySet());
            Collections.sort(__categories);
            for (String category : __categories) {
                double weight = _categories.get(category);

                query = "INSERT INTO proposal_categories VALUES(" + id + ",\"" + category + "\"," + weight + "," + weight / totalWeight + ")";
                this.db.executeInsert(query, false, null);
            }
        }
    }

    public void processProposalTopics() throws Exception {
        Map<Integer, Map<String, Double>> proposalTopics = new HashMap<>();

        List<String> forbidden = new ArrayList<>();
        for (String category : this.categoryTags.keySet()) {
            category = category.toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
            int index = category.indexOf(" y ");
            if (index < 0) {
                forbidden.add(category);
                forbidden.add(category + "s");
                forbidden.add(category + "es");
                forbidden.add(category.substring(0, category.length() - 1));
            }
        }

        // 1. EXTRACTING TOPICS FROM TAGS
        String query = "SELECT * FROM proposal_tags";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String tag = rs.getString("tag");

            if (!this.tagCategories.containsKey(tag)) {
                continue;
            }

            if (forbidden.contains(tag)) {  // discard tags that corrspond to category names
                continue;
            }

//            // Convert to singular
//            if (tag.endsWith("s")) {
//                String _tag = tag.substring(0, tag.length() - 1);
//                if (tagCategories.containsKey(_tag)) {
//                    tag = _tag;
//                } else {
//                    _tag = tag.substring(0, tag.length() - 2);
//                    if (tagCategories.containsKey(_tag)) {
//                        tag = _tag;
//                    }
//                }
//            }
            if (!proposalTopics.containsKey(id)) {
                proposalTopics.put(id, new HashMap<>());
            }
            if (!proposalTopics.get(id).containsKey(tag)) {
                proposalTopics.get(id).put(tag, 0.0);
            }

            double weight = 1.0 + proposalTopics.get(id).get(tag);
            proposalTopics.get(id).put(tag, weight);
        }
        rs.close();

        // 2. EXTRACTING TOPICS FROM TITLES
        List<String> _tags = new ArrayList<>(this.tagCategories.keySet());
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

                    if (!this.tagCategories.containsKey(tag)) {
                        continue;
                    }

                    if (forbidden.contains(tag)) {  // discard tags that corrspond to category names
                        continue;
                    }

                    // Convert to singular
                    if (tag.endsWith("s")) {
                        String _tag = tag.substring(0, tag.length() - 1);
                        if (tagCategories.containsKey(_tag)) {
                            tag = _tag;
                        } else {
                            _tag = tag.substring(0, tag.length() - 2);
                            if (tagCategories.containsKey(_tag)) {
                                tag = _tag;
                            }
                        }
                    }

                    if (!proposalTopics.containsKey(id)) {
                        proposalTopics.put(id, new HashMap<>());
                    }
                    if (!proposalTopics.get(id).containsKey(tag)) {
                        proposalTopics.get(id).put(tag, 0.0);
                    }

                    double weight = 1.0 + proposalTopics.get(id).get(tag);
                    proposalTopics.get(id).put(tag, weight);

                }
            }
        }
        rs.close();

        // Reducing vocabulary
        List<Integer> _ids = new ArrayList<>(proposalTopics.keySet());
        Collections.sort(_ids);
        for (int id : _ids) {
            List<String> _topics = new ArrayList<>(proposalTopics.get(id).keySet());
            Collections.sort(_topics);
            for (int i = 0; i < _topics.size(); i++) {
                String t1 = _topics.get(i);
                for (int j = 0; j < _topics.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    String t2 = _topics.get(j);
                    if (t1.contains(t2)) {
                        proposalTopics.get(id).remove(t2);
                    }
                }
            }
        }

        // Normalizing weights
        Map<Integer, Double> totalWeights = new HashMap<>();
        for (int id : proposalTopics.keySet()) {
            Map<String, Double> _topics = proposalTopics.get(id);
            double totalWeight = 0;
            for (String category : _topics.keySet()) {
                double weight = _topics.get(category);
                totalWeight += weight;
            }
            totalWeights.put(id, totalWeight);
        }

        for (int id : proposalTopics.keySet()) {
            double totalWeight = totalWeights.get(id);

            Map<String, Double> _categories = proposalTopics.get(id);
            List<String> __categories = new ArrayList<>(_categories.keySet());
            Collections.sort(__categories);
            for (String category : __categories) {
                double weight = _categories.get(category);

                query = "INSERT INTO proposal_topics VALUES(" + id + ",\"" + category + "\"," + weight + "," + weight / totalWeight + ")";
                this.db.executeInsert(query, false, null);
            }
        }
    }

    public void obtainSynonymsProposalTopics() throws Exception {
        List<String> forbidden = new ArrayList<>();
        for (String category : this.categoryTags.keySet()) {
            category = category.toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
            int index = category.indexOf(" y ");
            if (index < 0) {
                forbidden.add(category);
                forbidden.add(category + "s");
                forbidden.add(category + "es");
                forbidden.add(category.substring(0, category.length() - 1));
            }
        }

        Map<String, Map<String, String>> clusters = new HashMap<>();

        List<String> _tags = new ArrayList<>(this.tagCategories.keySet());
        Collections.sort(_tags);

        String query = "SELECT id, title FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
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

            String lastTag = null;
            for (int t = _tags.size() - 1; t >= 0; t--) {
                String tag = _tags.get(t);

                if (title.contains(" " + tag + " ")) {

                    if (!this.tagCategories.containsKey(tag)) {
                        continue;
                    }

                    if (forbidden.contains(tag)) {  // discard tags that corrspond to category names
                        continue;
                    }

                    if (lastTag != null && lastTag.contains(tag)) {
                        if (!clusters.containsKey(lastTag)) {
                            clusters.put(lastTag, new HashMap<>());
                        }
                        clusters.get(lastTag).put(tag, null);
                    }
                    lastTag = tag;
                }
            }
        }
        rs.close();

        List<String> list = new ArrayList<>(clusters.keySet());
        Collections.sort(list);
        System.out.println("***** SYNONYMS FROM TOPICS");
        for (String l : list) {
            System.out.print(l);
            for (String l2 : clusters.get(l).keySet()) {
                System.out.print("\t" + l2);
            }
            System.out.println("");
        }
    }

    public void obtainSynonymsProposalCategories() throws Exception {
        Map<String, Map<String, String>> clusters = new HashMap<>();

        List<String> _tags = new ArrayList<>(this.tagCategories.keySet());
        Collections.sort(_tags);

        String query = "SELECT id, title FROM proposals";
        ResultSet rs = this.db.executeSelect(query);
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

            String lastTag = null;
            for (int t = _tags.size() - 1; t >= 0; t--) {
                String tag = _tags.get(t);
                if (title.contains(" " + tag + " ")) {

                    if (lastTag != null && lastTag.contains(tag)) {
                        if (!clusters.containsKey(lastTag)) {
                            clusters.put(lastTag, new HashMap<>());
                        }
                        clusters.get(lastTag).put(tag, null);
                    }
                    lastTag = tag;
                }
            }
        }
        rs.close();

        List<String> list = new ArrayList<>(clusters.keySet());
        Collections.sort(list);
        System.out.println("***** SYNONYMS FROM CATEGORIES ");
        for (String l : list) {
            System.out.print(l);
            for (String l2 : clusters.get(l).keySet()) {
                System.out.print("\t" + l2);
            }
            System.out.println("");
        }
    }

    private void obtainSynonymTopics(String file1, String file2) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), "UTF-8"));
        List<String> topics1 = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            topics1.add(line.trim());
        }
        Map<String, List<String>> topics2 = new HashMap<>();
        for (int i = 0; i < topics1.size(); i++) {
            String t1 = topics1.get(i);
            topics2.put(t1, new ArrayList<>());
            for (int j = i + 1; j < topics1.size(); j++) {
                String t2 = topics1.get(j);
                if (t2.contains(t1)) {
                    topics2.get(t1).add(t2);
                    i = j;
                } else {
                    break;
                }
            }
        }
        reader.close();
        List<String> _topics = new ArrayList<>(topics2.keySet());
        Collections.sort(_topics);
        for (String t1 : _topics) {
            System.out.print(t1);
            for (String t2 : topics2.get(t1)) {
                System.out.print(";" + t2);
            }
            System.out.println("");
        }
    }

    public static void main(String[] args) {
        try {
            DMCategoryProcessor processor = new DMCategoryProcessor();
            //processor.processProposalTags(); // to be executed once
            //processor.processProposalCategories(); // no
            //processor.processProposalTopics();// no
            //processor.obtainSynonymsProposalCategories(); // no
            ///processor.obtainSynonymsProposalTopics();  // no
            //processor.obtainSynonymTopics("./data_kmi/topics1.txt", "./data_kmi/topics2.txt"); // file1 contains the results of "SELECT DISTINCT topic FROM proposal_topics"            
            //            
            processor.generateTopics("./data_kmi/topics4.txt"); // to be executed once
            processor.processProposalTopics2(); // to be executed once
            processor.processProposalCategories2(); // to be executed once
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateTopics(String file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        String category = null;
        int idCategory = 0;
        int idTopic = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                String tokens[] = line.split("\t");
                category = tokens[2].trim();
                idCategory++;
                idTopic = idCategory * 100;
            } else {
                String tokens[] = line.split(";");
                String topic = tokens[0];
                idTopic++;
                String query = "INSERT INTO cat_topics VALUES(" + idTopic + ",\"" + topic + "\",\"" + category + "\")";
                this.db.executeInsert(query, false, null);

                for (int i = 0; i < tokens.length; i++) {
                    String tag = tokens[i];
                    query = "INSERT INTO cat_topic_tags VALUES(\"" + tag + "\",\"" + topic + "\",\"" + category + "\")";
                    this.db.executeInsert(query, false, null);
                }
            }
        }
        reader.close();
    }

    public void processProposalTopics2() throws Exception {
        Map<String, String> tagTopic2 = new HashMap<>();
        String query = "SELECT * FROM cat_topic_tags";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String tag = rs.getString("tag");
            String topic = rs.getString("topic");
            String category = rs.getString("category");
            tagTopic2.put(tag, topic);
        }
        rs.close();

        Map<Integer, Map<String, Double>> proposalTopics = new HashMap<>();

        Map<Integer, Map<String, String>> sources = new HashMap<>();

        // 1. EXTRACTING TOPICS FROM TAGS        
        query = "SELECT * FROM proposal_tags";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String tag = rs.getString("tag");

            if (!tagTopic2.containsKey(tag)) {
//                System.out.println(tag);
                continue;
            }

            String topic = tagTopic2.get(tag);

            if (!proposalTopics.containsKey(id)) {
                proposalTopics.put(id, new HashMap<>());
            }
            if (!proposalTopics.get(id).containsKey(topic)) {
                proposalTopics.get(id).put(topic, 0.0);
            }

            double weight = 1.0 + proposalTopics.get(id).get(topic);
            proposalTopics.get(id).put(topic, weight);

            if (!sources.containsKey(id)) {
                sources.put(id, new HashMap<>());
            }
            sources.get(id).put(topic, "tag");
        }
        rs.close();

        // 2. EXTRACTING TOPICS FROM TITLES        
        List<String> _tags = new ArrayList<>(tagTopic2.keySet());
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

                    if (!tagTopic2.containsKey(tag)) {
                        continue;
                    }

                    String topic = tagTopic2.get(tag);

                    if (!proposalTopics.containsKey(id)) {
                        proposalTopics.put(id, new HashMap<>());
                    }
                    if (!proposalTopics.get(id).containsKey(topic)) {
                        proposalTopics.get(id).put(topic, 0.0);
                    }

                    double weight = 1.0 + proposalTopics.get(id).get(topic);
                    proposalTopics.get(id).put(topic, weight);

                    if (!sources.containsKey(id)) {
                        sources.put(id, new HashMap<>());
                    }

                    if (sources.get(id).containsKey(topic)) {
                        if (sources.get(id).get(topic).equals("tag")) {
                            sources.get(id).put(topic, "tag-title");
                        }
                    } else {
                        sources.get(id).put(topic, "title");
                    }
                }
            }
        }
        rs.close();

        // Removing topics which are sub-string of other topics
        for (int id : proposalTopics.keySet()) {
            List<String> toRemove = new ArrayList<>();
            List<String> _topics = new ArrayList<>(proposalTopics.get(id).keySet());
            for (int i = 0; i < _topics.size(); i++) {
                String t1 = _topics.get(i);
                for (int j = i + 1; j < _topics.size(); j++) {
                    String t2 = _topics.get(j);
                    if (t1.contains(t2)) {
                        if (!toRemove.contains(t2)) {
                            toRemove.add(t2);
                        }
                    } else if (t2.contains(t1)) {
                        if (!toRemove.contains(t1)) {
                            toRemove.add(t1);
                        }
                    }
                }
            }
            for( String t: toRemove ) {
                proposalTopics.get(id).remove(t);
            }
        }

        // Normalizing weights
        Map<Integer, Double> totalWeights = new HashMap<>();
        for (int id : proposalTopics.keySet()) {
            Map<String, Double> _topics = proposalTopics.get(id);
            double totalWeight = 0;
            for (String category : _topics.keySet()) {
                double weight = _topics.get(category);
                totalWeight += weight;
            }
            totalWeights.put(id, totalWeight);
        }

        for (int id : proposalTopics.keySet()) {
            double totalWeight = totalWeights.get(id);

            Map<String, Double> _topics = proposalTopics.get(id);
            List<String> __topics = new ArrayList<>(_topics.keySet());
            Collections.sort(__topics);
            for (String topic : __topics) {
                double weight = _topics.get(topic);

                String source = sources.get(id).get(topic);

                query = "INSERT INTO proposal_topics VALUES(" + id + ",\"" + topic + "\"," + weight + "," + weight / totalWeight + ",\"" + source + "\")";
                this.db.executeInsert(query, false, null);
            }
        }
    }

    public void processProposalCategories2() throws Exception {
        Map<String, String> topicCategory2 = new HashMap<>();
        String query = "SELECT * FROM cat_topic_tags";
        ResultSet rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            String tag = rs.getString("tag");
            String topic = rs.getString("topic");
            String category = rs.getString("category");
            topicCategory2.put(topic, category);
        }
        rs.close();

        Map<Integer, Map<String, Double>> proposalCategories = new HashMap<>();

        Map<Integer, Map<String, String>> sources = new HashMap<>();

        query = "SELECT * FROM proposal_topics";
        rs = this.db.executeSelect(query);
        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String topic = rs.getString("topic");
            double weight = rs.getDouble("n_weight");

            String source = rs.getString("source");

            String category = topicCategory2.get(topic);

            if (!proposalCategories.containsKey(id)) {
                proposalCategories.put(id, new HashMap<>());
            }
            if (!proposalCategories.get(id).containsKey(category)) {
                proposalCategories.get(id).put(category, 0.0);
            }
            weight += proposalCategories.get(id).get(category);
            proposalCategories.get(id).put(category, weight);

            if (!sources.containsKey(id)) {
                sources.put(id, new HashMap<>());
            }

            if (source.equals("tag-title")) {
                sources.get(id).put(category, source);
            } else if (!sources.get(id).containsKey(category)) {
                sources.get(id).put(category, source);
            } else if (!sources.get(id).get(category).equals(source)) {
                sources.get(id).put(category, "tag-title");
            }
        }
        rs.close();

        // Normalizing weights
        Map<Integer, Double> totalWeights = new HashMap<>();
        for (int id : proposalCategories.keySet()) {
            Map<String, Double> _categories = proposalCategories.get(id);
            double totalWeight = 0;
            for (String category : _categories.keySet()) {
                double weight = _categories.get(category);
                totalWeight += weight;
            }
            totalWeights.put(id, totalWeight);
        }

        for (int id : proposalCategories.keySet()) {
            double totalWeight = totalWeights.get(id);

            Map<String, Double> _categories = proposalCategories.get(id);
            List<String> __categories = new ArrayList<>(_categories.keySet());
            Collections.sort(__categories);
            for (String category : __categories) {
                double weight = _categories.get(category);

                String source = sources.get(id).get(category);

                query = "INSERT INTO proposal_categories VALUES(" + id + ",\"" + category + "\"," + weight + "," + weight / totalWeight + ",\"" + source + "\")";
                this.db.executeInsert(query, false, null);
            }
        }
    }
}
