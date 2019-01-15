package com.boot.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpUtils {
    public HttpUtils() {
    }

    private static URLConnection openConnection(String url) throws IOException {
        URL realUrl = new URL(url);
        URLConnection conn = realUrl.openConnection();
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        return conn;
    }

    private static String parseParam(Map<String, String> param) {
        List<String> list = new ArrayList();
        param.forEach((k, v) -> {
            list.add(k + "=" + v);
        });
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < list.size(); ++i) {
            if (i > 0) {
                sb.append("&");
            }

            sb.append((String)list.get(i));
        }

        return sb.toString();
    }

    private static String dealGetParam(String url, Map<String, String> getParam) {
        String f = "?";
        if (url.indexOf("?") != -1) {
            f = "&";
        }

        url = url + f + parseParam(getParam);
        return url;
    }

    public static String sendGet(String url) {
        return sendGet(url, (Map)(new HashMap()));
    }

    public static String sendGet(String url, Map<String, String> getParam) {
        return sendGet(dealGetParam(url, getParam), parseParam(getParam));
    }

    public static String sendPost(String url, Map<String, String> postParam) {
        return sendPost(url, parseParam(postParam));
    }

    public static String sendPost(String url, Map<String, String> getParam, Map<String, String> postParam) {
        return sendPost(dealGetParam(url, getParam), parseParam(postParam));
    }

    private static String sendGet(String url, String param) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;

        try {
            URLConnection connection = openConnection(url);
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception var14) {
            ;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception var13) {
                ;
            }

        }

        return result.toString();
    }

    private static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();

        try {
            URLConnection conn = openConnection(url);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception var15) {
            ;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

                if (in != null) {
                    in.close();
                }
            } catch (IOException var14) {
                ;
            }

        }

        return result.toString();
    }
}
