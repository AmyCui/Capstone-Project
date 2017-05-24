package com.amycui.medsminder.web;


import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class DrugInfoLoader extends AsyncTaskLoader {

    String mDrugname;
    Context mContext;

    public DrugInfoLoader(Context context, String drugname){
        super(context);
        mDrugname = drugname;
        mContext = context;
    }

    @Override
    public Object loadInBackground() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try {

            URL url = buildDrugInfoRequestUrl(mDrugname);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            long count = 0;
            while ((line = reader.readLine()) != null) {
                count += line.length();
                buffer.append(line + "\n");
            }
            moviesJsonStr = buffer.toString();
            return moviesJsonStr;

        } catch (Exception e) {
            Timber.e(e.getMessage());
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Timber.e("Error closing stream: ", e.getMessage());
                }
            }
        }
    }


    /**
     * Build query to drug info from openFDA.com using drug name
     * @param drugname commercial, or, generic drug name
     * @return query url
     * @throws MalformedURLException
     */
    public URL buildDrugInfoRequestUrl(String drugname) throws MalformedURLException {
        //example request url looks like this:
        //https://api.fda.gov/drug/label.json?search=openfda.generic_name:"aspirin"+openfda.brand_name:"aspirin"
        final String OPENFDA_BASE_URL = "https://api.fda.gov/drug/";
        final String LABEL_PATH = "label.json";
        final String SEARCH_KEY = "search";
        final String GENERIC_NAME_PARAM = "openfda.generic_name";
        final String BRAND_NAME_PARAM = "openfda.brand_name";
        final String SEARCH_VALUE = GENERIC_NAME_PARAM + ":\"" + drugname + "\"" +
                "+" +BRAND_NAME_PARAM + ":\"" + drugname + "\"";

        Uri builtUri = Uri.parse(OPENFDA_BASE_URL).buildUpon()
                .appendPath(LABEL_PATH)
                .appendQueryParameter(SEARCH_KEY,SEARCH_VALUE)
                .build();

        URL url = new URL(builtUri.toString());
        return url;
    }
}
