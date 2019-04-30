package indi.fly.boot.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HttpUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final String ENCODE = "utf-8";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf-8";
    public HttpUtils() {
    }

    private static HttpURLConnection openConnection(String url, String proxyHost) throws IOException {
        URL realUrl = new URL(url);
        URLConnection conn;
        if(StringUtils.check(proxyHost)){
            String proxyIp = proxyHost.substring(0, proxyHost.lastIndexOf(":"));
            int proxyPort = Integer.valueOf(proxyHost.substring(proxyHost.lastIndexOf(":") + 1));
            InetSocketAddress proxyAddr = new InetSocketAddress(proxyIp, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
            conn = realUrl.openConnection(proxy);
        }else{
            conn = realUrl.openConnection();
        }
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.setConnectTimeout(8 * 1000);
        conn.setReadTimeout(10 * 1000);
        conn.setDoInput(true);
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        // 设置自动执行重定向
        httpConn.setInstanceFollowRedirects(true);
        return httpConn;
    }


    private static String sendGet(String url, Map<String, String> head, String proxyHost) {
        url = getHttpUrl(url);
        String result = "";
        try {
            HttpURLConnection connection = openConnection(url, proxyHost);
            if (head != null) {
                for (Map.Entry<String, String> entry : head.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                result = inputStreamTOString(connection.getInputStream(), ENCODE);
            }else{
                logger.info("url: {}", url);
                logger.info("Http Get请求无结果，响应码为：{}", responseCode);
            }
        } catch (Exception e) {
            logger.info("url: {}", url);
            logger.error("Http Get请求异常 " + e);
        }
        return result;
    }

    public static String sendGet(String url) {
        return sendGet(url,null,null);
    }

    public static String sendGet(String url, String proxyHost) {
        return sendGet(url,null, proxyHost);
    }

    private static String sendPost(String url, Map<String, String> head, Map<String, Object> formPara, String jsonPara) {
        String result = "";
        try {
            HttpURLConnection conn = openConnection(url, null);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            if (head != null) {
                for (Map.Entry<String, String> entry : head.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setRequestProperty("Content-Type", FORM_CONTENT_TYPE);
            if (formPara != null) {
                String httpEntity = parseParam(formPara);
                OutputStream out = conn.getOutputStream();
                out.write(httpEntity.getBytes());
                out.flush();
                out.close();
            } else if (jsonPara != null) {
                conn.setRequestProperty("Content-Type", JSON_CONTENT_TYPE);
                OutputStream out = conn.getOutputStream();
                out.write(jsonPara.getBytes());
                out.flush();
                out.close();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                result = inputStreamTOString(conn.getInputStream(), ENCODE);
            }else {
                logger.info("url: {}", url);
                logger.info("Http Post请求获取不到源码，响应码为：{}", responseCode);
            }
        } catch (Exception e) {
            logger.info("url: {}", url);
            logger.error("Http Post请求获取源码异常 ", e);
        }
        return result;
    }

    public static String sendPost(String url, Map<String, String> head, Map<String, Object> formPara) {
        return sendPost(url, head, formPara, null);
    }

    public static String sendPost(String url, Map<String, String> head, String jsonPara) {
        return sendPost(url, head, null, jsonPara);
    }



    private static String parseParam(Map<String, Object> param) {
        List<String> list = new ArrayList<>();
        param.forEach((k, v) -> {
            list.add(k + "=" + v);
        });
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); ++i) {
            if (i > 0) {
                sb.append("&");
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private static String getHttpUrl(String str) {
        try {
            str = URLEncoder.encode(str, ENCODE).replace("%3A", ":")
                    .replaceAll("%2F", "/")
                    .replaceAll("%3F", "?")
                    .replaceAll("%3D", "=");
        }catch (Exception e){
            e.printStackTrace();
        }
        return str;
    }

    private static String inputStreamTOString(InputStream in,String encoding) throws Exception{

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[4096 * 4];
        int count = -1;
        while((count = in.read(data,0,4096 * 4)) != -1)
            outStream.write(data, 0, count);
        return new String(outStream.toByteArray(), encoding);
    }
}
