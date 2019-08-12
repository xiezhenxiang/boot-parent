package indi.shine.boot.base.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http请求工具
 * @author xiezhenxiang 2019/4/12
 **/
public final class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final String ENCODE = "utf-8";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf-8";
    public HttpUtil() {
    }

    private static HttpURLConnection openConnection(String url, String proxyHost) throws IOException {

        URL realUrl = new URL(url);
        URLConnection conn;
        if(StringUtil.verify(proxyHost)){
            String proxyIp = proxyHost.substring(0, proxyHost.lastIndexOf(":"));
            int proxyPort = Integer.valueOf(proxyHost.substring(proxyHost.lastIndexOf(":") + 1));
            InetSocketAddress proxyAddr = new InetSocketAddress(proxyIp, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
            conn = realUrl.openConnection(proxy);
        }else{
            conn = realUrl.openConnection();
        }
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.setConnectTimeout(8 * 1000);
        conn.setReadTimeout(15 * 1000);
        conn.setDoInput(true);
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        // 设置自动执行重定向
        httpConn.setInstanceFollowRedirects(true);
        return httpConn;
    }


    public static String sendGet(String url, Map<String, String> head, String proxyHost) {

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
                result = inputStreamTOString(connection.getInputStream());
            } else if (responseCode == 301 || responseCode == 302) {
                url = connection.getHeaderField("Location");
                return sendGet(url, head, proxyHost);
            } else{
                logger.info("url: {}", url);
                logger.info("Http Get请求无结果，响应码为：{}", responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("url: {}", url);
            logger.error("Http Get请求异常 " + e);
        }
        return result;
    }

    public static String sendGet(String url, Map<String, String> head) {

        return sendGet(url, head, null);
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
                result = inputStreamTOString(conn.getInputStream());
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

    public static InputStream download(String url, String proxyHost) {

        url = getHttpUrl(url);
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = openConnection(url, proxyHost);
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                inputStream = connection.getInputStream();
            }else{
                logger.info("url: {}", url);
                logger.info("Http Get请求无结果，响应码为：{}", responseCode);
            }
        } catch (Exception e) {
            logger.info("url: {}", url);
            logger.error("Http Get请求异常 " + e);
        }
        return inputStream;
    }


    public static InputStream download(String url) {
        return download(url, null);
    }

    /**
     * 下载网页到本地，包括网页中的静态资源
     * @author xiezhenxiang 2019/8/6
     **/
    public static String  downloadFullHtml(String url, String fileDir, String proxyHost) {

        fileDir = fileDir.replaceAll("\\\\", "/");
        fileDir = fileDir.endsWith("/") ? fileDir : fileDir + "/";
        String staticDir = fileDir + "static/";
        File dir = new File(staticDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String html = sendGet(url, proxyHost);
        // 去除URL转义
        html = html.replaceAll("\\\\/", "/");
        // js <script src // css href /img src
        String regex = "(?<=\"|')(http)[\\S]*?\\.(css|js|jpg|png|bmp|jpeg|png|gif|CSS|JS|JPG|PNG|BMP|JPEG|PNG|GIF)(?=\"|')";
        List<String>ls = RegexUtil.subRegex(html, regex);

        try {

            for (String src : ls) {

                InputStream in = download(src);
                if (in == null) {
                    continue;
                }
                String fileName = src.substring(src.lastIndexOf("/") + 1);
                FileUtils.copyInputStreamToFile(in, new File(staticDir + fileName));
                html = html.replaceAll(src, "static/" + fileName);
            }
            OutputStream sourceFile = new FileOutputStream(new File(fileDir + "page.html"));
            byte[] b = html.getBytes(ENCODE);
            sourceFile.write(b);
            sourceFile.flush();
            sourceFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return html;
    }

    public static String downloadFullHtml(String url, String fileDir) {
        return downloadFullHtml(url, fileDir, null);
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

        if (StringUtil.isChinese(str)) {
            try {
                str = URLEncoder.encode(str, ENCODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        str = str.replace("%3A", ":")
                .replaceAll("%2F", "/")
                .replaceAll("%3F", "?")
                .replaceAll("%3D", "=");
        return str;
    }

    private static String inputStreamTOString(InputStream in) throws Exception{

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[4096 * 4];
        int count;

        while ((count = in.read(data,0,4096 * 4)) != -1) {
            outStream.write(data, 0, count);
        }
        return new String(outStream.toByteArray(), ENCODE);
    }

}
