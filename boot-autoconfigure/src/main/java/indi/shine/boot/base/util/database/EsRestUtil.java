package indi.shine.boot.base.util.database;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import indi.shine.boot.base.exception.ServiceException;
import indi.shine.boot.base.util.Snowflake;
import indi.shine.boot.base.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Low Level RestClient support es 5.x
 * @author xiezhenxiang 2019/9/7
 **/
@Slf4j
public class EsRestUtil {

    private RestClient client;
    private HttpHost[] hosts;
    private volatile static ConcurrentHashMap<String, RestClient> pool = new ConcurrentHashMap<>(10);

    private static HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory consumerFactory =
            new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(200 * 1024 * 1024);

    public static EsRestUtil getInstance(HttpHost... hosts) {

        if (hosts.length == 0) {
            throw ServiceException.newInstance(60000, "elastic host is empty!");
        }
        return new EsRestUtil(hosts);
    }

    public void createIndex(String index, String mapping) {

        initClient();
        String endpoint ="/" + index;
        NStringEntity entity = new NStringEntity(mapping, ContentType.APPLICATION_JSON);
        try {   
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic create index fail!");
        }
    }

    public void deleteIndex(String index) {

        initClient();
        String endpoint ="/" + index;
        try {
            client.performRequest("DELETE", endpoint);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic delete index fail!");
        }
    }

    /**
     * add a new type to an existing index
     * @author xiezhenxiang 2019/9/12
     **/
    public void createMappings(String index, String mappings) {

        initClient();
        JSONObject para = new JSONObject();
        para.put("mappings", JSONObject.parseObject(mappings));

        String endpoint ="/" + index;
        NStringEntity entity = new NStringEntity(para.toJSONString(), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic create mappings fail!");
        }
    }

