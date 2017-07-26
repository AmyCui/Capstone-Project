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

    @BindView(R.id.card_image) AspectRatioImageView mPrescriptionImage;
    @BindView(R.id.card_title)
    TextView mPrescriptionName;
    @BindView(R.id.card_subtitle) TextView mPrescriptionDate;
    @BindView(R.id.card_edit_btn)
    Button mEditBtn;
    @BindView(R.id.card_info_btn) Button mInfoBtn;

    private Context mContext;
    private String mPrescriptionNameText;
    private String mPrescriptionId;

    public PrescriptionCardView(View rootView, Context context){
        mContext = context;
        ButterKnife.bind(this, rootView);

        mPrescriptionImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_card_image));

        mPrescriptionName.setText(mContext.getResources().getText(R.string.default_prescription_title));

        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPrescriptionEditActivity();
            }
        });

        mInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPrescriptionInfoActivity();
            }
        });
    }

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

    public void SetPrescriptionId(String id){
        mPrescriptionId = id;
    }

    public void SetPrescriptionImage(String imgPath){
        try {
            Picasso.with(mContext).load(imgPath).fit().centerInside().into(mPrescriptionImage);
        }catch(Exception e){

            Timber.e("SetPrescriptionImage: ", e.getMessage());
        }
    }


    public void SetPrescriptionName(String name){
        mPrescriptionNameText = name;
        mPrescriptionName.setText(mPrescriptionNameText);
    }

    public void SetPrescriptionDate(String date){
        mPrescriptionDate.setText(date);
    }
}
