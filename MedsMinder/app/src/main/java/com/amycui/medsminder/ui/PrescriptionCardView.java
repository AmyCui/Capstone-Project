package com.amycui.medsminder.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
    private String mPrescriptionNameText;
    private String mPrescriptionId;
    //endregion

    //region Constructor
    public PrescriptionCardView(View rootView, Context context){
        mContext = context;
        ButterKnife.bind(this, rootView);
        //set a default image for the card
        mPrescriptionImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_card_image));
        //set a default prescription name
        mPrescriptionName.setText(mContext.getResources().getText(R.string.default_prescription_title));
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

    //endregion

    //region private methods
    private void launchPrescriptionEditActivity(){
        // create intent to launch presceiptionEditActivity
        Intent intent = new Intent(mContext, PrescriptionEditActivity.class);
        // pass in current card represented prsecription name
        intent.putExtra(Intent.EXTRA_TEXT, mPrescriptionId);

        mContext.startActivity(intent);
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
