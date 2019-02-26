package indi.fly.boot.base.jersey;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class JerseyHttp {
    private JerseyClient client;
    private JerseyClientProperties clientProperties;

    public JerseyHttp() {
        this(new JerseyClientProperties());
    }

    public JerseyHttp(JerseyClientProperties clientProperties) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.property("jersey.config.client.connectTimeout", clientProperties.getConnectTimeout()).property("jersey.config.client.readTimeout", clientProperties.getReadTimeout()).register(JacksonJsonProvider.class).register(MultiPartFeature.class);
        this.client = JerseyClientBuilder.createClient(clientConfig);
        this.clientProperties = clientProperties;
    }

    private MultivaluedMap<String, Object> getDefaultRequestHeader() {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap();
        headers.add("Accept", this.clientProperties.getAcceptContentType());
        return headers;
    }

    public MultivaluedMap<String, Object> getMultiMap() {
        MultivaluedMap<String, Object> params = new MultivaluedHashMap();
        return params;
    }

    public String sendGet(String url, MultivaluedMap<String, Object> query) {
        return this.sendGet(url, this.getDefaultRequestHeader(), query);
    }

    public String sendGet(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query) {
        return (String)this.sendGet(url, headers, query, String.class);
    }

    public <T> T sendGet(String url, MultivaluedMap<String, Object> query, Class<T> cls) {
        return this.sendGet(url, this.getDefaultRequestHeader(), query, cls);
    }

    public <T> T sendGet(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, Class<T> cls) {
        WebTarget webTarget = this.parseQueryParams(url, query);
        return webTarget.request().headers(headers).get(cls);
    }

    public <T> T sendGet(String url, MultivaluedMap<String, Object> query, GenericType<T> hsp) {
        return this.sendGet(url, this.getDefaultRequestHeader(), query, hsp);
    }

    public <T> T sendGet(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, GenericType<T> hsp) {
        WebTarget webTarget = this.parseQueryParams(url, query);
        return webTarget.request().headers(headers).get(hsp);
    }

    public String sendPost(String url, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post) {
        return this.sendPost(url, this.getDefaultRequestHeader(), query, post);
    }

    public String sendPost(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post) {
        return (String)this.sendPost(url, headers, query, post, String.class);
    }

    public <T> T sendPost(String url, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post, Class<T> cls) {
        return this.sendPost(url, this.getDefaultRequestHeader(), query, post, cls);
    }

    public <T> T sendPost(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post, Class<T> cls) {
        WebTarget webTarget = this.parseQueryParams(url, query);
        return webTarget.request().headers(headers).post(Entity.entity(this.parsePostParams(post), this.clientProperties.getRequestContentEncode()), cls);
    }

    public <T> T sendPost(String url, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post, GenericType<T> hsp) {
        return this.sendPost(url, this.getDefaultRequestHeader(), query, post, hsp);
    }

    public <T> T sendPost(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post, GenericType<T> hsp) {
        WebTarget webTarget = this.parseQueryParams(url, query);
        return webTarget.request().headers(headers).post(Entity.entity(this.parsePostParams(post), this.clientProperties.getRequestContentEncode()), hsp);
    }

    public String sendTextPost(String url, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post) {
        return this.sendTextPost(url, this.getDefaultRequestHeader(), query, post);
    }

    public String sendTextPost(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post) {
        return (String)this.sendHttp(url, headers, query, post, MediaType.TEXT_PLAIN_TYPE, String.class);
    }

    public String sendJsonPost(String url, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post) {
        return this.sendJsonPost(url, this.getDefaultRequestHeader(), query, post);
    }

    public String sendJsonPost(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post) {
        return (String)this.sendHttp(url, headers, query, post, MediaType.APPLICATION_JSON_TYPE, String.class);
    }

    public String sendUpload(String url, MultivaluedMap<String, Object> query, FormDataMultiPart multipart) {
        return this.sendUpload(url, this.getDefaultRequestHeader(), query, multipart);
    }

    public String sendUpload(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, FormDataMultiPart multipart) {
        WebTarget webTarget = this.parseQueryParams(url, query);
        return (String)webTarget.request().headers(headers).post(Entity.entity(multipart, multipart.getMediaType()), String.class);
    }

    private <T> T sendHttp(String url, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> query, MultivaluedMap<String, Object> post, MediaType mediaType, Class<T> cls) {
        WebTarget webTarget = this.parseQueryParams(url, query);
        return webTarget.request().headers(headers).post(Entity.entity(this.parsePostParams(post), mediaType), cls);
    }

    private WebTarget parseQueryParams(String url, MultivaluedMap<String, Object> query) {
        WebTarget webTarget = this.client.target(url);
        Entry item;
        if (query != null && query.size() > 0) {
            for(Iterator var4 = query.entrySet().iterator(); var4.hasNext(); webTarget = ((WebTarget)webTarget).queryParam((String)item.getKey(), new Object[]{((List)item.getValue()).size() > 0 ? ((List)item.getValue()).get(0) : null})) {
                item = (Entry)var4.next();
            }
        }

        return (WebTarget)webTarget;
    }

    private MultivaluedMap<String, String> parsePostParams(MultivaluedMap<String, Object> post) {
        MultivaluedMap<String, String> p = new MultivaluedHashMap();
        if (post != null && post.size() > 0) {
            Iterator var3 = post.entrySet().iterator();

            while(var3.hasNext()) {
                Entry<String, List<Object>> item = (Entry)var3.next();
                p.add(item.getKey(), ((List)item.getValue()).size() > 0 ? ((List)item.getValue()).get(0).toString() : null);
            }
        }

        return p;
    }
}
