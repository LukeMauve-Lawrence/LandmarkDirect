package com.example.landmarkdirect;


import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Parses info from Google's API
public class MyJsonParser {
    private HashMap<String, String> parseJsonObject(JSONObject object) {
        HashMap<String, String> dataList = new HashMap<>();
        try {
            //get from object
            String id = object.getString("place_id");

            String name = object.getString("name");

            String latitude = object.getJSONObject("geometry").getJSONObject("location")
                    .getString("lat");
            String longitude = object.getJSONObject("geometry").getJSONObject("location")
                    .getString("lng");

            //place values into hash map
            dataList.put("place_id", id);
            dataList.put("name", name);
            dataList.put("lat", latitude);
            dataList.put("lng", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    private List<HashMap<String,String>> parseJsonArray(JSONArray jsonArray) {
        List<HashMap<String,String>> dataList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                HashMap<String,String> data = parseJsonObject((JSONObject) jsonArray.get(i));
                dataList.add(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }

    public List<HashMap<String,String>> parseResult(JSONObject object) {
        JSONArray jsonArray = null;

        try {
            jsonArray = object.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parseJsonArray(jsonArray);
    }
}
