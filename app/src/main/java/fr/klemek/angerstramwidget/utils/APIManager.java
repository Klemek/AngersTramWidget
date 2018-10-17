package fr.klemek.angerstramwidget.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import fr.klemek.angerstramwidget.TimeList;

public final class APIManager {

    private APIManager() {

    }

    private static HashMap<String, String[]> getAPIParameters(String stop, boolean forward) {
        HashMap<String, String[]> params = new HashMap<>();
        params.put("dataset", new String[]{"bus-tram-circulation-passages"});
        params.put("refine.mnemoligne", new String[]{"A"});
        params.put("rows", new String[]{"100"});
        params.put("sort", new String[]{"-departtheorique"});
        if (forward) {
            params.put("refine.mnemoarret", new String[]{"1" + stop});
            //params.put("refine.dest", new String[]{"ANGERS ROSERAIE"});
        } else {
            params.put("refine.mnemoarret", new String[]{"2" + stop});
            //params.put("refine.dest", new String[]{"AVRILLE ARDENNE"});
        }
        params.put("timezone", new String[]{"Europe/Paris"});
        return params;
    }

    public static TimeList loadList(String stop, boolean forward) {
        TimeList list = new TimeList();
        HttpUtils.HttpResult res = HttpUtils.executeRequest("GET", Constants.API_URL, getAPIParameters(stop, forward));
        Log.i(Constants.LOGGER_TAG, res.result.length()+" bytes received");
        if (res.code != 200 && res.getJSON().length() == 0)
            return null;
        try {
            JSONArray records = res.getJSON().getJSONArray("records");
            Log.i(Constants.LOGGER_TAG, records.length() + " records in response");
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                if (record.has("fields") && record.getJSONObject("fields").has("depart")) {
                    list.addOffsetDateTime(record.getJSONObject("fields").getString("depart"));
                }
            }
        } catch (JSONException e) {
            Log.e(Constants.LOGGER_TAG, "JSON Exception", e);
        }
        list.sort();
        Log.i(Constants.LOGGER_TAG, list.size() + " records available");
        return list;
    }

}
