package indi.fly.boot.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HttpUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final String code = "utf-8";

    public HttpUtils() {
    }

    private static URLConnection openConnection(String url, String proxyIp, Integer proxyPort) throws IOException {
        URL realUrl = new URL(url);
        URLConnection conn;
        if(StringUtils.check(proxyIp) && proxyPort != null){
            InetSocketAddress proxyAddr = new InetSocketAddress(proxyIp, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
            conn = realUrl.openConnection(proxy);
        }else{
            conn = realUrl.openConnection();
        }
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.setConnectTimeout(10*1000);
        conn.setReadTimeout(15*1000);
        return conn;
    }

    public static String sendGet(String url) {
        return sendGet(url, null, null);
    }

    public static String sendGet(String url, String proxy_ip, Integer proxy_port) {
        url = getHttpUrl(url);
        String result = "";

        try {
            HttpURLConnection connection = (HttpURLConnection) openConnection(url, proxy_ip, proxy_port);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                result = inputStreamTOString(connection.getInputStream(), code);
            }else{
                logger.info("url: {}", url);
                logger.info("Http Get请求获取不到源码，响应码为：{}", responseCode);
            }
        } catch (Exception e) {
            logger.info("url: {}", url);
            logger.error("Http Get请求获取源码异常" + e);
        }
        return result;
    }

    public static String sendGet(String url, Map<String, String> getParam) {
        return sendGet(dealGetParam(url, getParam));
    }

    public static String sendPost(String url, Map<String, String> postParam) {
        return sendPost(url, parseParam(postParam));
    }

    public static String sendPost(String url, Map<String, String> getParam, Map<String, String> postParam) {
        return sendPost(dealGetParam(url, getParam), parseParam(postParam));
    }

    private static String sendPost(String url, String param) {
        return sendPost(url, param, null, null);
    }

    private static String sendPost(String url, String param, String proxy_ip, Integer proxy_port) {
        String result = "";
        try {
            HttpURLConnection conn = (HttpURLConnection) openConnection(url, proxy_ip, proxy_port);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                result = inputStreamTOString(conn.getInputStream(),code);
            }else {
                logger.info("url: {}", url);
                logger.info("Http Post请求获取不到源码，响应码为：{}", responseCode);
            }
        } catch (Exception e) {
            logger.info("url: {}", url);
            logger.error("Http Post请求获取源码异常", e);
        }
        return result;
    }


    private static String parseParam(Map<String, String> param) {
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

    private static String dealGetParam(String url, Map<String, String> getParam) {
        String f = "?";
        if (url.contains("?")) {
            f = "&";
        }

        url = url + f + parseParam(getParam);
        return url;
    }

    private static String getHttpUrl(String str) {
        try {
            str = URLEncoder.encode(str, code).replace("%3A", ":")
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
