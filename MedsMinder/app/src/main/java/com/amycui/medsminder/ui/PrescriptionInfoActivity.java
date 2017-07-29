package com.amycui.medsminder.ui;

import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amycui.medsminder.R;
import com.amycui.medsminder.web.DrugInfoJSONHelper;
import com.amycui.medsminder.web.DrugInfoLoader;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PrescriptionInfoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    //region constants
    //Loader id for drug info loader
    private static final int DRUG_INFO_LOADER = 301;
    //endregion

    //region View Fields
    @BindView(R.id.info_generic_name) TextView mGenericNameText;
    @BindView(R.id.info_description) TextView mDescriptionText;
    @BindView(R.id.info_purpose) TextView mPurposeText;
    @BindView(R.id.info_indications_and_usage) TextView mIndicationsText;
    @BindView(R.id.info_warnings) TextView mWarningsText;

    private Toolbar mToolbar;
    //endregion

    //region Class Fields
    private String mDrugName;
    //endregion

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_info);
        //bind views
        ButterKnife.bind(this);
        //set default text
        String defaultText = this.getResources().getString(R.string.info_default_text);
        mGenericNameText.setText(defaultText);
        mDescriptionText.setText(defaultText);
        mPurposeText.setText(defaultText);
        mIndicationsText.setText(defaultText);
        mWarningsText.setText(defaultText);
        //get drug name
        mDrugName = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        //setup toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar_info_activity);
        setSupportActionBar(mToolbar);
        String title = "";
        if(mDrugName != null && !mDrugName.isEmpty())
            title = getResources().getString(R.string.info_activity_title,mDrugName);
        else
            title = getResources().getString(R.string.info_activity_title_default);
        setTitle(title);
        // Enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //load drug info
        getSupportLoaderManager().initLoader(DRUG_INFO_LOADER,null,this).forceLoad();
    }
    //endregion

    //region Loader functions
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        if(id == DRUG_INFO_LOADER){
             if(mDrugName != null && !mDrugName.isEmpty()){
                 return new DrugInfoLoader(this,mDrugName);
             }
             else{
                 Timber.e("No DrugName provided. Can't load info");
                 return null;
             }
        }
        else {
            Timber.e("Unknown loader id: " + id);
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(loader.getId() == DRUG_INFO_LOADER){
            if(data!=null) {
                String[] drugInfo = null;
                try {
                    drugInfo = DrugInfoJSONHelper.GetDrugInfoFromJson(data);
                } catch (JSONException e) {
                    Timber.e("Error parsing JSON result: " + e.getMessage());
                }
                if (drugInfo != null && drugInfo.length > 0) {
                    setDrugInfoViews(drugInfo);
                }
            }
            else{
                Toast.makeText(this, mDrugName + " info not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
    //endregion

    //region other override functions

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // support back button
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //endregion

    private void setDrugInfoViews(String[] viewData){
        String genericName = viewData[DrugInfoJSONHelper.DrugInfoItems.generic_name.ordinal()];
        if(genericName!=null && !genericName.isEmpty())
            mGenericNameText.setText(genericName);

        String description = viewData[DrugInfoJSONHelper.DrugInfoItems.description.ordinal()];
        if(description!=null && !description.isEmpty())
            mDescriptionText.setText(description);

        String purpose = viewData[DrugInfoJSONHelper.DrugInfoItems.purpose.ordinal()];
        if(purpose!=null && !purpose.isEmpty())
            mPurposeText.setText(purpose);

        String indications = viewData[DrugInfoJSONHelper.DrugInfoItems.indications_and_usage.ordinal()];
        if(indications!=null && !indications.isEmpty())
            mIndicationsText.setText(indications);

        String warnings = viewData[DrugInfoJSONHelper.DrugInfoItems.warnings.ordinal()];
        if(warnings!=null && !warnings.isEmpty())
            mWarningsText.setText(warnings);
    }
}
