package com.boot.base.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class EsRestClient {
	
	private static final String PUT = "PUT";
	private static final String POST = "POST";
	private static final String GET = "GET";
	private static final String HEAD = "HEAD";
	private static final String DELETE = "DELETE";
    private static final String IP = "212.64.106.43";
    private static final Integer PORT = 9200;
    private static RestClient restClient;

    static {
        restClient = RestClient.builder(new HttpHost(IP, PORT))
                .setMaxRetryTimeoutMillis(60000)
                .build();
    }

	public static RestClient getClient(){
		return restClient;
	}

	public static String sendPost(String index,String type,String query){
		RestClient restClient = getClient();
		String  rs = null;
		try {
			HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
			String endpoint = "/"+index+"/"+type+"/_search";
			if(StringUtils.isBlank(type)){
				endpoint = "/"+index+"/_search";
			}
			Response response = restClient.performRequest(POST, endpoint, Collections.singletonMap("pretty", "true"),entity);
			rs = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			//close(restClient);
		}
		return rs;
	}
	
	public static String sendPost(List<String> indexs,List<String> types,String query){
		RestClient restClient = getClient();
		String  rs = null;
		try {
			String index = StringUtils.join(indexs, ",");
			String type = "/";
			if(Objects.nonNull(types) && !types.isEmpty()){
				type += StringUtils.join(types, ",")+"/";
			}
			HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
			String endpoint = "/"+index+type+"_search";
			Response response = restClient.performRequest(POST, endpoint, Collections.singletonMap("pretty", "true"),entity);
			rs = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			//close(restClient);
		}
		return rs;
	}
	
	public static String sendPut(String index,String type,String id,String data){
		RestClient restClient = getClient();
		String  rs = null;
		try {
			HttpEntity entity = new NStringEntity(data, ContentType.APPLICATION_JSON);
			String requestType = POST;
			String endpoint = "/"+index+"/"+type;
			if(StringUtils.isNoneBlank(id)){
				requestType = PUT;
				endpoint = "/"+index+"/"+type+"/"+id;
			}
			Response response = restClient.performRequest(requestType, endpoint, Collections.singletonMap("pretty", "true"),entity);
			rs = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			//close(restClient);
		}
		return rs;
	}
	
	public static String sendDelete(String index,String type){
		return sendDelete(index,type,null);
	}
	
	public static String sendDelete(String index,String type,String id){
		RestClient restClient = getClient();
		String rs = null;
		try {
			String endpoint = "/"+index+"/"+type+"/"+id;
			if(StringUtils.isBlank(id)){
				endpoint = "/"+index+"/"+type;
			}else if(StringUtils.isBlank(type)){
				endpoint = "/"+index;
			}
			Response response = restClient.performRequest(DELETE, endpoint);
			rs = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			//close(restClient);
		}
		return rs;
	}
	
	public static boolean sendHead(String index,String type){
		RestClient restClient = getClient();
		int code = 200;
		try {
//			String endpoint = "/"+index+"/"+type;//1.7
			String endpoint = "/"+index+"/_mapping/"+type;//5.x
			if(StringUtils.isBlank(type)){
				endpoint = "/"+index;
			}
			Response response = restClient.performRequest(HEAD, endpoint);//200存在，404不存在
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			//close(restClient);
		}
		return code == 200?true:false;
	}
	
	public static void close(RestClient restClient){
		if(Objects.nonNull(restClient)){
			try {
				restClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
