package com.amycui.medsminder.widget;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.amycui.medsminder.R;
import com.amycui.medsminder.data.PrescriptionContract;

public class WidgetRemoteViewsService extends RemoteViewsService {

    private static final String[] WIDGET_PRESCRIPTION_COLUMNS = {
            PrescriptionContract.PrescriptionEntry._ID,
            PrescriptionContract.PrescriptionEntry.COLUMN_IMAGE_URL,
            PrescriptionContract.PrescriptionEntry.COLUMN_NAME,
            PrescriptionContract.PrescriptionEntry.COLUMN_DOSAGE,
            PrescriptionContract.PrescriptionEntry.COLUMN_UNIT,
            PrescriptionContract.PrescriptionEntry.COLUMN_FREQUENCY,
            PrescriptionContract.PrescriptionEntry.COLUMN_REPEAT_UNIT
    };

    private static final String[] WIDGET_REMINDERS_COLUMNS = {
            PrescriptionContract.RemindersEntry.COLUMN_START_DATE,
            PrescriptionContract.RemindersEntry.COLUMN_END_DATE
    };


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor prescriptionData = null;
            private Cursor reminderData = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (prescriptionData != null) {
                    prescriptionData.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                prescriptionData = getContentResolver().query(PrescriptionContract.PrescriptionEntry.CONTENT_URI,
                        WIDGET_PRESCRIPTION_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(prescriptionData != null){
                    prescriptionData.close();
                    prescriptionData = null;
                }
                if(reminderData != null){
                    reminderData.close();
                    reminderData = null;
                }
            }

            @Override
            public int getCount() {
                return prescriptionData == null? 0 : prescriptionData.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        prescriptionData == null || !prescriptionData.moveToPosition(position)) {
                    return null;
                }

                // create remote views
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                // get cursor data
                prescriptionData.moveToPosition(position);

                // get image url
                String imgsrc = prescriptionData.getString(prescriptionData.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_IMAGE_URL));
                Bitmap bitmap = null;
                if(imgsrc != null && !imgsrc.isEmpty()){
                    bitmap = BitmapFactory.decodeFile(imgsrc);
                }else{
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_card_image);
                }
                views.setImageViewBitmap(R.id.widget_list_image, bitmap);
                // get prescription name
                String name = prescriptionData.getString(prescriptionData.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_NAME));
                if(name != null && !name.isEmpty()){
                    views.setTextViewText(R.id.widget_list_pres_name, name);
                }
                // Construct Rx instructions text
                String dosage = prescriptionData.getString(prescriptionData.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_DOSAGE));
                String unit = prescriptionData.getString(prescriptionData.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_UNIT));
                String frequency = prescriptionData.getString(prescriptionData.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_FREQUENCY));
                String repeat = prescriptionData.getString(prescriptionData.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_REPEAT_UNIT));
                if(dosage != null && unit != null && frequency != null && repeat != null){
                    String rxInstruction = getResources().getString(R.string.widget_rx_instruction_text, dosage,unit,frequency,repeat);
                    views.setTextViewText(R.id.widget_list_instruction, rxInstruction);
                }
                // Construct reminders Start ~ End date
                String id = prescriptionData.getString(0);
                if(id != null && !id.isEmpty()){
                    reminderData = getContentResolver().query(PrescriptionContract.RemindersEntry.CONTENT_URI,
                            WIDGET_REMINDERS_COLUMNS,
                            PrescriptionContract.RemindersEntry._ID + " = ?",
                            new String[]{id},
                            null
                    );
                    if(reminderData != null && reminderData.moveToFirst()){
                        String startdate = reminderData.getString(reminderData.getColumnIndex(PrescriptionContract.RemindersEntry.COLUMN_START_DATE));
                        String enddate = reminderData.getString(reminderData.getColumnIndex(PrescriptionContract.RemindersEntry.COLUMN_END_DATE));
                        if(startdate != null && enddate != null){
                            String dates = getResources().getString(R.string.widget_dates_text, startdate, enddate);
                            views.setTextViewText(R.id.widget_list_date_range, dates);
                        }
                    }
                }
                // setup fillin intent
                Intent fillinintent = new Intent();
                fillinintent.putExtra(Intent.EXTRA_TEXT, id);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillinintent);


                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
