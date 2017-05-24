package com.amycui.medsminder.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DrugInfoJSONHelper {


    private static final String DRUG_INFO_RESULT_JSON_TOKEN = "results";

    /**
     * Parse DrugInfoItems out of input JSON data.
     * @param drugInfoJsonData: json result queried from openFDA label database
     * @return String array contains information specified in DrugInfoItems enum
     * @throws JSONException
     */
    public static String[] GetDrugInfoFromJson(String drugInfoJsonData) throws JSONException {

        int numOfEntries = DrugInfoItems.values().length;
        String[] drugInfoResult = new String[numOfEntries];

        JSONObject drugInfObject = new JSONObject(drugInfoJsonData);
        JSONArray drugInfoResultArray = drugInfObject.getJSONArray("DRUG_INFO_RESULT_JSON_TOKEN");
        JSONObject drugInfoResultObject = drugInfoResultArray.getJSONObject(0);

        for(int i=0; i<numOfEntries; i++){
            String resultString  = drugInfoResultObject.getJSONArray(DrugInfoItems.values()[i].toString()).getString(0);
            drugInfoResult[i] = resultString;
        }

        return drugInfoResult;
    }

    /**
     * The drug information items to be extracted and displayed
     */
    public enum DrugInfoItems{
        generic_name,
        description,
        purpose,
        indications_and_usage,
        warnings
    }
}


