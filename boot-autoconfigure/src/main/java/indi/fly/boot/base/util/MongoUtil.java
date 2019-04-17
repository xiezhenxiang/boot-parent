package indi.fly.boot.base.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiezhenxiang 2019/4/17
 */
class MongoUtil {

    private static MongoClient mongoClient;

    private static final String MONGODB_URL = "127.0.0.1";
    private static final int MONGODB_PORT = 27017;
    private volatile static MongoClient client = null;

    public static MongoClient getMongoClient() {

        if (client == null) {
            synchronized (MongoUtil.class){
                if (client == null) {
                    try {
                        MongoClientOptions options = MongoClientOptions.builder()
                                .connectionsPerHost(20).minConnectionsPerHost(1)
                                .maxConnectionIdleTime(30000).maxConnectionLifeTime(180000)
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


    public static void insertMany(String database, String collection, List<Document> documentList) {
        MongoClient mongoClient = null;

        try {
            mongoClient = getMongoClient();
            mongoClient.getDatabase(database).getCollection(collection).insertMany(documentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertOne(String database, String collection, Document doc) {
        try {
            mongoClient = getMongoClient();
            mongoClient.getDatabase(database).getCollection(collection).insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateOne(String database, String collection, Document query, Document doc) {
        try {
            mongoClient = getMongoClient();
            mongoClient.getDatabase(database).getCollection(collection).updateOne(query, new Document("$set", doc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateMany(String database, String collection, Document query, Document doc) {
        try {
            mongoClient = getMongoClient();
            mongoClient.getDatabase(database).getCollection(collection).updateMany(query, new Document("$set", doc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MongoCursor<Document> find(String db, String col, Document query, Document sort) {
        MongoCursor<Document> mongoCursor = null;
        query = query == null ? new Document() : query;
        sort = sort == null ? new Document() : sort;
        try {
            mongoClient = getMongoClient();
            FindIterable<Document> findIterable = mongoClient.getDatabase(db).getCollection(col).find(query).sort(sort);
            mongoCursor = findIterable.iterator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mongoCursor;
    }

    public static MongoCursor<Document> find(String db, String col, Document query, Document sort, Integer pageNo, Integer pageSize) {
        MongoCursor<Document> mongoCursor = null;
        query = query == null ? new Document() : query;
        sort = sort == null ? new Document() : sort;
        try {
            mongoClient = getMongoClient();
            FindIterable<Document> findIterable = mongoClient.getDatabase(db).getCollection(col).find(query).sort(sort);
            if(pageNo != null)  findIterable.skip(pageNo);
            if(pageSize != null)    findIterable.limit(pageSize);
            mongoCursor = findIterable.iterator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mongoCursor;
    }

    public static void delete(String db, String col, Document query){
        MongoClient mongoClient = null;
        try {
            mongoClient = getMongoClient();
            mongoClient.getDatabase(db).getCollection(col).deleteMany(query);
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
}
