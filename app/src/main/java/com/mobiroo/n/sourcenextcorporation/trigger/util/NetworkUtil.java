package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.util.Base64;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by krohnjw on 4/24/2014.
 */
public class NetworkUtil {

    public static class Response {

        protected int code;
        protected String body;
        protected String message;

        public Response() {
            this.code = -1;
            this.body = "";
            this.message = "";
        }

        public Response(int code, String message, String body) {
            this.code = code;
            this.message = message;
            this.body = body;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String getBody() {
            return body;
        }

        public JSONObject getJson() throws JSONException {
            return new JSONObject(new JSONTokener(new String(getBody())));
        }

        public JSONObject getJsonFromBase64() throws JSONException {
            return new JSONObject(new JSONTokener(new String(Base64.decode(getBody(), Base64.DEFAULT))));
        }
    }

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private static void logd(String message) {
        Logger.d("NET-UTIL: " + message);
    }

    private static void loge(String message, Exception e) {
        Logger.e("NET-UTIL: " + message, e);
    }

    public static Response getHttpResponse(String uri, String method) {
        return getHttpResponse(uri, method, null);
    }

    public static Response getHttpResponse(String uri, String method, List<NameValuePair> pairs) {
        if (uri.startsWith("https://")) {
            return getHttpsResponse(uri, method, pairs);
        }

        Response response = new Response(-1, "", "");

        URL url;
        HttpURLConnection conn = null;
        OutputStreamWriter writer = null;
        try {
            logd("Querying " + uri + " as " + method);
            url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(7000);


            if (pairs != null) {
                logd("Writing data to stream");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                writer.write(getQuery(pairs));
                writer.flush();
                writer.close();
                conn.connect();
            }

            logd(String.format("Server response %s, %s ", conn.getResponseCode(), conn.getResponseMessage()));

            BufferedReader in = null;

            if (conn.getResponseCode() >= 400) {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }

            StringBuilder b = new StringBuilder();
            if (in != null) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    b.append(inputLine);
                }
            }
            if (pairs != null) {
                writer.close();
            }
            logd("Returning body " + b.toString());

            return new Response(conn.getResponseCode(), conn.getResponseMessage(), b.toString());
        } catch (Exception e) {
            loge("Exception querying uri: " + e, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return response;
    }

    public static Response getHttpsResponse(String uri, String method) {
        return getHttpsResponse(uri, method, null);
    }

    public static Response getHttpsResponse(String uri, String method, List<NameValuePair> pairs) {
        if (uri.startsWith("http://")) {
            return getHttpResponse(uri, method, pairs);
        }

        Response response = new Response(-1, "", "");

        URL url;
        HttpsURLConnection conn = null;
        OutputStream os = null;
        OutputStreamWriter writer = null;
        try {
            logd("Querying " + uri + " as " + method);

            url = new URL(uri);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(7000);
            if (pairs != null) {
                logd("Writing data to stream");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                os = conn.getOutputStream();
                writer = new OutputStreamWriter(os, "UTF-8");
                writer.write(getQuery(pairs));
                writer.flush();
                writer.close();
                conn.connect();


            }

            logd(String.format("Server response %s, %s ", conn.getResponseCode(), conn.getResponseMessage()));

            BufferedReader in = null;

            if (conn.getResponseCode() >= 400) {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }

            StringBuilder b = new StringBuilder();

            if (in != null) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    b.append(inputLine);
                }

                if (pairs != null) {
                    writer.close();
                }

                logd("Returning body " + b.toString());
            }
            return new Response(conn.getResponseCode(), conn.getResponseMessage(), b.toString());
        } catch (Exception e) {
            loge("Exception querying uri: " + e, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return response;
    }

    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
