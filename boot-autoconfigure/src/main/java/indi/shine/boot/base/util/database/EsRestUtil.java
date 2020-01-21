package indi.shine.boot.base.util.database;

import indi.shine.boot.base.exception.ServiceException;
import indi.shine.boot.base.util.JacksonUtil;
import indi.shine.boot.base.util.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

/**
 * Low Level RestClient
 * support es 5.x and 6.x
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

    public boolean exists(String index, String type) {

        initClient();
        String endpoint = "/" + index;
        if (StringUtils.isNotBlank(type)) {
            endpoint += "/_mapping/" + type;
        }
        boolean success = false;
        try {
            Response response = client.performRequest("HEAD", endpoint);
            success = response.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            log.error("elastic send head index fail!");
            throw new RuntimeException(e);
        }
        return success;
    }

    public void createIndex(String index, String mapping) {

        initClient();
        String endpoint ="/" + index;
        NStringEntity entity = new NStringEntity(mapping, ContentType.APPLICATION_JSON);
        try {   
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic create index fail!");
            throw new RuntimeException(e);
        }
    }

    public void deleteIndex(String index) {

        initClient();
        String endpoint ="/" + index;
        try {
            client.performRequest("DELETE", endpoint);
        } catch (IOException e) {
            log.error("elastic delete index fail!");
            throw new RuntimeException(e);
        }
    }

    /**
     * add a new type to an existing index
     * @author xiezhenxiang 2019/9/12
     **/
    public void createMappings(String index, String mappings) {

        initClient();
        String entityStr = "{\"mappings\":" + mappings + "}";
        String endpoint ="/" + index;
        NStringEntity entity = new NStringEntity(entityStr, ContentType.APPLICATION_JSON);
        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic create mappings fail!");
            throw new RuntimeException(e);
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
            log.error("elastic put mapping fail!");
            throw new RuntimeException(e);
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
        Map<String, Object> para = new HashMap<>();
        para.put("conflicts", "proceed");
        Map<String, Object> source = new HashMap<>();
        Map<String, Object> dest = new HashMap<>();

        if (StringUtils.isNotBlank(sourceHostUri)) {
            Map<String, Object> remote = new HashMap<>();
            remote.put("host", sourceHostUri);
            source.put("remote", remote);
        }
        source.put("index", sourceIndex);

        if (StringUtils.isNotBlank(sourceType)) {
            source.put("type", sourceType);
        }
        if (StringUtils.isNotBlank(query)) {
            source.put("query", JacksonUtil.parseObject(query, HashMap.class));
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
        NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(para), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic reindex fail!");
            throw new RuntimeException(e);
        } finally {
            setRefreshInterval(sourceIndex, "1s");
        }
    }

    private void setRefreshInterval(String index, String interval) {

        initClient();
        String endpoint ="/" + index + "/_settings";
        String entityStr = "{\"refresh_interval\":\""+ interval +"\"}";
        NStringEntity entity = new NStringEntity(entityStr, ContentType.APPLICATION_JSON);
        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic set refresh_interval fail!");
            throw new RuntimeException(e);
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
        List<Map> actions = new ArrayList<>();
        Arrays.stream(aliases).forEach(s -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("index", index);
            map.put("alias", s);
            HashMap<String, Object> actionMap =new HashMap<>();
            actionMap.put(action, map);
            actions.add(actionMap);
        });

        HashMap<String, Object> paraData = new HashMap<>();
        paraData.put("actions", actions);
        String endpoint ="/_aliases";
        NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(paraData), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic " + action + " alias fail!");
            throw new RuntimeException(e);
        }
    }

    public void deleteById(String index, String type, String id) {

        initClient();
        String endpoint = "/" + index + "/" + type + "/" + id;
        try {
            client.performRequest("DELETE", endpoint, Collections.emptyMap());
        } catch (IOException e) {
            log.error("elastic delete doc fail!");
            throw new RuntimeException(e);
        }
    }

    public void deleteByQuery(String index, String type, String queryStr) {

        initClient();
        String entityStr = "{\"query\":\""+ queryStr +"\"}";
        String endpoint = StringUtils.isBlank(type) ? "/" + index : "/" + index + "/" + type;
        endpoint += "/_delete_by_query?refresh&conflicts=proceed";
        NStringEntity entity = new NStringEntity(entityStr, ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic delete_by_query fail!");
            throw new RuntimeException(e);
        }
    }

    /**
     * remove all data of index
     **/
    public void clearIndex(String index, String type) {

        String query = "{\"match_all\":{}}";
        deleteByQuery(index, type, query);
    }

    public void upsertById(String index, String type, String id, Map<String, Object> doc) {

        initClient();
        String endpoint = "/" + index + "/" + type + "/" + id;
        NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(doc), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("PUT", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic index doc fail!");
            throw new RuntimeException(e);
        }
    }

    public void updateById(String index, String type, String id, Map<String, Object> doc) {

        initClient();
        String endpoint = "/" + index + "/" + type + "/" + id + "/_update";
        doc.remove("_id");
        Map<String, Object> paraData = new HashMap<>();
        paraData.put("doc", doc);
        NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(paraData), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic update doc fail!");
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findById(String index, String type, String id) {

        initClient();
        Map<String, Object> doc;
        String endpoint = "/" + index + "/" + type + "/" + id;
        try {
            Response response = client.performRequest("GET", endpoint);
            String str = EntityUtils.toString(response.getEntity(), "utf-8");
            Map<String, Object> rs = JacksonUtil.parseObject(str, Map.class, String.class, Object.class);
            doc = (Map<String, Object>) rs.get("_source");
            doc.put("_id", rs.get("_id"));
        } catch (IOException e) {
            log.error("elastic find doc fail!");
            throw new RuntimeException(e);
        }
        return doc;
    }

    public void insert(String index, String type, Map<String, Object> doc) {

        initClient();
        String endpoint = "/" + index + "/" + type;
        NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(doc), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic insert doc fail!");
            throw new RuntimeException(e);
        }
    }

    public void bulkInsert(String index, String type, Collection<Map<String, Object>> ls) {

        initClient();
        if (!ls.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Map<String, Object> obj : ls) {
                long id = Snowflake.nextId();
                builder.append("{\"create\":{\"_index\":\"").append(index).append("\",\"_type\":\"").append(type).append("\",\"_id\":\"").append(id).append("\" } }\n");
                builder.append(JacksonUtil.toJsonString(obj)).append("\n");
            }

            String endpoint = "/_bulk";
            NStringEntity entity = new NStringEntity(builder.toString(), ContentType.APPLICATION_JSON);
            try {
                client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
            } catch (IOException e) {
                log.error("elastic bulk insert fail!");
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 根据id批量插入更新
     * @author xiezhenxiang 2019/9/9
     **/
    public void bulkUpsertById(String index, String type, Collection<Map<String, Object>> ls) {

        initClient();
        if (!ls.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Map<String, Object> obj : ls) {
                String id = obj.get("_id").toString();
                if (id != null) {
                    builder.append("{\"index\":{\"_index\":\"").append(index).append("\",\"_type\":\"").append(type).append("\",\"_id\":\"").append(id).append("\"}}\n");
                    obj.remove("_id");
                    builder.append(JacksonUtil.toJsonString(obj)).append("\n");
                }
            }

            String endpoint = "/_bulk";
            NStringEntity entity = new NStringEntity(builder.toString(), ContentType.APPLICATION_JSON);
            try {
                client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
            } catch (IOException e) {
                log.error("elastic bulk index fail!");
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 根据id批量删除
     * @author xiezhenxiang 2019/9/9
     **/
    public void bulkDeleteById(String index, String type, Collection<String> ids) {

        initClient();
        if (ids.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        ids.forEach(s -> {
            builder.append("{\"delete\":{\"_index\":\"").append(index).append("\",\"_type\":\"").append(type).append("\",\"_id\":\"").append(s).append("\"}}\n");
        });

        String endpoint = "/_bulk";
        NStringEntity entity = new NStringEntity(builder.toString(), ContentType.APPLICATION_JSON);
        try {
            client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            log.error("elastic bulk delete fail!");
            throw new RuntimeException(e);
        }
    }

    public void updateByQuery (String index, String type, String queryStr, Map<String, Object> doc) {

        if (!doc.isEmpty()) {
            initClient();
            doc.remove("_id");
            String endpoint = StringUtils.isBlank(type) ? "/" + index : "/" + index + "/" + type;
            endpoint += "/_update_by_query?conflicts=proceed";

            Map<String, Object> paraData = new HashMap<>();
            paraData.put("query", JacksonUtil.parseObject(queryStr, Map.class));

            Map<String, Object> script = new HashMap<>();
            script.put("lang", "painless");
            script.put("params", doc);
            StringBuilder source = new StringBuilder();
            doc.keySet().forEach(s -> {
                source.append("ctx._source.").append(s).append("=params.").append(s).append(";");
            });
            script.put("inline", source.toString());
            paraData.put("script", script);

            NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(paraData), ContentType.APPLICATION_JSON);
            try {
                client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
            } catch (IOException e) {
                log.error("elastic update_by_query fail!");
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 深度检索，建议1w数据量以下使用
     */
    public List<Map<String, Object>> findByQuery(String index, String type,  String query, String sort, Integer pageNo, Integer pageSize) {

        initClient();
        Objects.requireNonNull(pageNo, "pageNo is null!");
        Objects.requireNonNull(pageSize, "pageSize is null!");

        Map<String, Object> paraData = new HashMap<>();
        paraData.put("from", (pageNo - 1) * pageSize);
        paraData.put("size", pageSize);
        if (StringUtils.isNotBlank(query)) {
            paraData.put("query", JacksonUtil.parseObject(query, Map.class));
        }
        if (StringUtils.isNoneBlank(sort)) {
            paraData.put("sort", JacksonUtil.parseObject(sort, Map.class));
        }

        String endpoint = StringUtils.isNoneBlank(type) ? "/" + index + "/" + type : "/" + index;
        endpoint += "/_search";
        List<Map<String, Object>> ls;
        try {
            NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(paraData), ContentType.APPLICATION_JSON);
            Response response = client.performRequest("POST", endpoint, Collections.emptyMap(), entity, consumerFactory);
            String rsp = EntityUtils.toString(response.getEntity(), "utf-8");
            ls = dataInRsp(rsp);
        } catch (IOException e) {
            log.error("elastic search docs fail!");
            throw new RuntimeException(e);
        }
        return ls;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> dataInRsp(String rsp) {

        List<Map<String, Object>> ls = new ArrayList<>();
        Map rMap = JacksonUtil.parseObject(rsp, Map.class);
        List<Map> hits = (List<Map>) ((Map) rMap.get("hits")).get("hits");
        for (Map hit : hits) {
            Map<String, Object> m = (Map<String, Object>) hit.get("_source");
            m.put("_id", hit.get("_id"));
            ls.add(m);
        }
        return ls;
    }

    public long count(String index, String type, String query) {

        String entityStr = StringUtils.isBlank(query) ? "{}" : "{\"query\":"+ query +"}";
        String endpoint = StringUtils.isNoneBlank(type) ? "/" + index + "/" + type : "/" + index;
        endpoint += "/_count";
        NStringEntity entity = new NStringEntity(entityStr, ContentType.APPLICATION_JSON);
        Response response = null;
        long count;
        try {
            response = client.performRequest("GET", endpoint, Collections.emptyMap(), entity, consumerFactory);
            String rsp = EntityUtils.toString(response.getEntity(), "utf-8");
            count = Long.parseLong(JacksonUtil.parseObject(rsp, Map.class).get("count").toString());
        } catch (IOException e) {
            log.error("elastic count docs fail!");
            throw new RuntimeException(e);
        }
        return count;
    }

    /**
     * 游标检索全量数据,配合循环使用，第一次传scrollId为null,第二次传返回的scrollId,直到数据读完位置（rs[1].length <= 2）
     */
    @SuppressWarnings("unchecked")
    public ScrollRsp findByScroll(String index, String type, String query, String sort, Integer size, String scrollId) {

        initClient();
        Objects.requireNonNull(size, "size is null");

        Map<String, Object> paraData = new HashMap<>();
        String endpoint;
        if (scrollId == null) {
            if (StringUtils.isNotBlank(query)) {
                paraData.put("query", JacksonUtil.parseObject(query, Map.class));
            }
            if (StringUtils.isNoneBlank(sort)) {
                paraData.put("sort", JacksonUtil.parseObject(sort, Map.class));
            }
            paraData.put("size", size);
            endpoint = StringUtils.isNoneBlank(type) ? "/" + index + "/" + type : "/" + index;
            endpoint += "/_search?scroll=5m";
        } else {
            endpoint = "/_search/scroll";
            paraData.put("scroll", "5m");
            paraData.put("scroll_id", scrollId);
        }

        ScrollRsp scrollRsp = new ScrollRsp();
        try {
            NStringEntity entity = new NStringEntity(JacksonUtil.toJsonString(paraData), ContentType.APPLICATION_JSON);
            Response response = client.performRequest("POST", endpoint, Collections.emptyMap(), entity, consumerFactory);
            String rsp = EntityUtils.toString(response.getEntity(), "utf-8");
            Map<String, Object> m = JacksonUtil.parseObject(rsp, Map.class);
            scrollId = m.get("_scroll_id").toString();
            List<Map<String, Object>> ls = dataInRsp(rsp);
            scrollRsp.setData(ls);
            scrollRsp.setScrollId(scrollId);
            scrollRsp.setHasNext(!ls.isEmpty());
            if (!scrollRsp.hasNext) {
                endpoint = "/_search/scroll/" + scrollId;
                client.performRequest("DELETE", endpoint);
            }
        } catch (IOException e) {
            log.error("elastic search by scroll fail!");
            throw new RuntimeException(e);
        }
        return scrollRsp;
    }

    public static class ScrollRsp {

        private String scrollId;
        private List<Map<String, Object>> data;
        private boolean hasNext;

        public String getScrollId() {
            return scrollId;
        }

        public void setScrollId(String scrollId) {
            this.scrollId = scrollId;
        }

        public List<Map<String, Object>> getData() {
            return data;
        }

        public void setData(List<Map<String, Object>> data) {
            this.data = data;
        }

        public boolean hasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }
    }

    public static void main(String[] args) {

        EsRestUtil restClient = getInstance(new HttpHost("192.168.4.11", 9200));

        ScrollRsp rs = restClient.findByScroll("kg_gw_help_test22", "data", null, null, 5000, null);
        int page = 1;
        while (rs.hasNext()) {
            List<Map<String, Object>> data = rs.getData();
            System.out.println(JacksonUtil.toJsonString(data));
            rs = restClient.findByScroll("kg_gw_help_test22", "data", null, null, 5000, rs.getScrollId());
            System.out.println("page: " + page++);
        }

        List<Map<String, Object>> byQuery = restClient.findByQuery("kg_gw_help_test22", "data", null, null, 1, 10);
        System.out.println(JacksonUtil.toJsonString(byQuery));
        System.out.println(restClient.count("kg_gw_help_test22", "data", null));
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

    private String hostStr() {

        StringBuilder builder = new StringBuilder();
        for (HttpHost host : hosts) {
            builder.append(host.toHostString()).append("_");
        }
        return builder.substring(0, builder.length() - 1);
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
