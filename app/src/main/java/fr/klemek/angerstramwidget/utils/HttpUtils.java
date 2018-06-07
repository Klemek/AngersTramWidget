package fr.klemek.angerstramwidget.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing utils for http requests
 *
 * @author Clement Gouin
 */
public final class HttpUtils {

    private static final String ENCODING = "UTF-8";

    private static final List<String> SUPPORTED_METHODS = new ArrayList<>(Arrays.asList("GET","PUT","POST","DELETE"));

    private HttpUtils() {
    }

    /**
     * Execute an http/https request
     *
     * @param sMethod
     *            the http method
     * @param sUrl
     *            the url to reach
     * @return the results of the request
     */
    public static HttpResult executeRequest(String sMethod, String sUrl) {
        return executeRequest(sMethod, sUrl, null, null, null);
    }

    /**
     * Execute an http/https request
     *
     * @param sMethod
     *            the http method
     * @param sUrl
     *            the url to reach
     * @param params
     *            the url parameters (or null if not needed)
     * @return the results of the request
     */
    public static HttpResult executeRequest(String sMethod, String sUrl, Map<String, String[]> params) {
        return executeRequest(sMethod, sUrl, params, null, null);
    }

    /**
     * Execute an http/https request
     *
     * @param sMethod
     *            the http method
     * @param sUrl
     *            the url to reach
     * @param data
     *            the json data of the request
     * @return the results of the request
     */
    public static HttpResult executeRequest(String sMethod, String sUrl, JSONObject data) {
        return executeRequest(sMethod, sUrl, null, null, data);
    }

    /**
     * Execute an http/https request
     *
     * @param sMethod
     *            the http method
     * @param sUrl
     *            the url to reach
     * @param params
     *            the url parameters (or null if not needed)
     * @param headers
     *            additional headers for the request (or null if not needed)
     * @return the results of the request
     */
    public static HttpResult executeRequest(String sMethod, String sUrl, Map<String, String[]> params,
                                            Map<String, String> headers) {
        return executeRequest(sMethod, sUrl, params, headers, null);
    }

    /**
     * Execute an http/https request
     *
     * @param sMethod
     *            the http method
     * @param sUrl
     *            the url to reach
     * @param params
     *            the url parameters (or null if not needed)
     * @param headers
     *            additional headers for the request (or null if not needed)
     * @param data
     *            the json data of the request
     * @return the results of the request
     */
    private static HttpResult executeRequest(String sMethod, String sUrl, Map<String, String[]> params,
                                            Map<String, String> headers, JSONObject data) {
        StringBuilder result = new StringBuilder();
        int responseCode = 0;
        Map<String, List<String>> responseHeaders = new HashMap<>(0);
        URL url;
        HttpURLConnection conn = null;
        try {
            Log.i(Constants.LOGGER_TAG,String.format("%s request to %s%s", sMethod, sUrl, getParametersString(params)));

            url = new URL(sUrl + getParametersString(params));

            conn = (HttpURLConnection) url.openConnection();

            if(SUPPORTED_METHODS.contains(sMethod))
                conn.setRequestMethod(sMethod);
            else {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", sMethod);
            }
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Encoding", ENCODING);

            if (headers != null)
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    Log.v(Constants.LOGGER_TAG,String.format("\theader %s : %s", entry.getKey(), entry.getValue()));
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }

            if (data != null) {
                Log.v(Constants.LOGGER_TAG,String.format( "\tdata : %s", data.toString()));
                conn.setRequestProperty("Content-Type", "application/json");
                byte[] bData = data.toString().getBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(bData.length));
                conn.setDoOutput(true);

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.write(bData);
                }
            }

            responseCode = conn.getResponseCode();

            if (responseCode == 301 || responseCode == 302) {
                String newURL = conn.getHeaderField("Location");
                Log.i(Constants.LOGGER_TAG,String.format( "Redirected to %s", newURL));
                return executeRequest(sMethod, newURL, params, headers);
            }

            readInputStream(result, conn.getInputStream());
            responseHeaders = conn.getHeaderFields();
        } catch (IOException e) {
            Log.e(Constants.LOGGER_TAG,"Error in request", e);
            if (conn != null && responseCode >= 200)
                try {
                    readInputStream(result, conn.getErrorStream());
                } catch (IOException e1) {
                    Log.w(Constants.LOGGER_TAG,"Error in reading content",e);
        }
        }
        Log.i(Constants.LOGGER_TAG,String.format("response : %s", result.toString().replace("\n", "")));
        return new HttpResult(responseCode, result.toString(), responseHeaders);
    }

    /**
     * Read an InputStream into a StringBuilder
     *
     * @param sb
     *            the StringBuilder to use
     * @param is
     *            the InputStream to read
     * @throws IOException exception
     */
    private static void readInputStream(StringBuilder sb, InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            for (int c; (c = br.read()) >= 0;)
                sb.append((char) c);
        }
    }

    /**
     * Write the parameters into a url encoded format
     *
     * @param params
     *            the params to pass to the request
     * @return the url format of the parameters
     */
    private static String getParametersString(Map<String, String[]> params) {
        if (params == null)
            return "";
        StringBuilder result = new StringBuilder();
        boolean first = true;
        try {
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                for (String p : entry.getValue()) {
                    if (first)
                        first = false;
                    else
                        result.append("&");

                    result.append(URLEncoder.encode(entry.getKey(), ENCODING));

                    result.append("=");
                    result.append(URLEncoder.encode(p, ENCODING));
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.w(Constants.LOGGER_TAG,"Unsupported encoding UTF-8", e);
        }
        return "?" + result.toString();
    }

    /**
     * A class containing the simple results of a http request
     */
    public static class HttpResult {

        public final int code;
        public final String result;
        public final Map<String, List<String>> headers;
        private JSONObject json = null;

        HttpResult(int code, String result, Map<String, List<String>> headers) {
            super();
            this.code = code;
            this.result = result;
            this.headers = headers;
        }

        /**
         * @return the result of the request parsed as JSON
         */
        public JSONObject getJSON() {
            if (json == null)
                try {
                    json = new JSONObject(result);
                } catch (JSONException e) {
                    Log.e(Constants.LOGGER_TAG,"Cannot parse JSON", e);
                    json = new JSONObject();
                }
            return json;
        }
    }
}
