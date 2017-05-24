package com.amycui.medsminder.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amycui.medsminder.R;

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

    public PrescriptionCardView(View rootView, Context context){
        mContext = context;
        ButterKnife.bind(this, rootView);

        mPrescriptionImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_card_image));

        mPrescriptionName.setText(mContext.getResources().getText(R.string.default_prescription_title));

        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:  go to edit acitivity
            }
        });

        mInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: go to drug info activity
            }
        });
    }

    public void SetPrescriptionImage(String imgPath){
        try {
            mPrescriptionImage.setImageBitmap(BitmapFactory.decodeFile(imgPath));
        }catch(Exception e){

            Timber.e("SetPrescriptionImage: ", e.getMessage());
        }
    }


    public void SetPrescriptionName(String name){
        mPrescriptionName.setText(name);
    }

    public void SetPrescriptionDate(String date){
        mPrescriptionDate.setText(date);
    }
}
