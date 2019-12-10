package indi.shine.boot.base.util.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOptions;
import indi.shine.boot.base.exception.ServiceException;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiezhenxiang 2019/4/17
 */
public class MongoUtil {

    private String ip;
    private List<ServerAddress> urlList;
    private volatile MongoClient client;
    private static volatile Map<String, MongoClient> pool = new HashMap<>(10);
    private Integer batchSize = 3000;

    public MongoUtil(String ip, Integer port) {

        this.ip = ip;
        String[] ips = ip.split(",");
        urlList = new ArrayList<>();
        for (String one : ips) {
            urlList.add(new ServerAddress(one, port));
        }
        initClient();
    }

    public MongoUtil(String hosts) {

        StringBuilder ipStr = new StringBuilder();
        urlList = new ArrayList<>();
        String[] hostArr = hosts.split(",");
        for (String one : hostArr) {
            String[] ipPort = one.split(":");
            ipStr.append(ipPort[0]).append(",");
            urlList.add(new ServerAddress(ipPort[0], Integer.parseInt(ipPort[1])));
        }
        this.ip = ipStr.substring(0, ipStr.length() - 1);
        initClient();
    }

    public MongoUtil(MongoClient mongoClient) {

        this.client = mongoClient;
    }

    public MongoCursor<Document> find(String db, String col, Bson query, Bson sort) {

        return find(db, col, query, sort, null, null);
    }

    public MongoCursor<Document> find(String db, String col, Bson query) {

        return find(db, col, query, null, null, null);
    }

    public MongoCursor<Document> find(String db, String col, Bson query, Bson sort, Integer pageNo, Integer pageSize) {

        initClient();
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
        return findIterable.batchSize(batchSize).maxAwaitTime(10L, TimeUnit.MINUTES).iterator();
    }

    public MongoCursor<Document> aggregate(String db, String col, List<Bson> aggLs) {

        initClient();
        return client.getDatabase(db).getCollection(col).aggregate(aggLs).useCursor(true).batchSize(batchSize).useCursor(true)
                .maxTime(10L, TimeUnit.MINUTES).iterator();
    }

    public void insertMany(String database, String collection, List<Document> documentList) {

        initClient();
        if (documentList == null || documentList.isEmpty()) {
            return;
        }

        client.getDatabase(database).getCollection(collection).insertMany(documentList);
    }

    public void insertOne(String database, String collection, Document doc) {

        initClient();
        client.getDatabase(database).getCollection(collection).insertOne(doc);
    }

    public void updateOne(String database, String collection, Bson query, Document doc) {

        initClient();
        client.getDatabase(database).getCollection(collection).replaceOne(query, new Document("$set", doc));
    }

    public void upsertOne(String database, String collection, Bson query, Document doc) {

        initClient();
        client.getDatabase(database).getCollection(collection).replaceOne(query, doc, new UpdateOptions().upsert(true));
    }

