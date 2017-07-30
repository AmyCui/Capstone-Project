package com.amycui.medsminder.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amycui.medsminder.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PrescriptionCardView {

    //region View fields
    @BindView(R.id.card_image)
    AspectRatioImageView mPrescriptionImage;
    @BindView(R.id.card_title) TextView mPrescriptionName;
    @BindView(R.id.card_subtitle) TextView mPrescriptionDate;
    @BindView(R.id.card_edit_btn) Button mEditBtn;
    @BindView(R.id.card_info_btn) Button mInfoBtn;
    //endregion

    //region fields
    private Context mContext;
    private Activity mActivity;
    private String mPrescriptionNameText;
    private String mPrescriptionId;
    private Bitmap mBitmap;
    //endregion

    //region Constructor
    public PrescriptionCardView(View rootView, Context context, Activity activity){
        mContext = context;
        mActivity = activity;
        ButterKnife.bind(this, rootView);
        //mPrescriptionImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_card_image));
        Picasso.with(mContext).load(R.drawable.default_card_image).into(mPrescriptionImage);
        //Edit button will launch PrescriptionEditActivity
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPrescriptionEditActivity();
            }
        });
        //Info button will launch PrescriptionInfoActivity
        mInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPrescriptionInfoActivity();
            }
        });
        //transition
        if (Build.VERSION.SDK_INT >= 21) {
            mPrescriptionImage.setTransitionName("grid_element");
        }
    }
    //endregion



    //region public methods

    /**
     * Set the Prescription database table _ID for current prescription the card represents
     * The id will be used to look for the prescription data for this prescrition in PrescriptionEditActivity
     * @param id the Prescription database table _ID for current prescription the card represents
     */
    public void SetPrescriptionId(String id){
        mPrescriptionId = id;
    }

    /**
     * Set the image of the current prescription for the card
     * @param imgPath path for the image on phone
     */
    public void SetPrescriptionImage(String imgPath){
        try {
            Picasso.with(mContext).load(imgPath).fit().centerInside().into(mPrescriptionImage);
        }catch(Exception e){

            Timber.e("SetPrescriptionImage: ", e.getMessage());
        }
    }

    /**
     * Set the name of the current prescription for the card
     * @param name
     */
    public void SetPrescriptionName(String name){
        mPrescriptionNameText = name;
        mPrescriptionName.setText(mPrescriptionNameText);
    }

    /**
     * Set date of the current prescription
     * @param date
     */
    public void SetPrescriptionDate(String date){
        mPrescriptionDate.setText(date);
    }

    public void DisableInfoButton(){
        mInfoBtn.setEnabled(false);
    }

    //endregion

    //region private methods
    private void launchPrescriptionEditActivity(){
        // create intent to launch presceiptionEditActivity
        Intent intent = new Intent(mContext, PrescriptionEditActivity.class);
        // pass in current card represented prsecription name
        intent.putExtra(Intent.EXTRA_TEXT, mPrescriptionId);

        Bundle bundle = ActivityOptionsCompat
                .makeSceneTransitionAnimation(mActivity, mPrescriptionImage, "grid_element")
                .toBundle();

        mContext.startActivity(intent, bundle);
    }

    private void launchPrescriptionInfoActivity(){
        // create intent to launch presceiptionEditActivity
        Intent intent = new Intent(mContext, PrescriptionInfoActivity.class);
        // pass in current card represented prsecription name
        intent.putExtra(Intent.EXTRA_TEXT, mPrescriptionNameText);

        mContext.startActivity(intent);
    }
    //endregion


}
