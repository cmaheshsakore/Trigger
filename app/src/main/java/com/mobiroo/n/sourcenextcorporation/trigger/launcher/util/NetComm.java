package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by krohnjw on 6/6/2014.
 */
public class NetComm {

    public static class Response {

        protected int code;
        protected String body;
        protected String message;

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
            return new JSONObject(new JSONTokener(getBody()));
        }
    }

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    public static final String CONTENT_TYPE_JSON = "application/json";

    private static void logd(String message) {
        Logger.d("NetComm: " + message);
    }

    private static void loge(String message, Exception e) {
        Logger.e("NetComm: " + message, e);
    }


    public static Response getHttpsResponse(Context context, String uri, String method, String contentType, Object data) {
        return getHttpsResponse(context, uri, method, contentType, null, data);
    }

    public static Response getHttpsResponse(Context context, String uri, String method, String contentType, String keyToCompress, Object data) {
        URL url;
        HttpsURLConnection conn = null;
        String boundary = "====" + System.currentTimeMillis() + "====";

        try {
            url = new URL(uri);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(7000);

            if (keyToCompress != null) {
                contentType = "multipart/form-data; boundary=" + boundary;
            }

            if (contentType != null) {
                conn.setRequestProperty("Content-Type", contentType);
            }

            if (data != null) {
                conn.setDoInput(true);
                conn.setDoOutput(true);

                if (keyToCompress != null) {
                    addCompressedFile(conn, boundary, keyToCompress, data);
                } else {
                    OutputStream os = conn.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");

                    if (data instanceof List) {
                        writer.write(getQuery((List<NameValuePair>) data));
                    } else if (data instanceof JSONObject) {
                        writer.write(((JSONObject) data).toString());
                    } else {
                        writer.write(data.toString());
                    }
                    writer.flush();
                    writer.close();

                    os.close();
                }
            }

            logd(String.format("Server response for %s to %s: %s, %s ", method, uri, conn.getResponseCode(), conn.getResponseMessage()));

            BufferedReader responseReader = null;

            if (conn.getResponseCode() >= 400) {
                responseReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            } else {
                responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }

            StringBuilder b = new StringBuilder();

            if (responseReader != null) {
                String inputLine;
                while ((inputLine = responseReader.readLine()) != null) {
                    b.append(inputLine);
                }
                responseReader.close();
            }


            //logd("Returning body " + b.toString());

            return new Response(conn.getResponseCode(), conn.getResponseMessage(), b.toString());
        } catch (Exception e) {
            loge("Exception querying uri: " + e, e);
            e.printStackTrace();
            return new Response(-1, "-1", e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

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

    public static boolean isConnectedToNetwork(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            if (ni.getType() == ConnectivityManager.TYPE_WIFI)
                if (ni.isConnectedOrConnecting())
                    haveConnectedWifi = true;
            if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
                if (ni.isConnectedOrConnecting())
                    haveConnectedMobile = true;
        }

        return haveConnectedMobile || haveConnectedWifi;
    }

    private static final String TWO_HYPHENS = "--";
    private static final String LINE_FEED = "\r\n";
    private static void addCompressedFile(HttpsURLConnection conn, String boundary, String keyToCompress, Object data) throws IOException {
        if (! (data instanceof JSONObject)) {
            throw new IOException("NetComm: Can only compress JSONObject");
        }

        DataOutputStream os = new DataOutputStream(conn.getOutputStream());

        JSONObject d = (JSONObject) data;

        Iterator<String> keys = d.keys();
        while (keys.hasNext()) {
            String key = keys.next();

            if (key.equals(keyToCompress)) {
                String val;
                try {
                    val = d.get(key).toString();
                } catch (JSONException e) {
                    logd("Could not convert to json for: " + key);
                    continue;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
                gzipOutputStream.write(val.getBytes());
                gzipOutputStream.close();



                os.writeBytes(TWO_HYPHENS + boundary + LINE_FEED);
                os.writeBytes("Content-Disposition: form-data; name=\"" + key + "_compressed\"" + LINE_FEED);
                os.writeBytes("Content-Type: application/octet-stream" + LINE_FEED);
                os.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
                os.writeBytes(LINE_FEED);
                os.write(baos.toByteArray());
                os.writeBytes(LINE_FEED);

                baos.close();

                continue;
            }

            try {
                String val = d.get(key).toString();
                os.writeBytes(TWO_HYPHENS + boundary + LINE_FEED);
                os.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + LINE_FEED);
                os.writeBytes("Content-Type: text/plain" + LINE_FEED);
                os.writeBytes(LINE_FEED);
                os.writeBytes(val);
                os.writeBytes(LINE_FEED);
            } catch (JSONException e) {
                logd("Could not convert to json for: " + key);
            }

        }

        os.writeBytes(TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_FEED);
        os.flush();
        os.close();
    }
}