    public void upsertMany(String database, String collection, List<Document> ls, boolean upsert, String... fieldArr) {

        initClient();
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

    public void updateMany(String database, String collection, Bson query, Document doc) {

        initClient();
        client.getDatabase(database).getCollection(collection).updateMany(query, new Document("$set", doc));
    }

    public Long count(String db, String col, Bson query){

        initClient();
        return client.getDatabase(db).getCollection(col).count(query);
    }

    public List<Document> getIndex(String db, String col) {

        initClient();
        List<Document> indexLs = new ArrayList<>();
        MongoCursor<Document> cursor = client.getDatabase(db).getCollection(col).listIndexes().iterator();
        cursor.forEachRemaining(s -> indexLs.add((Document) s.get("key")));

        return indexLs;
    }

    public void delete(String db, String col, Bson query){

        initClient();
        client.getDatabase(db).getCollection(col).deleteMany(query);
    }

    public void createIndex(String db, String col, Document... indexArr) {

        initClient();
        for (Document index : indexArr) {
            client.getDatabase(db).getCollection(col).createIndex(index);
        }
    }

    public void dropIndex(String db, String col, Document... indexArr) {

        initClient();
        for (Document index : indexArr) {
            client.getDatabase(db).getCollection(col).dropIndex(index);
        }
    }


    public synchronized void copyDataBase(String fromDbName, String toDbName) {

        initClient();
        MongoIterable<String> colNames = client.getDatabase(fromDbName).listCollectionNames();

        for (String colName : colNames) {
            copyCollection(fromDbName, colName, toDbName, colName);
        }
    }

    public synchronized void copyDataBase(String fromDbName, String toDbName, MongoUtil toMongoUtil) {

        initClient();
        MongoIterable<String> colNames = client.getDatabase(fromDbName).listCollectionNames();

        for (String colName : colNames) {
            copyCollection(fromDbName, colName, toMongoUtil, toDbName, colName);
        }
    }

    public void copyCollection(String fromDbName, String fromColName, String toDbName, String toColName) {

        initClient();
        copyCollection(this, fromDbName, fromColName, this, toDbName, toColName);
    }

    public void copyCollection(String fromDbName, String fromColName, MongoUtil toMongoUtil, String toDbName, String toColName) {

        initClient();
        copyCollection(this, fromDbName, fromColName, toMongoUtil, toDbName, toColName);
    }

    private void copyCollection(MongoUtil fromMongo, String fromDbName, String fromColName, MongoUtil toMongo, String toDbName, String toColName) {

        initClient();
        List<Document> indexLs = fromMongo.getIndex(fromDbName, fromColName);

        toMongo.createIndex(toDbName, toColName,  indexLs.toArray(new Document[0]));

        MongoCursor<Document> cursor = fromMongo.find(fromDbName, fromColName, null, null);

        List<Document> docLs = new ArrayList<>();

        cursor.forEachRemaining(doc -> {

            docLs.add(doc);
            if (docLs.size() >= batchSize) {
                toMongo.insertMany(toDbName, toColName, docLs);
                docLs.clear();
            }
        });

        toMongo.insertMany(toDbName, toColName, docLs);
    }

    public String uploadFile(String fileDatabase, String fileCol, String fileName, InputStream in) {

        initClient();
        GridFSBucket bucket = GridFSBuckets.create(client.getDatabase(fileDatabase), fileCol);
        ObjectId fileId = bucket.uploadFromStream(fileName, in);
        return fileId.toString();
    }

    public void downloadFile(String fileDatabase, String fileCol, String id, OutputStream out) {

        initClient();
        GridFSBucket bucket = GridFSBuckets.create(client.getDatabase(fileDatabase), fileCol);
        bucket.downloadToStream(new ObjectId(id), out);
    }

    public void downloadFile(String fileDatabase, String fileCol, String id, File outFile) {

        initClient();
        OutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            downloadFile(fileDatabase, fileCol, id, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public InputStream downloadFile(String fileDatabase, String fileCol, String id) {

        initClient();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        downloadFile(fileDatabase, fileCol, id, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    public void deleteFile(String fileDatabase, String fileCol, String id) {

        initClient();
        GridFSBucket bucket = GridFSBuckets.create(client.getDatabase(fileDatabase), fileCol);
        bucket.delete(new ObjectId(id));
    }


    private void initClient() {

        if (client == null) {

            synchronized (MongoUtil.class){

                if (client == null) {

                    if (pool.containsKey(ip)) {
                        client = pool.get(ip);
                        if (client == null) {
                            pool.remove(ip);
                        } else {
                            return;
                        }
                    }
                    try {
                        MongoClientOptions options = MongoClientOptions.builder()
                                .connectionsPerHost(20)
                                .minConnectionsPerHost(1)
                                .maxConnectionIdleTime(0)
                                .maxConnectionLifeTime(0)
                                .connectTimeout(30000)
                                .socketTimeout(120000)
                                .build();

                        client = new MongoClient(urlList, options);
                        pool.put(ip, client);

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("mongodb connect error!");
                    }
                }
            }
        }
    }

    public MongoClient getClient() {

        initClient();
        return client;
    }

    public void bachSize(int batch) {

        this.batchSize = batch;
    }
}
