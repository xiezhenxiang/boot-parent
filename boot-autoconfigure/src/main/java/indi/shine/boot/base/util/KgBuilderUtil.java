package indi.shine.boot.base.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.*;

/**
 * @author xiezhenxiang 2019/6/24
 */
public class KgBuilderUtil {

    private String kgDbName;
    public KgBuilderUtil(String kgName) {
        kgDbName = getKgDbName(kgName);
    }

    private List<Document> entityIdLs = new ArrayList<>();
    private List<Document> abstractLs = new ArrayList<>();
    private List<Document> entityImageLs = new ArrayList<>();
    private List<Document> basicInfoLs = new ArrayList<>();
    private List<Document> conceptInstanceLs = new ArrayList<>();
    private List<Document> synonymousLs = new ArrayList<>();
    private List<Document> parentSonLs = new ArrayList<>();
    private List<Document> attrSummaryLs = new ArrayList<>();
    private List<Document> attrObjectLs = new ArrayList<>();
    private List<Document> attrPrivateObjectLs = new ArrayList<>();
    private List<Document> attrStringLs = new ArrayList<>();
    private List<Document> attrFloatLs = new ArrayList<>();
    private List<Document> attrIntegerLs = new ArrayList<>();
    private List<Document> attrDateLs = new ArrayList<>();
    private List<Document> attrDefineLs = new ArrayList<>();
    private List<Document> attrTextLs = new ArrayList<>();
    private final static int THRESHOLD = 10000;

    /**
     * 创建概念
     * */
    public Long createConcept(Long parentId, String name, String meaningTag, String abs) {

        Long id = lastEntityId();
        Document doc = new Document("id", id).append("name", name).append("type", 0).append("concept_id", parentId);
        entityIdLs.add(doc);
        doc = new Document("_id", id).append("name", name).append("type", 0);
        if (meaningTag != null) {
            doc.append("meaning_tag", meaningTag);
        }
        basicInfoLs.add(doc);
        if (abs != null) {
            doc = new Document("id", id).append("abstract", abs);
            abstractLs.add(doc);
        }
        if (parentId != null) {
            doc = new Document("parent", parentId).append("son", id);
        }
        parentSonLs.add(doc);

        return id;
    }

    /**
     * 创建属性定义
     **/
    public void createAttrDefine(String name, String type, Integer dataType, Long domain, String range) {

        int id = lastAttrId();
        Document doc = new Document("id", id).append("name", name).append("type", type).append("domain_value", domain);
        if (dataType != null) {
            doc.append("data_type", dataType);
        }
        if(range != null) {
            doc.append("range_value", range);
        }
        Date time = TimeUtil.now();
        doc.append("is_functional", 0).append("direction", 0).append("creator", "admin").append("create_time", time)
                .append("modify_time", time).append("modifier", "admin");

        attrDefineLs.add(doc);
    }

    /**
     * 创建边属性定义
     **/
    public Integer createSideAttrDefine(String name, Integer type, Integer dataType, String range, Integer relationAttrId) {

        Document attr;
        Optional<Document> opt = attrDefineLs.stream().filter(s -> s.getInteger("id").equals(relationAttrId)).findFirst();

        if (opt.isPresent()) {
            attr = opt.get();
        } else {
            String defineColName = kgDbName + "_attribute_definition";
            attr = MongoUtil.find("kg_attribute_definition", defineColName, new Document("id", relationAttrId)).next();
        }
        List<JSONObject> extraInfo = new ArrayList<>();
        Integer attrId = 1;
        if (attr.containsKey("extra_info")) {
            extraInfo = JSONArray.parseArray(attr.getString("extra_info"), JSONObject.class);
            Optional<JSONObject> opt2 = extraInfo.stream().max(Comparator.comparingInt(s -> s.getInteger("seqNo")));
            if (opt2.isPresent()) {
                attrId = opt2.get().getInteger("seqNo") + 1;
            }
        }
        JSONObject obj = new JSONObject();
        obj.fluentPut("seqNo", attrId).fluentPut("name", name).fluentPut("type", type).fluentPut("indexed", 0);

        if (dataType != null) {
            obj.put("dataType", dataType);
        }

        if (range != null) {
            obj.put("objRange", range);
        }
        extraInfo.add(obj);
        attr.append("extra_info", extraInfo);

        attrDefineLs.add(attr);
        return attrId;
    }

