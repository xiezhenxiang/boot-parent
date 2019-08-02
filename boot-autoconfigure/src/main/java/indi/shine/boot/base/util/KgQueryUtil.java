package indi.shine.boot.base.util;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import java.util.HashSet;
import java.util.Set;

/**
 * @author xiezhenxiang 2019/6/18
 */
public class KgQueryUtil {

    private static MongoUtil KG_MONGO;

    public KgQueryUtil(MongoUtil kgMongoUtil) {
        KG_MONGO = kgMongoUtil;
    }

    public String getKgDbName(String kgName) {

        String kgDbName = null;
        MongoCursor<Document> cursor = KG_MONGO.find("kg_attribute_definition", "kg_db_name", new Document("kg_name", kgName));
        if (cursor.hasNext()) {
            kgDbName = cursor.next().getString("db_name");
        }
        return kgDbName;
    }

    /**
     * 通过kgName得到属性定义表名
     **/
    public String getAttributeDefineColName(String kgName) {
        return getKgDbName(kgName) + "_attribute_definition";
    }

    /**
     * 获取实体的同义词
     * @author wangh 2019/7/3
     */
    public Set<String> getSynonymyById(String kgName,Set<Long> ids){
        MongoCursor<Document> iterator = KG_MONGO.find(getKgDbName(kgName), "entity_synonym", new Document("id", new Document("$in",ids)));
        Set<String> synonymy = new HashSet<>();
        while (iterator.hasNext()){
            Document next = iterator.next();
            synonymy.add(next.getString("synonym"));
        }
        iterator.close();
        return synonymy;
    }

    /**
     * 获取所有子概念ID（包括自身）
     * @author xiezhenxiang 2019/6/18
     **/
    public HashSet<Long> getSonConceptIds(String kgName, Long conceptId) {

        HashSet<Long> ls = Sets.newHashSet(conceptId);
        String kgDbName = getKgDbName(kgName);
        MongoCursor<Document> cursor = KG_MONGO.find(kgDbName, "parent_son", new Document("parent", conceptId));

        while (cursor.hasNext()) {
            ls.add(cursor.next().getLong("son"));
        }

        return ls;
    }

    /**
     * 获取所有父子相关概念ID（包括自身、父概念、子概念）
     * @author xiezhenxiang 2019/6/18
     **/
    public HashSet<Long> getParentSonConceptIds(String kgName, Long conceptId) {

        HashSet<Long> ls = Sets.newHashSet(conceptId);
        ls.addAll(getSonConceptIds(kgName, conceptId));
        String kgDbName = getKgDbName(kgName);
        long sonId = conceptId;

        while (true) {
            MongoCursor<Document> cursor = KG_MONGO.find(kgDbName, "parent_son", new Document("son", sonId));
            if (cursor.hasNext()) {
                Document doc = cursor.next();
                ls.add(doc.getLong("parent"));
                sonId = doc.getLong("parent");
            } else {
                break;
            }
        }

        return ls;
    }

    public String getConceptNameById(String kgName, long conceptId) {
        String kgDbName = getKgDbName(kgName);
        Document first = KG_MONGO.find(kgDbName, "basic_info", new Document("_id", conceptId)).next();
        if (first == null) {
            return "";
        }
        return first.getString("name");
    }

    public Long getConceptIdByName(String kgName, long parentId, String conceptName) {

        Document query = new Document("type", 0).append("name", conceptName).append("concept_id", parentId);

        String kgDbName = getKgDbName(kgName);
        Document first = KG_MONGO.find(kgDbName, "entity_id", query).next();

        if (first == null) {
            return null;
        }

        return first.getLong("id");
    }
}

