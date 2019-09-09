package indi.shine.boot.base.util.database;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import indi.shine.boot.base.exception.ServiceException;
import indi.shine.boot.base.util.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.assertj.core.util.Lists;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Low Level RestClient support es 5.x
 * @author xiezhenxiang 2019/9/7
 **/
@Slf4j
public class EsRestClient {

    private RestClient client;
    private HttpHost[] hosts;
    private volatile static ConcurrentHashMap<String, RestClient> pool = new ConcurrentHashMap<>(10);

    public static EsRestClient getInstance(HttpHost... hosts) {

        if (hosts.length == 0) {
            throw ServiceException.newInstance(60000, "elastic host is empty!");
        }
        return new EsRestClient(hosts);
    }

    public void createIndex(String index, String mapping) {

        initClient();
        String endpoint ="/" + index;
        NStringEntity entity = new NStringEntity(mapping, ContentType.APPLICATION_JSON);
        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic create index fail!");
        }
    }

    public void deleteIndex(String index) {

        initClient();
        String endpoint ="/" + index;
        try {
            client.performRequest("DELETE", endpoint);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic delete index fail!");
        }
    }

    public void addAlias(String index, String ... aliases) {
        actionAlias(index, "add", aliases);
    }

    public void removeAlias(String index, String ... aliases) {
        actionAlias(index, "remove", aliases);
    }

    private void actionAlias(String index, String action, String... aliases) {

        initClient();
        JSONArray actions = new JSONArray();

        Arrays.stream(aliases).forEach(s -> {

            JSONObject obj = new JSONObject();
            obj.put("index", index);
            obj.put("alias", s);
            JSONObject actionObj = new JSONObject();
            actionObj.put(action, obj);

            actions.add(actionObj);
        });

        JSONObject paraData = new JSONObject();
        paraData.put("actions", actions);

        String endpoint ="/_aliases";
        NStringEntity entity = new NStringEntity(paraData.toJSONString(), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic " + action + " alias fail!");
        }
    }

    public void deleteByQuery(String index, String type, String queryStr) {

        initClient();
        JSONObject paraData = new JSONObject();
        paraData.put("query", JSONObject.parseObject(queryStr));

        String endpoint = StringUtils.isBlank(type) ? "/" + index : "/" + index + "/" + type;
        endpoint += "/_delete_by_query?refresh&conflicts=proceed";

        NStringEntity entity = new NStringEntity(paraData.toJSONString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic delete_by_query fail!");
        }
    }

    public void upsertById(String index, String type, String id, JSONObject doc) {

        initClient();
        String endpoint = "/" + index + "/" + type + "/" + id;

        NStringEntity entity = new NStringEntity(doc.toJSONString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic index doc fail!");
        }
    }

    public JSONObject getById(String index, String type, String id) {

        initClient();
        JSONObject doc;
        String endpoint = "/" + index + "/" + type + "/" + id;

        try {
            Response response = client.performRequest("GET", endpoint);
            String str = EntityUtils.toString(response.getEntity(), "utf-8");
            JSONObject rs = JSONObject.parseObject(str);
            doc = rs.getJSONObject("_source");
            doc.put("_id", rs.getString("_id"));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic index doc fail!");
        }

        return doc;
    }

    public void insert(String index, String type, JSONObject doc) {

        initClient();
        String endpoint = "/" + index + "/" + type;

        NStringEntity entity = new NStringEntity(doc.toJSONString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic index doc fail!");
        }
    }

    private void bulkInsert(String index, String type, Collection<JSONObject> ls) {

        initClient();
        if (ls.isEmpty()) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        ls.forEach(s -> {
            long id = Snowflake.nextId();
            buffer.append("{\"create\":{\"_index\":\""+ index +"\",\"_type\":\"" + type + "\",\"_id\":\""+ id + "\" } }\n");
            buffer.append(s.toJSONString() + "\n");
        });

        String endpoint = "/_bulk";

        NStringEntity entity = new NStringEntity(buffer.toString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic bulk create fail!");
        }
    }

    public void updateOne() {

        initClient();
    }

    /**
     * 根据id批量更新
     * @author xiezhenxiang 2019/9/9
     **/
    public void bulkUpsert(String index, String type, Collection<JSONObject> ls) {

        initClient();
        if (ls.isEmpty()) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        ls.forEach(s -> {

            String id = s.getString("_id");
            if (id != null) {
                buffer.append("{\"index\":{\"_index\":\"" + index + "\",\"_type\":\"" + type + "\",\"_id\":\"" + id + "\"}}\n");
                s.remove("_id");
                buffer.append(s.toJSONString() + "\n");
            }
        });

        String endpoint = "/_bulk";

        NStringEntity entity = new NStringEntity(buffer.toString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw ServiceException.newInstance(60000, "elastic bulk index fail!");
        }
    }

    public void bulkUpSert() {

        initClient();
    }

    public void find(String index, String type, Integer pageNo, Integer pageSize, String query, String sort) {

        initClient();
        HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory consumerFactory =
                new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(200 * 1024 * 1024);
    }

    public static void main(String[] args) {

        EsRestClient restClient = getInstance(new HttpHost("192.168.4.11", 9200));
        JSONObject doc = new JSONObject();
        doc.put("name", "xzxxzx2");
        doc.put("_id", "1");
        List<JSONObject> ls = Lists.newArrayList();
        ls.add(doc);
        // restClient.bulkUpdate("kg_gw_help_test", "data", ls);
        // restClient.insert("kg_gw_help_test", "data", doc);
        // System.out.println(restClient.getById("kg_gw_help_test", "data", "1").toJSONString());
    }

    private EsRestClient(HttpHost... hosts) {

        this.hosts = hosts;
        initClient();
    }

    private void initClient() {

        if (client == null) {
            synchronized (EsRestClient.class){
                if (client == null) {

                    String key = hostStr();

                    if (pool.containsKey(key)) {

                        client = pool.get(key);
                        if (client == null) {
                            pool.remove(key);
                        } else {
                            return;
                        }
                    }
                    client = RestClient.builder(hosts)
                            .setMaxRetryTimeoutMillis(1000 * 60)
                            .setRequestConfigCallback(request -> {
                                request.setConnectTimeout(1000 * 5);
                                request.setConnectionRequestTimeout(1000 * 5);
                                request.setSocketTimeout(1000 * 60);
                                return request;
                            })
                            .setHttpClientConfigCallback(s ->
                                s.setDefaultIOReactorConfig(IOReactorConfig.custom()
                                        .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                                        .setConnectTimeout(1000 * 5)
                                        .setSoTimeout(1000 * 60)
                                        .build())
                            )
                            .build();
                    pool.put(key, client);
                }
            }
        }
    }

    private String hostStr() {

        String str = "";

        for (int i = 0; i < hosts.length; i ++) {
            str += hosts[i].toHostString() + "_";
        }
        return str.substring(0, str.length() - 1);
    }

    public RestClient getClient() {

        initClient();
        return client;
    }

    public void close () {

        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
