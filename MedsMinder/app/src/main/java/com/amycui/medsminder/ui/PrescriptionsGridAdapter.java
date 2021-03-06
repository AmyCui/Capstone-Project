package com.amycui.medsminder.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.amycui.medsminder.R;
import com.amycui.medsminder.data.PrescriptionDbHelper;

import java.util.ArrayList;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PrescriptionsGridAdapter extends ArrayAdapter {

    /**
     * The data items needed for prescription grid
     */
    public static enum PrescriptionGridItems{
        prescription_id,
        prescription_image_url,
        prescription_name,
        prescription_date
    }



    private Context mContext;
    private Activity mActivity;
    private int mLayoutResourceId;
    private ArrayList mViewDataList;


    public PrescriptionsGridAdapter(Context context, Activity activity, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        mContext = context;
        mActivity = activity;
        mLayoutResourceId = layoutResourceId;
        mViewDataList = data;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //inflate view if hasn't done so
        View itemView = view;

        if(itemView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            itemView = inflater.inflate(mLayoutResourceId, parent, false);
        }
        //create a cardview for this item
        PrescriptionCardView cardView = new PrescriptionCardView(itemView, mContext, mActivity);
        String[] viewData = (String[]) mViewDataList.get(position);
        if(viewData != null){
            String id = viewData[PrescriptionGridItems.prescription_id.ordinal()];
            if(id != null && !id.isEmpty()) cardView.SetPrescriptionId(id);

            String imgPath = viewData[PrescriptionGridItems.prescription_image_url.ordinal()];
            if(imgPath != null && !imgPath.isEmpty()) cardView.SetPrescriptionImage(imgPath);

            String name = viewData[PrescriptionGridItems.prescription_name.ordinal()];
            if(name != null && !name.isEmpty()) {
                cardView.SetPrescriptionName(name);
            }else{
                cardView.DisableInfoButton();
            }


            String date = viewData[PrescriptionGridItems.prescription_date.ordinal()];
            if(date != null && !date.isEmpty()) cardView.SetPrescriptionDate(date);
        }

        return itemView;
    }


    public void SetData(ArrayList newdata)
    {
        mViewDataList.clear();
        mViewDataList = newdata;
    }

    @Override
    public void clear() {
        super.clear();
        mViewDataList.clear();
    }

    @Override
    public void addAll(Object[] items) {
        super.addAll(items);
        mViewDataList = new ArrayList();
        for(int i=0; i< items.length; i++)
        {
            mViewDataList.add(i, items[i]);
        }
    }

    @Nullable
    @Override
    public String[] getItem(int position) {
        return (String[])mViewDataList.get(position);
    }



}