    /**
     * 添加实体
     **/
    public Long addEntity(long conceptId, String name, String meaningTag, List<String> synonymList, String abs, String imgPath, Document meta_data){

        long id = lastEntityId();
        Document doc = new Document("_id", id).append("name", name);
        if (meta_data != null) {
            doc.append("meta_data", meta_data);
        }
        basicInfoLs.add(doc);

        doc = new Document("id", id).append("name", name.toLowerCase()).append("type", 1).append("concept_id", conceptId);
        if (meaningTag != null) {
            doc.append("meaning_tag", meaningTag);
        }
        if (meta_data != null) {
            doc.append("meta_data", meta_data);
        }
        entityIdLs.add(doc);

        doc = new Document("concept_id", conceptId).append("ins_id", id);
        conceptInstanceLs.add(doc);

        if (synonymList != null) {
            for (String synonym : synonymList) {
                doc = new Document("id", id).append("synonym", synonym);
                synonymousLs.add(doc);
            }
        }
        if (abs != null) {
            doc = new Document("id", id).append("abstract", abs);
            abstractLs.add(doc);
        }
        if (imgPath != null) {
            doc = new Document("id", id).append("image_url", imgPath);
            entityImageLs.add(doc);
        }
        if (entityIdLs.size() >= THRESHOLD) {
            bulkInsert();
        }

        return id;
    }

    public long addEntity(long conceptId, String name, String meaningTag, String abs) {
        return addEntity(conceptId, name, meaningTag, null, abs, null, null);
    }

    /**
     * 添加数值属性数据
     **/
    public void addBasicData(long conceptId, long entityId, int attrId, Object value, int type) {

        Document doc = new Document("entity_id", entityId).append("attr_id", attrId);
        attrSummaryLs.add(doc);
        doc = new Document("entity_id", entityId).append("entity_type", conceptId).append("attr_id", attrId).append("attr_value",  value);
        switch (type) {
            case 1:
            case 3:
                attrIntegerLs.add(doc);
                break;
            case 2:
                attrFloatLs.add(doc);
                break;
            case 4:
            case 41:
            case 42:
                attrDateLs.add(doc);
                break;
            case 5:
            case 51:
                attrStringLs.add(doc);
                break;
            case 10:
                attrTextLs.add(doc);
                break;
            default:
                break;
        }
        if (attrSummaryLs.size() >= THRESHOLD) {
            bulkInsert();
        }
    }

    /**
     * 添加关系属性数据
     */
    public void addRelation(long conceptId, long entityId, int attrId, long valueConceptId, long value){

        addSideRelation(conceptId, entityId, attrId, valueConceptId, value, null, null, null);
    }

    /**
     * 添加私有关系
     * @author xiezhenxiang 2019/7/21
     **/
    public void addPrivateRelation(long conceptId, long entityId, String attrName, long valueConceptId, long value) {

        Document doc = new Document("entity_id", entityId).append("entity_type", conceptId)
                .append("attr_name", attrName).append("attr_value", value).append("attr_value_type", valueConceptId);
        attrPrivateObjectLs.add(doc);
    }

    /**
     * 添加关系属性数据（带边属性）
     * @author xiezhenxiang 2019/7/15
     **/
    public void addSideRelation(long conceptId, long entityId, int attrId, long valueConceptId, long value,
                            List<Integer> sideAttrIds, List<Integer> sideAttrTypes, List<Object> sideAttrValues) {

        Document doc = new Document("entity_id", entityId).append("entity_type", conceptId).append("attr_id", attrId)
                .append("attr_value",  value).append("attr_value_type", valueConceptId);

        boolean flag = true;

        for (int i = 0; sideAttrIds !=null && i < sideAttrIds.size(); i ++) {
            if (sideAttrTypes.get(i) == 0) {
                doc.append("attr_ext_" + attrId + "_" + sideAttrIds.get(i), sideAttrValues.get(i));
            } else {
                MongoUtil.insertOne(kgDbName, "attribute_object", doc);
                Document objExt = new Document("triple_id", doc.get("_id").toString())
                        .append("attr_id", attrId + "_" + sideAttrIds.get(i))
                        .append("ext_id", sideAttrValues.get(i));
                MongoUtil.insertOne(kgDbName, "attribute_object_ext", objExt);
                flag = false;
            }
        }
        if (flag) {
            attrObjectLs.add(doc);
            if (attrObjectLs.size() >= THRESHOLD){
                bulkInsert();
            }
        }
    }



