package indi.shine.boot.base.util;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
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

    public static void insertMany(String database, String collection, List<Document> documentList) {
        getMongoClient();
        if (documentList == null || documentList.isEmpty()) {
            return;
        }
        try {
            client.getDatabase(database).getCollection(collection).insertMany(documentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertOne(String database, String collection, Document doc) {
        getMongoClient();
        try {
            client.getDatabase(database).getCollection(collection).insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateOne(String database, String collection, Document query, Document doc) {
        getMongoClient();
        try {
            client.getDatabase(database).getCollection(collection).updateOne(query, new Document("$set", doc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void upsertOne(String database, String collection, Document query, Document doc) {
        getMongoClient();
        try {
            client.getDatabase(database).getCollection(collection).updateOne(query, doc, new UpdateOptions().upsert(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void upsertManey(String database, String collection, List<Document> ls, List<String> fields) {
        getMongoClient();
        List<UpdateOneModel<Document>> requests = ls.stream().map(s -> new UpdateOneModel<Document>(
            new Bson() {
                @Override
                public <TDocument> BsonDocument toBsonDocument(Class<TDocument> aClass, CodecRegistry codecRegistry) {
                    Document doc = new Document();
                    for (String field : fields) {
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
        try {
            client.getDatabase(database).getCollection(collection).updateMany(query, new Document("$set", doc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        try {
            FindIterable<Document> findIterable = client.getDatabase(db).getCollection(col).find(query).sort(sort);
            if(pageNo != null) {
                pageNo = (pageNo - 1) * pageSize;
                findIterable.skip(pageNo);
            }
            if(pageSize != null) {
                findIterable.limit(pageSize);
            }
            mongoCursor = findIterable.noCursorTimeout(true).iterator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mongoCursor;
    }

    public static void delete(String db, String col, Document query){
        getMongoClient();
        try {
            client.getDatabase(db).getCollection(col).deleteMany(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Long count(String db, String col, Document query){
        long count = 0L;
        MongoClient mongoClient;
        try {
            mongoClient = getMongoClient();
            count = mongoClient.getDatabase(db).getCollection(col).count(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static List<Document> getIndex(String db, String col) {
        getMongoClient();
        List<Document> indexs = Lists.newArrayList();
        MongoCursor<Document> cursor = client.getDatabase(db).getCollection(col).listIndexes().iterator();
        cursor.forEachRemaining(s -> indexs.add((Document) s.get("key")));
        return indexs;
    }

    public static void createIndex(String db, String col, List<Document> index) {
        if (index.isEmpty()) {
            return;
        }
        getMongoClient();
        List<IndexModel> ls = index.stream().map(IndexModel::new).collect(toList());
        client.getDatabase(db).getCollection(col).createIndexes(ls);
    }

    public static MongoClient getMongoClient() {

        if (client == null) {
            synchronized (MongoUtil.class){
                if (client == null) {
                    try {
                        MongoClientOptions options = MongoClientOptions.builder()
                                .connectionsPerHost(20).minConnectionsPerHost(1)
                                .maxConnectionIdleTime(0).maxConnectionLifeTime(0)
                                .connectTimeout(30000).socketTimeout(120000).build();
                        String[] urls = MONGODB_URL.split(",");
                        List<ServerAddress> urlList = new ArrayList<>();
                        for (String url : urls) {
                            urlList.add(new ServerAddress(url, MONGODB_PORT));
                        }
                        client = new MongoClient(urlList, options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return client;
    }
}
