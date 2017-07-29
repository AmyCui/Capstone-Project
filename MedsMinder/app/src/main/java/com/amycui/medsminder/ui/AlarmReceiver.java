package com.amycui.medsminder.ui;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.amycui.medsminder.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    private int mAlarmId = -1;
    private String mImgUrl;
    private String mDosage;
    private String mUnit;
    private String mPresName;
    private String mAlarmEnddate;


    @Override
    public void onReceive(Context context, Intent intent) {

        //get notification data from intent first

        String[] notificationData = intent.getStringArrayExtra(Intent.EXTRA_TEXT);
        if(notificationData == null)
            return;
        mImgUrl = notificationData[AlarmReceiver.NotificationItems.prescription_image_url.ordinal()];
        mPresName = notificationData[AlarmReceiver.NotificationItems.prescription_name.ordinal()];
        mDosage = notificationData[AlarmReceiver.NotificationItems.prescription_dosage.ordinal()];
        mUnit = notificationData[AlarmReceiver.NotificationItems.prescription_unit.ordinal()];
        mAlarmId = Integer.parseInt(notificationData[NotificationItems.alarm_id.ordinal()]);
        mAlarmEnddate = notificationData[NotificationItems.alarm_enddate.ordinal()];

        //check if the alarm needs to be cancelled
        String dateFormat = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        Date alarmEnddate = null;
        try {
            alarmEnddate = sdf.parse(mAlarmEnddate);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        if(alarmEnddate != null){
            Calendar endDateCalendar = Calendar.getInstance();
            endDateCalendar.setTime(alarmEnddate);
            //cancel alarm if enddate < today's date
            if(endDateCalendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
                try {
                    Intent notificationIntent = new Intent(context, AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, mAlarmId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

        }

        if(mDosage == null || mUnit == null || mPresName == null)
            return;

        //create notification manager
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Create the content intent for the notification, which launches this activity
        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (context, mAlarmId, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build the notification
        String notification_title = context.getResources().getString(R.string.notification_title, mDosage, mUnit,mPresName);
        // String notification_text = ???
        Bitmap prescriptionImg = BitmapFactory.decodeFile(mImgUrl);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                //.setContentText(context.getString(R.string.notification_text))
                .setLargeIcon(prescriptionImg)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        //Deliver the notification
        notificationManager.notify(mAlarmId, builder.build());
    }

    public static enum NotificationItems{
        prescription_image_url,
        prescription_name,
        prescription_dosage,
        prescription_unit,
        alarm_id,
        alarm_enddate
    }
}