    private Long lastEntityId() {

        long id = 0L;
        if (!basicInfoLs.isEmpty()) {
            id = basicInfoLs.get(basicInfoLs.size() - 1).getLong("_id") + 1;
        } else {
             MongoCursor<Document> cursor = MongoUtil.find(kgDbName, "basic_info", null, new Document("_id", -1), 1, 1);
             if (cursor.hasNext()) {
                 id = cursor.next().getLong("_id") + 1;
             }
        }
        return id;
    }

    private int lastAttrId() {

        int id = 1;
        if (!attrDefineLs.isEmpty()) {
            id = attrDefineLs.get(attrDefineLs.size() - 1).getInteger("id") + 1;
        } else {
            String defineColName = kgDbName + "_attribute_definition";
            MongoCursor<Document> cursor = MongoUtil.find("kg_attribute_definition", defineColName, null, new Document("_id", -1), 1, 1);
            if (cursor.hasNext()) {
                id = cursor.next().getInteger("id") + 1;
            }
        }
        return id;
    }


    public void bulkInsert(){

        if (entityIdLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "entity_id", entityIdLs);
            entityIdLs.clear();
        }
        if (basicInfoLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "basic_info", basicInfoLs);
            basicInfoLs.clear();
        }
        if (!abstractLs.isEmpty()) {
            MongoUtil.insertMany(kgDbName, "entity_abstract", abstractLs);
            abstractLs.clear();
        }
        if (!attrDefineLs.isEmpty()) {
            String colName = kgDbName + "_attribute_definition";
            MongoUtil.upsertMany("kg_attribute_definition", colName, attrDefineLs, true, "id");
            attrDefineLs.clear();
        }
        if (parentSonLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "parent_son", parentSonLs);
            parentSonLs.clear();
        }
        if (attrSummaryLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "attribute_summary", attrSummaryLs);
            attrSummaryLs.clear();
        }
        if (attrObjectLs.size() > 0) {
            MongoUtil.upsertMany(kgDbName, "attribute_object", attrObjectLs, true,"entity_id", "attr_id", "attr_value");
            attrObjectLs.clear();
        }
        if (attrPrivateObjectLs.size() > 0) {
            MongoUtil.upsertMany(kgDbName, "attribute_private_object", attrPrivateObjectLs, true,"entity_id", "attr_name", "attr_value");
            attrPrivateObjectLs.clear();
        }
        if (attrStringLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "attribute_string", attrStringLs);
            attrStringLs.clear();
        }
        if (attrFloatLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "attribute_float", attrFloatLs);
            attrFloatLs.clear();
        }
        if (attrIntegerLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "attribute_integer", attrIntegerLs);
            attrIntegerLs.clear();
        }
        if (attrDateLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "attribute_date_time", attrDateLs);
            attrDateLs.clear();
        }
        if (entityImageLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "entity_image", entityImageLs);
            entityImageLs.clear();
        }
        if (conceptInstanceLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "concept_instance", conceptInstanceLs);
            conceptInstanceLs.clear();
        }
        if (synonymousLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "entity_synonym", synonymousLs);
            synonymousLs.clear();
        }
        if (attrTextLs.size() > 0) {
            MongoUtil.insertMany(kgDbName, "attribute_text", attrTextLs);
            attrTextLs.clear();
        }

    }

    private String getKgDbName(String kgName) {

        String kgDbName = null;
        MongoCursor<Document> cursor = MongoUtil.find("kg_attribute_definition", "kg_db_name", new Document("kg_name", kgName));
        if (cursor.hasNext()) {
            kgDbName = cursor.next().getString("db_name");
        }
        return kgDbName;
    }
}
