package indi.shine.boot.base.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOptions;
import indi.shine.boot.base.exception.ServiceException;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static indi.shine.boot.base.util.AlgorithmUtil.elfHash;

/**
 * @author xiezhenxiang 2019/4/17
 */
public class MongoUtil {

    private String ip;
    private Integer port;
    private volatile MongoClient client;
    private volatile Map<Integer, MongoClient> pool = new HashMap<>(10);

    public static MongoUtil getInstance(String ip, Integer port) {

        return new MongoUtil(ip, port);
    }

    private MongoUtil(String ip, Integer port) {

        this.ip = ip;
        this.port = port;
        initClient();
    }

    public MongoCursor<Document> find(String db, String col, Document query, Document sort) {

        return find(db, col, query, sort, null, null);
    }

    public  MongoCursor<Document> find(String db, String col, Document query) {

        return find(db, col, query, null, null, null);
    }

    public MongoCursor<Document> find(String db, String col, Document query, Document sort, Integer pageNo, Integer pageSize) {

        MongoCursor<Document> mongoCursor = null;
        query = query == null ? new Document() : query;
        sort = sort == null ? new Document() : sort;

        FindIterable<Document> findIterable = client.getDatabase(db).getCollection(col).find(query).sort(sort);
        if(pageNo != null) {
            pageNo = (pageNo - 1) * pageSize;
            findIterable.skip(pageNo);
        }
        if (pageSize != null) {
            findIterable.limit(pageSize);
        }
        mongoCursor = findIterable.noCursorTimeout(true).iterator();
        return mongoCursor;
    }

    public void insertMany(String database, String collection, List<Document> documentList) {

        if (documentList == null || documentList.isEmpty()) {
            return;
        }

        client.getDatabase(database).getCollection(collection).insertMany(documentList);
    }

    public void insertOne(String database, String collection, Document doc) {

        client.getDatabase(database).getCollection(collection).insertOne(doc);
    }

    public void updateOne(String database, String collection, Document query, Document doc) {

        client.getDatabase(database).getCollection(collection).updateOne(query, new Document("$set", doc));
    }

    public void upsertOne(String database, String collection, Document query, Document doc) {

        client.getDatabase(database).getCollection(collection).updateOne(query, doc, new UpdateOptions().upsert(true));
    }

    public void upsertMany(String database, String collection, List<Document> ls, boolean upsert, String... fieldArr) {

        if (ls == null || ls.isEmpty()) {
            return;
        }

        List<UpdateManyModel<Document>> requests = ls.stream().map(s -> new UpdateManyModel<Document>(
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
                new UpdateOptions().upsert(upsert)
        )).collect(Collectors.toList());

        client.getDatabase(database).getCollection(collection).bulkWrite(requests);
    }

    public void updateMany(String database, String collection, Document query, Document doc) {

        client.getDatabase(database).getCollection(collection).updateMany(query, new Document("$set", doc));
    }

    public Long count(String db, String col, Document query){

        return client.getDatabase(db).getCollection(col).count(query);
    }

    public List<Document> getIndex(String db, String col) {

        List<Document> indexLs = new ArrayList<>();
        MongoCursor<Document> cursor = client.getDatabase(db).getCollection(col).listIndexes().iterator();
        cursor.forEachRemaining(s -> indexLs.add((Document) s.get("key")));

        return indexLs;
    }

    public void delete(String db, String col, Document query){

        client.getDatabase(db).getCollection(col).deleteMany(query);
    }

    public void createIndex(String db, String col, Document... indexArr) {

        for (Document index : indexArr) {
            client.getDatabase(db).getCollection(col).createIndex(index);
        }
    }

    public void dropIndex(String db, String col, Document... indexArr) {

        for (Document index : indexArr) {
            client.getDatabase(db).getCollection(col).dropIndex(index);
        }
    }


    public synchronized void copyDataBase(String fromDbName, String toDbName) {

        MongoIterable<String> colNames = client.getDatabase(fromDbName).listCollectionNames();

        for (String colName : colNames) {
            copyCollection(fromDbName, colName, toDbName, colName);
        }
    }

    public synchronized void copyDataBase(String fromDbName, String toDbName, MongoUtil toMongoUtil) {

        MongoIterable<String> colNames = client.getDatabase(fromDbName).listCollectionNames();

        for (String colName : colNames) {
            copyCollection(fromDbName, colName, toMongoUtil, toDbName, colName);
        }
    }

    public void copyCollection(String fromDbName, String fromColName, String toDbName, String toColName) {

        copyCollection(this, fromDbName, fromColName, this, toDbName, toColName);
    }

    public void copyCollection(String fromDbName, String fromColName, MongoUtil toMongoUtil, String toDbName, String toColName) {

        copyCollection(this, fromDbName, fromColName, toMongoUtil, toDbName, toColName);
    }

    private void copyCollection(MongoUtil fromMongo, String fromDbName, String fromColName, MongoUtil toMongo, String toDbName, String toColName) {

        List<Document> indexLs = fromMongo.getIndex(fromDbName, fromColName);
        // 复制索引
        toMongo.createIndex(toDbName, toColName, (Document[]) indexLs.toArray());
        // 一万条批量插入
        int pageNo = 1, pageSize = 10000;
        while (true) {
            MongoCursor<Document> cursor = fromMongo.find(fromDbName, fromColName, null, null, pageNo, pageSize);
            if (!cursor.hasNext()) {
                break;
            }
            List<Document> docLs = new ArrayList<>();
            while (cursor.hasNext()) {
                docLs.add(cursor.next());
            }
            toMongo.insertMany(toDbName, toColName, docLs);
            pageNo ++;
        }
    }

    private void initClient() {

        if (client == null) {

            synchronized (MongoUtil.class){

                if (client == null) {
                    Integer key = elfHash(ip + port);
                    if (pool.containsKey(key)) {
                        client = pool.get(key);
                    } else {
                        try {
                            MongoClientOptions options = MongoClientOptions.builder()
                                    .connectionsPerHost(20)
                                    .minConnectionsPerHost(1)
                                    .maxConnectionIdleTime(0)
                                    .maxConnectionLifeTime(0)
                                    .connectTimeout(30000)
                                    .socketTimeout(120000)
                                    .build();

                            String[] ips = ip.split(",");
                            List<ServerAddress> urlList = new ArrayList<>();
                            for (String url : ips) {
                                urlList.add(new ServerAddress(url, port));
                            }

                            client = new MongoClient(urlList, options);
                            pool.put(key, client);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw ServiceException.newInstance(50051, "mongo connect error!");
                        }
                    }
                }
            }
        }
    }

    public MongoClient getClient() {
        return client;
    }
}
