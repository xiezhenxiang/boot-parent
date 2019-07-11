package indi.shine.boot.base.util;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author xiezhenxiang 2019/4/17
 */
class MongoUtil {

    private static final String MONGODB_URL = "127.0.0.1";
    private static final int MONGODB_PORT = 27017;
    private volatile static MongoClient client = null;


    public static MongoCursor<Document> find(String db, String col, Document query, Document sort) {

        return find(db, col, query, sort, null, null);
    }

    public static MongoCursor<Document> find(String db, String col, Document query) {

        return find(db, col, query, null, null, null);
    }

    public static MongoCursor<Document> find(String db, String col, Document query, Document sort, Integer pageNo, Integer pageSize) {

        getMongoClient();
        MongoCursor<Document> mongoCursor = null;
        query = query == null ? new Document() : query;
        sort = sort == null ? new Document() : sort;

        FindIterable<Document> findIterable = client.getDatabase(db).getCollection(col).find(query).sort(sort);
        if(pageNo != null && pageSize != null) {
            pageNo = (pageNo - 1) * pageSize;
            findIterable.skip(pageNo);
            findIterable.limit(pageSize);
        }
        mongoCursor = findIterable.noCursorTimeout(true).iterator();
        return mongoCursor;
    }

    public static void insertMany(String database, String collection, List<Document> documentList) {

        getMongoClient();
        if (documentList == null || documentList.isEmpty()) {
            return;
        }

        client.getDatabase(database).getCollection(collection).insertMany(documentList);
    }

    public static void insertOne(String database, String collection, Document doc) {

        getMongoClient();
        client.getDatabase(database).getCollection(collection).insertOne(doc);
    }

    public static void updateOne(String database, String collection, Document query, Document doc) {

        getMongoClient();
        client.getDatabase(database).getCollection(collection).updateOne(query, new Document("$set", doc));
    }

    public static void upsertOne(String database, String collection, Document query, Document doc) {

        getMongoClient();
        client.getDatabase(database).getCollection(collection).updateOne(query, doc, new UpdateOptions().upsert(true));
    }

    public static void upsertMany(String database, String collection, List<Document> ls, String... fieldArr) {

        getMongoClient();
        List<UpdateOneModel<Document>> requests = ls.stream().map(s -> new UpdateOneModel<Document>(
                new Bson() {
                    @Override
                    public <TDocument> BsonDocument toBsonDocument(Class<TDocument> aClass, CodecRegistry codecRegistry) {
                        Document doc = new Document();
                        for (String field : fieldArr) {
                            doc.append(field, s.get(field));
                        }
                        return doc.toBsonDocument(aClass, codecRegistry);
                    }
                },
                new Document("$set",s),
                new UpdateOptions().upsert(true)
        )).collect(Collectors.toList());

        client.getDatabase(database).getCollection(collection).bulkWrite(requests);
    }

    public static void updateMany(String database, String collection, Document query, Document doc) {

        getMongoClient();
        client.getDatabase(database).getCollection(collection).updateMany(query, new Document("$set", doc));
    }

    public static Long count(String db, String col, Document query){

        getMongoClient();
        return client.getDatabase(db).getCollection(col).count(query);
    }

    public static List<Document> getIndex(String db, String col) {

        getMongoClient();
        List<Document> indexLs = new ArrayList<>();
        MongoCursor<Document> cursor = client.getDatabase(db).getCollection(col).listIndexes().iterator();
        cursor.forEachRemaining(s -> indexLs.add((Document) s.get("key")));

        return indexLs;
    }

    public static void delete(String db, String col, Document query){

        getMongoClient();
        client.getDatabase(db).getCollection(col).deleteMany(query);
    }

    public static void createIndex(String db, String col, List<Document> index) {

        getMongoClient();

        if (index.isEmpty()) {
            return;
        }

        List<IndexModel> ls = index.stream().map(IndexModel::new).collect(toList());
        client.getDatabase(db).getCollection(col).createIndexes(ls);
    }


    public static synchronized void copyDataBase(String fromDbName, String toDbName) {

        getMongoClient();
        MongoIterable<String> colNames = client.getDatabase(fromDbName).listCollectionNames();

        for (String colName : colNames) {
            copyCollection(fromDbName, colName, toDbName, colName);
        }
    }

    public static void copyCollection(String fromDbName, String fromColName, String toDbName, String toColName) {

        List<Document> indexLs = getIndex(fromDbName, fromColName);
        // 复制索引
        createIndex(toDbName, toColName, indexLs);
        // 一万条批量插入
        int pageNo = 1, pageSize = 10000;
        while (true) {
            MongoCursor<Document> cursor = find(fromDbName, fromColName, null, null, pageNo, pageSize);
            if (!cursor.hasNext()) {
                break;
            }
            List<Document> docLs = new ArrayList<>();
            while (cursor.hasNext()) {
                docLs.add(cursor.next());
            }
            insertMany(toDbName, toColName, docLs);
            pageNo ++;
        }
    }

    public static MongoClient getMongoClient() {

        if (client == null) {
            synchronized (MongoUtil.class){
                if (client == null) {

                    MongoClientOptions options = MongoClientOptions.builder()
                            .connectionsPerHost(20)
                            .minConnectionsPerHost(1)
                            .maxConnectionIdleTime(0)
                            .maxConnectionLifeTime(0)
                            .connectTimeout(30000)
                            .socketTimeout(120000)
                            .build();

                    String[] urls = MONGODB_URL.split(",");
                    List<ServerAddress> urlList = new ArrayList<>();
                    for (String url : urls) {
                        urlList.add(new ServerAddress(url, MONGODB_PORT));
                    }

                    client = new MongoClient(urlList, options);
                }
            }
        }

        return client;
    }
}
