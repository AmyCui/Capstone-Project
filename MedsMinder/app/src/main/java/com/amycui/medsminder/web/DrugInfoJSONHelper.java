package com.amycui.medsminder.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class DrugInfoJSONHelper {


    private static final String DRUG_INFO_RESULT_JSON_TOKEN = "results";
    private static final String OPEN_FDA_JSON_TOKEN = "openfda";

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
        JSONArray drugInfoResultArray = drugInfObject.getJSONArray(DRUG_INFO_RESULT_JSON_TOKEN);
        JSONObject drugInfoResultObject = drugInfoResultArray.getJSONObject(0);
        JSONObject openFDAInfoObject = drugInfoResultObject.getJSONObject(OPEN_FDA_JSON_TOKEN);

        //parse generic name
        int generic_name_idx = DrugInfoItems.generic_name.ordinal();
        try{
            String generic_name = openFDAInfoObject.getJSONArray(DrugInfoItems.generic_name.toString()).getString(0);
            drugInfoResult[generic_name_idx] = generic_name;
        }catch (JSONException e){
            Timber.e("generic_name not found!");
        }
        //parse description
        int description_idx = DrugInfoItems.description.ordinal();
        try{
            String description = drugInfoResultObject.getJSONArray(DrugInfoItems.description.toString()).getString(0);
            drugInfoResult[description_idx] = description;
        }catch (JSONException e){
            Timber.e("description not found!");
        }
        //parse purpose
        int purpose_idx = DrugInfoItems.purpose.ordinal();
        try{
            String purpose = drugInfoResultObject.getJSONArray(DrugInfoItems.purpose.toString()).getString(0);
            drugInfoResult[purpose_idx] = purpose;
        }catch (JSONException e){
            Timber.e("purpose not found!");
        }
        //parse indications
        int indications_idx = DrugInfoItems.indications_and_usage.ordinal();
        try{
            String indications = drugInfoResultObject.getJSONArray(DrugInfoItems.indications_and_usage.toString()).getString(0);
            drugInfoResult[indications_idx] = indications;
        }catch (JSONException e){
            Timber.e("indications not found!");
        }
        //parse indications
        int warnings_idx = DrugInfoItems.warnings.ordinal();
        try{
            String warnings = drugInfoResultObject.getJSONArray("*" + DrugInfoItems.warnings.toString() + "*").getString(0);
            drugInfoResult[warnings_idx] = warnings;
        }catch (JSONException e){
            Timber.e("indications not found!");
        }


        return drugInfoResult;
    }

    /**
     * The drug information items to be extracted and displayed
     */
    public static enum DrugInfoItems{
        generic_name,
        description,
        purpose,
        indications_and_usage,
        warnings
    }
}


