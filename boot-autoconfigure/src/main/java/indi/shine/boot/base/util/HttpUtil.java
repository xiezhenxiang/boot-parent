package indi.shine.boot.base.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http请求工具
 * @author xiezhenxiang 2019/4/12
 **/
public final class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final String ENCODE = "utf-8";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String BOUNDARY = "----webkitformboundarykpioiok7ub8qe2ax";
    private final static String BOUNDARY_PREFIX = "--";
    private static final String FILE_CONTENT_TYPE = "multipart/form-data; boundary=" + BOUNDARY;
    private static final String JSON_CONTENT_TYPE = "application/json;charset=utf-8";
    private static int readTimeout = 30 * 1000;
    private static String proxyHost = null;

    private static HttpURLConnection openConnection(String url, Map<String, String> head) throws IOException {
        URL realUrl = new URL(url);
        URLConnection conn;
        if(StringUtils.isNotBlank(proxyHost)){
            String proxyIp = proxyHost.substring(0, proxyHost.lastIndexOf(":"));
            int proxyPort = Integer.parseInt(proxyHost.substring(proxyHost.lastIndexOf(":") + 1));
            InetSocketAddress proxyAddr = new InetSocketAddress(proxyIp, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
            conn = realUrl.openConnection(proxy);
        }else{
            conn = realUrl.openConnection();
        }
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.setRequestProperty("Charset", ENCODE);
        conn.setConnectTimeout(5 * 1000);
        conn.setReadTimeout(readTimeout);
        conn.setDoInput(true);
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        // 设置自动执行重定向
        httpConn.setInstanceFollowRedirects(true);
        if (head != null) {
            for (Map.Entry<String, String> entry : head.entrySet()) {
                httpConn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return httpConn;
    }

    public static String sendGet(String url, Map<String, String> head) {
        url = getHttpUrl(url);
        String result = "";
        try {
            HttpURLConnection connection = openConnection(url, head);
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            result = getResult(connection);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("url: {}", url);
            logger.error("Http Get请求异常 " + e);
        }
        return result;
    }

    public static String sendGet(String url) {
        return sendGet(url, null);
    }


    private static String sendPost(String url, Map<String, String> head, Map<String, Object> formPara, String jsonPara, String method) {
        String result = "";
        try {
            HttpURLConnection conn = openConnection(url, head);
            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();;
            if (formPara != null) {
                if (!head.containsKey("Content-Type")) {
                    conn.setRequestProperty("Content-Type", FORM_CONTENT_TYPE);
                }
                String httpEntity = parseParam(formPara);
                out.write(httpEntity.getBytes());
            } else if (jsonPara != null) {
                if (!head.containsKey("Content-Type")) {
                    conn.setRequestProperty("Content-Type", JSON_CONTENT_TYPE);
                }
                out.write(jsonPara.getBytes());
            }
            out.flush();
            out.close();
            result = getResult(conn);
        } catch (Exception e) {
            logger.info("url: {}", url);
            logger.error("Http {}请求获取源码异常 ", method, e);
        }
        return result;
    }

    private static String getResult(HttpURLConnection conn) throws Exception {
        String result = "";
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpStatus.SC_OK) {
            result = inputStreamToString(conn.getInputStream());
        } else{
            logger.info("url: {}", conn.getURL().toString());
            logger.warn("Http请求获取不到源码，响应码为：{}", responseCode);
        }
        return result;
    }

    public static String sendFile(String url, Map<String, String> head, Map<String, Object> formPara, Map<String, File> filePara) {
        String result = "";
        String method = "POST";
        try {
            HttpURLConnection conn = openConnection(url, head);
            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", FILE_CONTENT_TYPE);
            conn.connect();

            OutputStream out = new DataOutputStream(conn.getOutputStream());;
            StringBuilder formData = new StringBuilder();
            if (formPara != null && !formPara.isEmpty()) {
                for (Map.Entry<String, Object> entry : formPara.entrySet()) {
                    formData.append(BOUNDARY_PREFIX).append(BOUNDARY).append(System.lineSeparator())
                            .append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey()).append("\"").append(System.lineSeparator())
                            .append("Content-Type: text/plain; charset=utf-8").append(System.lineSeparator())
                            .append("Content-Transfer-Encoding: 8bit")
                            .append(System.lineSeparator()).append(System.lineSeparator())
                            .append(entry.getValue())
                            .append(System.lineSeparator());
                }

            }
            if (filePara != null && !filePara.isEmpty()) {
                for (Map.Entry<String, File> entry : filePara.entrySet()) {
                    formData.append(BOUNDARY_PREFIX).append(BOUNDARY).append(System.lineSeparator());
                    formData.append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey()).append("\"; filename=\"")
                            .append(entry.getValue().getName()).append("\"")
                            .append(System.lineSeparator())
                            .append("Content-Type:").append(getContentType(entry.getValue()))
                            .append(System.lineSeparator())
                            .append("Content-Transfer-Encoding: 8bit")
                            .append(System.lineSeparator())
                            .append(System.lineSeparator());

                    out.write(formData.toString().getBytes());
                    InputStream in = new FileInputStream(entry.getValue());
                    byte[] buffer = new byte[1024 * 1024];
                    int length = 0;
                    while ((length = in.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }
                    out.write(System.lineSeparator().getBytes());
                }
            }

            String endLine = BOUNDARY_PREFIX + BOUNDARY + BOUNDARY_PREFIX;
            out.write(endLine.getBytes());
            out.flush();
            out.close();
            result = getResult(conn);
        } catch (Exception e) {
            logger.info("url: {}", url);
            logger.error("Http {}请求获取源码异常 ", method, e);
        }
        return result;
    }

    private static String getContentType(File file)  {
        Path path = Paths.get(file.getAbsolutePath());
        String contentType = null;
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            logger.error("Read File ContentType Error");
        }
        // 若失败则调用另一个方法进行判断
        if (contentType == null) {
            contentType = new MimetypesFileTypeMap().getContentType(file);
        }
        return contentType;
    }

    public static String sendPut(String url, Map<String, String> head, String jsonPara) {
        return sendPost(url, head, null, jsonPara, "PUT");
    }
    public static String sendPost(String url, Map<String, String> head, Map<String, Object> formPara) {
        return sendPost(url, head, formPara, null, "POST");
    }

    public static String sendPost(String url, Map<String, String> head, String jsonPara) {
        return sendPost(url, head, null, jsonPara, "POST");
    }

    public static InputStream download(String url) {
        url = getHttpUrl(url);
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = openConnection(url, null);
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpStatus.SC_OK) {
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

        if (StrUtil.isChinese(str)) {
            try {
                str = URLEncoder.encode(str, ENCODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str.replaceAll("%3A", ":")
                  .replaceAll("%2F", "/")
                  .replaceAll("%3F", "?")
                  .replaceAll("%3D", "=")
                  .replaceAll("%26", "&")
                  .replaceAll(" ", "%20");
    }

    private static String inputStreamToString(InputStream in) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int len = 4096 * 4;
        byte[] data = new byte[len];
        int count;
        while ((count = in.read(data,0,len)) != -1) {
            outStream.write(data, 0, count);
        }
        return outStream.toString(ENCODE);
    }

    public static void setReadTimeout(int timeout) {
        readTimeout = timeout;
    }

    public static void setProxyHost(String httpProxyHost) {
        proxyHost = httpProxyHost;
    }
}