    /**
     * add new fields to an existing type
     * @author xiezhenxiang 2019/9/12
     **/
    public void putMapping(String index, String type, String properties) {

        initClient();

        String endpoint ="/" + index + "/_mapping/" + type;
        NStringEntity entity = new NStringEntity(properties, ContentType.APPLICATION_JSON);
        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic put mapping fail!");
        }
    }

    public void reindex(String sourceIndex, String sourceType, String destIndex) {
        reindex( null, sourceIndex, sourceType, destIndex, null, 3000);
    }

    /**
     * move index's data to another
     * @author xiezhenxiang 2019/9/10
     **/
    public void reindex(String sourceHostUri, String sourceIndex, String sourceType, String destIndex, String query, Integer batchSize) {

        initClient();
        String endpoint ="/_reindex?slices=5";

        JSONObject para = new JSONObject();
        para.put("conflicts", "proceed");
        JSONObject source = new JSONObject();
        JSONObject dest = new JSONObject();

        if (StringUtils.isNotBlank(sourceHostUri)) {

            JSONObject remote = new JSONObject();
            remote.put("host", sourceHostUri);
            source.put("remote", remote);
        }

        source.put("index", sourceIndex);

        if (StringUtils.isNotBlank(sourceType)) {
            source.put("type", sourceType);
        }
        if (StringUtils.isNotBlank(query)) {
            source.put("query", JSONObject.parseObject(query));
        }
        if (batchSize != null) {
            source.put("size", batchSize);
        }
        para.put("source", source);

        dest.put("index", destIndex);
        dest.put("version_type", "internal");
        dest.put("routing", "=cat");
        para.put("dest", dest);
        setRefreshInterval(destIndex, "30s");
        NStringEntity entity = new NStringEntity(para.toJSONString(), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic reindex fail!");
        } finally {
            setRefreshInterval(sourceIndex, "1s");
        }
    }

    private void setRefreshInterval(String index, Object interval) {

        initClient();
        String endpoint ="/" + index + "/_settings";
        JSONObject para = new JSONObject();
        para.put("refresh_interval", interval);

        NStringEntity entity = new NStringEntity(para.toJSONString(), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic set refresh_interval fail!");
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
            exit("elastic " + action + " alias fail!");
        }
    }

    public void deleteById(String index, String type, String id) {

        initClient();
        String endpoint = "/" + index + "/" + type + "/" + id;

        try {
            client.performRequest("DELETE", endpoint, Collections.emptyMap());
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic delete doc fail!");
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
            exit("elastic delete_by_query fail!");
        }
    }

    /**
     * remove all data of index
     **/
    public void clearIndex(String index, String type) {

        String query = "{\"match_all\":{}}";
        deleteByQuery(index, type, query);
    }

    public void upsertById(String index, String type, String id, JSONObject doc) {

        initClient();
        String endpoint = "/" + index + "/" + type + "/" + id;

        NStringEntity entity = new NStringEntity(doc.toJSONString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic index doc fail!");
        }
    }

    public void updateById(String index, String type, String id, JSONObject doc) {

        initClient();
        String endpoint = "/" + index + "/" + type + "/" + id + "/_update";

        doc.remove("_id");
        JSONObject paraData = new JSONObject();
        paraData.put("doc", doc);

        NStringEntity entity = new NStringEntity(paraData.toJSONString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic update doc fail!");
        }
    }

    public JSONObject findById(String index, String type, String id) {

        initClient();
        JSONObject doc = null;
        String endpoint = "/" + index + "/" + type + "/" + id;

        try {
            Response response = client.performRequest("GET", endpoint);
            String str = EntityUtils.toString(response.getEntity(), "utf-8");
            JSONObject rs = JSONObject.parseObject(str);
            doc = rs.getJSONObject("_source");
            doc.put("_id", rs.getString("_id"));
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic index doc fail!");
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
            exit("elastic insert doc fail!");
        }
    }

    public void bulkInsert(String index, String type, Collection<JSONObject> ls) {

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
            exit("elastic bulk create fail!");
        }
    }

    /**
     * 根据id批量插入更新
     * @author xiezhenxiang 2019/9/9
     **/
    public void bulkUpsertById(String index, String type, Collection<JSONObject> ls) {

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
            exit("elastic bulk index fail!");
        }
    }

    /**
     * 根据id批量插入更新
     * @author xiezhenxiang 2019/9/9
     **/
    public void bulkDeleteById(String index, String type, Collection<JSONObject> ls) {

        initClient();
        if (ls.isEmpty()) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        ls.forEach(s -> {

            String id = s.getString("_id");
            if (id != null) {
                buffer.append("{\"delete\":{\"_index\":\"" + index + "\",\"_type\":\"" + type + "\",\"_id\":\"" + id + "\"}}\n");
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
            exit("elastic bulk delete fail!");
        }
    }

    public void updateByQuery (String index, String type, String queryStr, JSONObject doc) {

        if (doc.isEmpty()) {
            return;
        }

        initClient();
        doc.remove("_id");
        String endpoint = StringUtils.isBlank(type) ? "/" + index : "/" + index + "/" + type;
        endpoint += "/_update_by_query?conflicts=proceed";

        JSONObject paraData = new JSONObject();
        paraData.put("query", JSONObject.parseObject(queryStr));

        JSONObject script = new JSONObject();
        script.put("lang", "painless");
        script.put("params", doc);
        StringBuffer source = new StringBuffer();
        doc.keySet().forEach(s -> {
            source.append("ctx._source."+ s +"=params."+ s +";");
        });
        script.put("inline", source.toString());

        paraData.put("script", script);

        NStringEntity entity = new NStringEntity(paraData.toJSONString(), ContentType.APPLICATION_JSON);

        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic update_by_query fail!");
        }
    }


    /**
     * 深度检索，建议1w数据量以下使用
     */
    public String findByQuery(String index, String type, Integer pageNo, Integer pageSize, String query, String sort) {

        initClient();
        Objects.requireNonNull(pageNo, "pageNo is null!");
        Objects.requireNonNull(pageSize, "pageSize is null!");

        String rs = null;
        JSONObject paraData = new JSONObject();
        paraData.put("from", (pageNo - 1) * pageSize);
        paraData.put("size", pageSize);
        if (StringUtils.isNotBlank(query)) {
            paraData.put("query", JSONObject.parseObject(query));
        }
        if (StringUtils.isNoneBlank(sort)) {
            paraData.put("sort", JSONArray.parseObject(query));
        }

        String endpoint = StringUtils.isNoneBlank(type) ? "/" + index + "/" + type : "/" + index;
        endpoint += "/_search";

        try {
            NStringEntity entity = new NStringEntity(paraData.toJSONString(), ContentType.APPLICATION_JSON);
            Response response = client.performRequest("POST", endpoint, Collections.emptyMap(), entity, consumerFactory);
            rs = EntityUtils.toString(response.getEntity(), "utf-8");
            rs = JSONPath.read(rs, "hits.hits").toString();
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic search docs fail!");
        }

        return rs;
    }

    /**
     * 游标检索全量数据,配合循环使用，第一次传scrollId为null,第二次传返回的scrollId,直到数据读完位置（rs[1].length <= 2）
     */
    public String[] findByScroll(String index, String type, String query, String sort, Integer size, String scrollId) {

        initClient();
        Objects.requireNonNull(size, "size is null");

        String rs = "";
        JSONObject paraData = new JSONObject();
        String endpoint;
        if (scrollId == null) {

            if (StringUtils.isNotBlank(query)) {
                paraData.put("query", JSONObject.parseObject(query));
            }
            if (StringUtils.isNoneBlank(sort)) {
                paraData.put("sort", JSONArray.parseObject(query));
            }
            paraData.put("size", size);

            endpoint = StringUtils.isNoneBlank(type) ? "/" + index + "/" + type : "/" + index;
            endpoint += "/_search?scroll=5m";
        } else {

            endpoint = "/_search/scroll";
            paraData.put("scroll", "5m");
            paraData.put("scroll_id", scrollId);
        }

        try {
            NStringEntity entity = new NStringEntity(paraData.toJSONString(), ContentType.APPLICATION_JSON);
            Response response = client.performRequest("POST", endpoint, Collections.emptyMap(), entity, consumerFactory);
            rs = EntityUtils.toString(response.getEntity(), "utf-8");
            scrollId = JSONPath.read(rs, "_scroll_id").toString();
            rs = JSONPath.read(rs, "hits.hits").toString();

            if (rs.length() <= 2) {
                endpoint = "/_search/scroll/" + scrollId;
                client.performRequest("DELETE", endpoint);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            exit("elastic search by scroll fail!");
        }

        return new String[] {scrollId, rs};
    }



    public static void main(String[] args) throws Exception {

        EsRestUtil restClient = getInstance(new HttpHost("192.168.4.11", 9200));

        String[] rs = restClient.findByScroll("kg_gw_help_test22", "data", null, null, 10000, null);

        int i = 1;
        while (rs[1].length() > 2) {

            String str = rs[1];
            String scrollId = rs[0];
            System.out.println(i ++);
            rs = restClient.findByScroll("kg_gw_help_test22", "data", null, null, 5000, rs[0]);
        }

    }

    private EsRestUtil(HttpHost... hosts) {

        this.hosts = hosts;
        initClient();
    }

    private void initClient() {

        if (client == null) {
            synchronized (EsRestUtil.class){
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
                                request.setSocketTimeout(1000 * 60 * 2);
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

    private void exit(String msg) {

        throw ServiceException.newInstance(60000, msg);
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
                client = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
