package fr.klemek.angerstramwidget;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;

import fr.klemek.angerstramwidget.utils.HttpUtils;
import fr.klemek.angerstramwidget.utils.HttpUtils.HttpResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpUtilsTest {

    @Test
    public void testExecuteRequestError0() {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.orga/");
        assertEquals(0, hr.code);
    }

    @Test
    public void testExecuteRequestError404() {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/404");
        assertEquals(404, hr.code);
    }

    @Test
    public void testExecuteRequestGET() throws JSONException {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/get");
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        assertNotNull(hr.getJSON());
        JSONObject obj = hr.getJSON();
        assertEquals("http://httpbin.org/get", obj.getString("url"));
    }

    @Test
    public void testExecuteRequestGETRedirect() {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://klemek.fr");
        assertEquals(200, hr.code);
    }

    @Test
    public void testExecuteRequestGETParameters() throws JSONException {
        HashMap<String, String[]> params = new HashMap<>();
        params.put("testparam", new String[]{"testvalue"});
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/get", params);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertNotNull(obj);
        assertTrue(obj.has("args"));
        assertTrue(obj.getJSONObject("args").has("testparam"));
        assertEquals("testvalue", obj.getJSONObject("args").get("testparam"));
    }

    @Test
    public void testExecuteRequestGETParameters2() throws JSONException {
        HashMap<String, String[]> params = new HashMap<>();
        params.put("testparam", new String[]{"testvalue", "testvalue2"});
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/get", params);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertNotNull(obj);
        assertTrue(obj.has("args"));
        assertTrue(obj.getJSONObject("args").has("testparam"));
        assertEquals(2, obj.getJSONObject("args").getJSONArray("testparam").length());
    }

    @Test
    public void testExecuteRequestGETHeaders() throws JSONException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Testheader", "testvalue");
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/headers", null, headers);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertNotNull(obj);
        assertTrue(obj.has("headers"));
        assertTrue(obj.getJSONObject("headers").has("Testheader"));
        assertEquals("testvalue", obj.getJSONObject("headers").get("Testheader"));
    }

    @Test
    public void testExecuteRequestPOSTData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("testdata", "testvalue");
        HttpResult hr = HttpUtils.executeRequest("POST", "http://httpbin.org/post", data);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertNotNull(obj);
        assertTrue(obj.getJSONObject("json").has("testdata"));
        assertEquals("testvalue", obj.getJSONObject("json").get("testdata"));
    }

    @Test
    public void testJSON() throws JSONException{
        JSONObject obj = new JSONObject();
        assertFalse(obj.has("test_key"));
        obj.put("test_key","test_value");
        assertTrue(obj.has("test_key"));
        assertEquals("test_value", obj.get("test_key"));
        assertEquals("{\"test_key\":\"test_value\"}", obj.toString());
    }

}
