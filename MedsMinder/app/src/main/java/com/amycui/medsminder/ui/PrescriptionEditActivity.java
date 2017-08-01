package com.amycui.medsminder.ui;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amycui.medsminder.R;
import com.amycui.medsminder.data.PrescriptionContract;
import com.amycui.medsminder.widget.WidgetProvider;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PrescriptionEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //region Constants
    // image picker activity request code
    private static final int PICK_IMAGE = 301;
    // Prescription edit cursor loader
    private static final int PRESCRIPTION_EDIT_LOADER = 101;
    // Reminders cursor loader
    private static final int REMINDERS_LOADER = 201;
    //endregion

    //region Data Fields
    private Toolbar mToolbar;
    //adapter for rx repeat (day/week/month) spinner
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    //prescription data from Prescription database
    private String mCurrentPrescriptionId;
    private String[] mCurrentPrescriptionData;
    //prescription ui fields from user input
    private String mPresImgPathUserInput;
    private String mDrugNameUserInput;
    private String mDrugUnitUserInput;
    private String mPresDateUserInput;
    private String mDosageUserInput;
    private String mFrequencyUserInput;
    private String mRepeatUserInput;
    //reminders data from reminders database
    private ArrayList<String[]> mCurrentRemindersData;
    private ArrayList<String> mCurrentReminderIds;
    //reminders ui fields from user input
    private String mReminderStartDateUserInput;
    private String mReminderEndDateUserInput;
    private ArrayList<String> mReminderTriggerTimeUserInput;
    private int mNumOfAlarms = 0;
    //fields for setting up alarms
    private NotificationManager mNotificationManager;
    private AlarmManager mAlarmManager;
    //endregion

    //region View Fields
    @BindView(R.id.pres_image_card) CardView mPresImageCard;
    @BindView(R.id.pres_image) ImageView mPresImage;
    @BindView(R.id.add_image_icon) ImageView mAddImageIcon;
    @BindView(R.id.add_image_text) TextView mAddImageText;
    @BindView(R.id.drug_name_edit) EditText mDrugName;
    @BindView(R.id.drug_unit_edit) EditText mDrugUnit;
    @BindView(R.id.pres_date_edit) EditText mPresDate;
    @BindView(R.id.dosage_edit) EditText mDosage;
    @BindView(R.id.frequency_edit) EditText mFrequency;
    @BindView(R.id.repeat_spinner) Spinner mRepeatSpinner;
    @BindView(R.id.reminders_checkbox) CheckBox mRemindersCheckbox;
    @BindView(R.id.reminders_label) TextView mReminderLabel;
    @BindView(R.id.reminder_instruction) TextView mRemindersInstruction;
    @BindView(R.id.alarmLayout) LinearLayout mAlarmLinearLayout;

    private ViewGroup mStartDateLayout;
    private TextView mStartDateLabel;
    private EditText mStartDateEdit;

    private ViewGroup mEndDateLayout;
    private TextView mEndDateLabel;
    private EditText mEndDateEdit;

    private ArrayList<ViewGroup> mAlarmLayoutList;
    private ArrayList<TextView> mAlarmTimeTextList;

    //endregion

    //region enum

    /**
     * Items needed from Prescription database table
     */
    public static enum PrescriptionEditItems{
        prescription_image_url,
        prescription_name,
        prescription_date,
        prescription_unit,
        prescription_dosage,
        prescription_frequency,
        prescription_repeat,
    }

    /**
     * Items needed from Reminders database table
     */
    public static enum PrescriptionReminderItems{
        reminder_id,
        reminder_start_date,
        reminder_end_date,
        reminder_trigger_time,
        reminder_repeat_ms
    }
    //endregion

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get prescription name
        mCurrentPrescriptionId = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        //set up views
        setContentView(R.layout.activity_prescription_edit);
        // ButterKnife bindview
        ButterKnife.bind(this);
        // Set up toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar_edit_activity);
        setSupportActionBar(mToolbar);
        // Enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Set up repeat spinner
        mSpinnerAdapter = createRepeatSpinnerArrayAdapter();
        mRepeatSpinner.setAdapter(mSpinnerAdapter);
        mRepeatSpinner.setOnItemSelectedListener(createSpinnerAdapterViewOnItemSelectedListener());
        // listen to Rx instruction section changes
        mDosage.addTextChangedListener(createOnTextChangedListener());
        mFrequency.addTextChangedListener(createOnTextChangedListener());
        // Set up prescription image click to show gallery
        mPresImageCard.setOnClickListener(createImageOnClickListener());
        // Set up prescription date DatePicker
        createDatePickerDialogForEditText(mPresDate);
        // Set up reminder section
        // add on checked changed listener to hide/show section
        mRemindersCheckbox.setOnCheckedChangeListener(createRemindersCheckboxOnCheckedChangedListener());
        // If is an existing prescription, check for if user has already set reminders
        if(mCurrentPrescriptionId != null && !mCurrentPrescriptionId.isEmpty()) {
            boolean reminderExist = getRemindersExistsForPrescription(mCurrentPrescriptionId);
            mRemindersCheckbox.setChecked(reminderExist);
            // If reminders exist, load reminders data
            if(reminderExist)
                getSupportLoaderManager().initLoader(REMINDERS_LOADER, null, this);
        }
        // Else this is a new prescription. there is no reminders set yet.
        else
            mRemindersCheckbox.setChecked(false);

        // load prescription data
        if(mCurrentPrescriptionId != null)
            getSupportLoaderManager().initLoader(PRESCRIPTION_EDIT_LOADER,null,this);

    }
    //endregion

    //region Activity Methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if is image picker then update image
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            mPresImgPathUserInput = data.getData().toString();
            updateDrugImage(mPresImgPathUserInput);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        getUserInputFromViews();
        SaveUserInputToDatabase();
        sendBroadcast(new Intent(WidgetProvider.ACTION_UPDATE));
        supportFinishAfterTransition();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region CursorLoader Methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Load prescription database information for the current prescription id
        if(id == PRESCRIPTION_EDIT_LOADER){
            return new CursorLoader(this, PrescriptionContract.PrescriptionEntry.CONTENT_URI,
                    null,
                    PrescriptionContract.PrescriptionEntry._ID + " = ?",
                    new String[]{mCurrentPrescriptionId},
                    null);
        }
        // Load reminders database information for the current prescription id
        else if(id == REMINDERS_LOADER){
            return new CursorLoader(this, PrescriptionContract.RemindersEntry.buildReminderUriFromPrescriptionId(mCurrentPrescriptionId),null,null,null,null);
        }
        else {
            Timber.e("Unknown loader id: " + id);
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // If callback trigger by prescription loader
        if(loader.getId() == PRESCRIPTION_EDIT_LOADER && data != null){
            mCurrentPrescriptionData = getCurrentPrescriptionData(data);
            updateViewsFromPrescriptionData(mCurrentPrescriptionData);
        }
        // If callback triggered by reminder loader
        else if(loader.getId()==REMINDERS_LOADER && data != null){
            mCurrentRemindersData = getCurrentRemindersData(data);
            updateViewsFromRemindersData(mCurrentRemindersData);
            createOrUpdateAlarms();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    //endregion

    //region Database
    private ArrayList<String[]> getCurrentRemindersData(Cursor cursor){
        ArrayList<String[]> reminders = null;

        if(cursor != null && cursor.moveToFirst()){
            reminders = new ArrayList<String[]>();
            do{
                String[] result = new String[PrescriptionReminderItems.values().length];
                result[PrescriptionReminderItems.reminder_id.ordinal()] =
                        cursor.getString(0);
                result[PrescriptionReminderItems.reminder_start_date.ordinal()] =
                        cursor.getString(cursor.getColumnIndex(PrescriptionContract.RemindersEntry.COLUMN_START_DATE));
                result[PrescriptionReminderItems.reminder_end_date.ordinal()] =
                        cursor.getString(cursor.getColumnIndex(PrescriptionContract.RemindersEntry.COLUMN_END_DATE));
                result[PrescriptionReminderItems.reminder_trigger_time.ordinal()] =
                        cursor.getString(cursor.getColumnIndex(PrescriptionContract.RemindersEntry.COLUMN_TRIGGER_TIME));
                result[PrescriptionReminderItems.reminder_repeat_ms.ordinal()] =
                        cursor.getString(cursor.getColumnIndex(PrescriptionContract.RemindersEntry.COLUMN_REPEAT_MS));
                reminders.add(result);
            }
            while(cursor.moveToNext());

        }

        return reminders;
    }

    private String[] getCurrentPrescriptionData(Cursor cursor){
        String[] result = null;
        if(cursor != null && cursor.moveToFirst()){
            result = new String[PrescriptionEditItems.values().length];
            result[PrescriptionEditItems.prescription_image_url.ordinal()] =
                    cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_IMAGE_URL));
            result[PrescriptionEditItems.prescription_name.ordinal()] =
                    cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_NAME));
            result[PrescriptionEditItems.prescription_date.ordinal()] =
                    cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_DATE));
            result[PrescriptionEditItems.prescription_unit.ordinal()] =
                    cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_UNIT));
            result[PrescriptionEditItems.prescription_dosage.ordinal()] =
                    cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_DOSAGE));
            result[PrescriptionEditItems.prescription_frequency.ordinal()] =
                    cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_FREQUENCY));
            result[PrescriptionEditItems.prescription_repeat.ordinal()] =
                    cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_REPEAT_UNIT));
        }
        return result;
    }

    /**
     * Save all the fields with *UserInput to database including prescriptions and reminders
     */
    private void SaveUserInputToDatabase(){
        SavePrescriptionUserInputToDatabase();
        if(mCurrentPrescriptionId != null){
            SaveRemindersUserInputToDatabase();
        }
        else
            Timber.e("No prescription data created. Cannot save reminder data");
    }

    private void SavePrescriptionUserInputToDatabase(){
        //create contentValue
        ContentValues newdata = new ContentValues();
        boolean hasNewData = false;
        newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_USER_KEY, MainActivity.getCurrentUserKey());
        if(mPresImgPathUserInput != null && !mPresImgPathUserInput.isEmpty()) {
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_IMAGE_URL, mPresImgPathUserInput);
            hasNewData = true;
        }
        if(mDrugNameUserInput != null && !mDrugNameUserInput.isEmpty()) {
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_NAME, mDrugNameUserInput);
            hasNewData = true;
        }
        if(mDrugUnitUserInput != null && !mDrugUnitUserInput.isEmpty()) {
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_UNIT, mDrugUnitUserInput);
            hasNewData = true;
        }
        if(mDosageUserInput != null && !mDosageUserInput.isEmpty()) {
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_DOSAGE, mDosageUserInput);
            hasNewData = true;
        }
        if(mPresDateUserInput != null && !mPresDateUserInput.isEmpty()) {
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_DATE, mPresDateUserInput);
            hasNewData = true;
        }
        if(mRepeatUserInput != null) {
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_REPEAT_UNIT, mRepeatUserInput);
        }
        if(mFrequencyUserInput != null && !mFrequencyUserInput.isEmpty()) {
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_FREQUENCY, mFrequencyUserInput);
            hasNewData = true;
        }
        // if no user input then don't create new data entry
        if(!hasNewData)
            return;
        // if id exist in database then update the entry
        if(mCurrentPrescriptionId != null){
            getContentResolver().update(
                    PrescriptionContract.PrescriptionEntry.CONTENT_URI,
                    newdata,
                    PrescriptionContract.RemindersEntry._ID + " = ?",
                    new String[]{mCurrentPrescriptionId});
        }
        // if id doesn't exist yet create a new entry and update id
        // id will be used to save reminders data
        else
        {
            Uri row = getContentResolver().insert(PrescriptionContract.PrescriptionEntry.CONTENT_URI, newdata);
            if(row != null)
                mCurrentPrescriptionId = PrescriptionContract.PrescriptionEntry.getUserFromUri(row);
        }
    }

    private void SaveRemindersUserInputToDatabase(){
        if(mAlarmTimeTextList != null && mAlarmTimeTextList.size()>0){
            int numOfAlarms = mAlarmTimeTextList.size();
            for(int i=0; i<numOfAlarms; i++){
                //create contentValue
                ContentValues newdata = new ContentValues();
                newdata.put(PrescriptionContract.RemindersEntry.COLUMN_PRESCRIPTION_ID, mCurrentPrescriptionId);
                newdata.put(PrescriptionContract.RemindersEntry.COLUMN_START_DATE, mReminderStartDateUserInput);
                newdata.put(PrescriptionContract.RemindersEntry.COLUMN_END_DATE,mReminderEndDateUserInput);
                newdata.put(PrescriptionContract.RemindersEntry.COLUMN_TRIGGER_TIME, mReminderTriggerTimeUserInput.get(i));
                //TODO: REPEAT MS
                newdata.put(PrescriptionContract.RemindersEntry.COLUMN_REPEAT_MS, "86400000");
                // If already exist, update entry
                if(mCurrentReminderIds != null && mCurrentReminderIds.size() > 0){
                    String id = mCurrentReminderIds.get(i);
                    getContentResolver().update(
                            PrescriptionContract.RemindersEntry.CONTENT_URI,
                            newdata,
                            PrescriptionContract.RemindersEntry._ID + " = ?",
                            new String[]{id}
                    );
                }
                // If is newly created, create a new entry in database
                else{
                     Uri row = getContentResolver().insert(
                             PrescriptionContract.RemindersEntry.CONTENT_URI,
                             newdata
                     );
                    if(row != null){
                        String newId = PrescriptionContract.RemindersEntry.getIdfromUri(row);
                        if(mCurrentReminderIds == null)
                            mCurrentReminderIds = new ArrayList<String>();
                        mCurrentReminderIds.add(newId);
                    }

                }
            }
        }

    }


    /**
     * Get current information in the input fields on page including edittext, spinner and some textviews
     */
    private void getUserInputFromViews(){
        getUserInputFromPrescriptionViews();
        getUserInputFromReminderViews();
    }



    private void getUserInputFromPrescriptionViews(){
        if(mDrugName != null) {
            mDrugNameUserInput = mDrugName.getText().toString();
        }
        if(mDrugUnit != null){
            mDrugUnitUserInput = mDrugUnit.getText().toString();
        }
        if(mPresDate != null){
            mPresDateUserInput = mPresDate.getText().toString();
        }
        if(mDosage != null) {
            mDosageUserInput = mDosage.getText().toString();
        }
        if(mFrequency != null){
            mFrequencyUserInput = mFrequency.getText().toString();
        }
        //TODO: repeat

    }

    private void getUserInputFromReminderViews(){
        if(mStartDateEdit != null){
            mReminderStartDateUserInput = mStartDateEdit.getText().toString();
        }
        if(mEndDateEdit != null){
            mReminderEndDateUserInput = mEndDateEdit.getText().toString();
        }
        if(mAlarmTimeTextList != null && mAlarmTimeTextList.size()>0){
            mReminderTriggerTimeUserInput = new ArrayList<String>();
            for(int i=0; i<mAlarmTimeTextList.size();i++){
                mReminderTriggerTimeUserInput.add(mAlarmTimeTextList.get(i).getText().toString());
            }
        }
    }

    private boolean getRemindersExistsForPrescription(String presId){
        Cursor result = this.getContentResolver().query(
                PrescriptionContract.RemindersEntry.CONTENT_URI,
                new String[]{PrescriptionContract.RemindersEntry._ID},
                PrescriptionContract.RemindersEntry.COLUMN_PRESCRIPTION_ID + " = ?",
                new String[]{presId},
                null
        );
        if(result != null && result.moveToFirst()) {
            mNumOfAlarms = result.getCount();
            return true;
        }
        return false;
    }

    /**
     * Update the UI textview fields based on data passed in from prescription database
     * @param prescriptionData String array of the prescription data for current prescription id
     */
    private void updateViewsFromPrescriptionData(String[] prescriptionData){
        if(prescriptionData != null){
            //update data to view
            String imgPath = prescriptionData[PrescriptionEditItems.prescription_image_url.ordinal()];
            updateDrugImage(imgPath);

            String name = prescriptionData[PrescriptionEditItems.prescription_name.ordinal()];
            updateDrugName(name);

            String presDate = prescriptionData[PrescriptionEditItems.prescription_date.ordinal()];
            updatePresDate(presDate);

            String unit = prescriptionData[PrescriptionEditItems.prescription_unit.ordinal()];
            updateDrugUnit(unit);

            String dosage = prescriptionData[PrescriptionEditItems.prescription_dosage.ordinal()];
            updateDosage(dosage);

            String frequency = prescriptionData[PrescriptionEditItems.prescription_frequency.ordinal()];
            updateFrequency(frequency);

            String repeat = prescriptionData[PrescriptionEditItems.prescription_repeat.ordinal()];
            //TODO: repeat update
        }
    }

    /**
     * Create or Update Reminders Views data from the Reminders database
     * @param remindersData ArrayList of string array of reminders data for current prescription id
     */
    private void updateViewsFromRemindersData(ArrayList<String[]> remindersData){
        if(remindersData != null && remindersData.size()>0){
            //update start date and end date. they are the same for all alarms
            String startdate = remindersData.get(0)[PrescriptionReminderItems.reminder_start_date.ordinal()];
            updateStartDate(startdate);
            String enddate = remindersData.get(0)[PrescriptionReminderItems.reminder_end_date.ordinal()];
            updateEndDate(enddate);
            //iterate to update alarm data
            ArrayList<String> timeList = new ArrayList<String>();
            mCurrentReminderIds = new ArrayList<String>();
            for(int i=0 ;i<remindersData.size();i++){
                String[] alarm = remindersData.get(i);
                String time = alarm[PrescriptionReminderItems.reminder_trigger_time.ordinal()];
                timeList.add(time);
                String id = alarm[PrescriptionReminderItems.reminder_id.ordinal()];
                mCurrentReminderIds.add(id);

            }
            updateAlarmTimeText(timeList);

        }
    }

    //endregion

    //region View Update Methods
    private void updateDrugImage(String uri){
        if(uri != null){
            mPresImgPathUserInput = uri;
            // Load image
            Picasso.with(this).load(uri).fit().centerInside().into(mPresImage);
            // Hide add image icon and text
            mAddImageIcon.setVisibility(View.INVISIBLE);
            mAddImageText.setVisibility(View.INVISIBLE);
        }
    }
    private void updateDrugName(String name){
        if(name != null){
            mDrugName.setText(name);
        }
    }
    private void updateDrugUnit(String unit){
        if(unit != null){
            mDrugUnit.setText(unit);
        }
    }
    private void updatePresDate(String date){
        if(date != null){
            mPresDate.setText(date);
        }
    }
    private void updateDosage(String dosage){
        if(dosage != null){
            mDosage.setText(dosage);
        }
    }
    private void updateFrequency(String frequency){
        if(frequency != null){
            mFrequency.setText(frequency);
        }
    }
    private void updateStartDate(String date){
        if(date != null && mStartDateEdit != null){
            mStartDateEdit.setText(date);
        }
    }
    private void updateEndDate(String date){
        if(date != null && mEndDateEdit != null){
            mEndDateEdit.setText(date);
        }
    }
    private void hideRemindersViews(){
       mAlarmLinearLayout.setVisibility(View.INVISIBLE);
    }
    private void createAndShowReminderViews(){
        mAlarmLinearLayout.setVisibility(View.VISIBLE);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        //create new reminder start date layout
        mStartDateLayout = (ViewGroup) inflater.inflate(R.layout.set_date,null);
        mStartDateLabel = (TextView)mStartDateLayout.findViewById(R.id.set_date_label);
        mStartDateLabel.setText(getResources().getText(R.string.start_date_title));
        mStartDateEdit = (EditText)mStartDateLayout.findViewById(R.id.set_date_date);
        mStartDateEdit.setContentDescription(getResources().getString(R.string.start_date_description));
        createDatePickerDialogForEditText(mStartDateEdit);
        //create default startdate
        createDefaultReminderStartDate();
        //create new reminder end date layout
        mEndDateLayout = (ViewGroup) inflater.inflate(R.layout.set_date,null);
        mEndDateLabel = (TextView)mEndDateLayout.findViewById(R.id.set_date_label);
        mEndDateLabel.setText(getResources().getText(R.string.end_date_title));
        mEndDateEdit = (EditText)mEndDateLayout.findViewById(R.id.set_date_date);
        mEndDateEdit.setContentDescription(getResources().getString(R.string.end_date_description));
        createDatePickerDialogForEditText(mEndDateEdit);
        //create default end date
        createDefaultReminderEndDate();
        //add to alarm linear layout
        mAlarmLinearLayout.addView(mStartDateLayout);
        mAlarmLinearLayout.addView(mEndDateLayout);
        //create new alarm layout
        int numOfAlarms = mNumOfAlarms;
        if(numOfAlarms == 0 && mFrequency != null && !mFrequency.getText().toString().isEmpty())
            numOfAlarms = Integer.parseInt(mFrequency.getText().toString());
        mAlarmLayoutList = new ArrayList<ViewGroup>();
        mAlarmTimeTextList = new ArrayList<TextView>();
        for(int i=0; i<numOfAlarms; i++){
            ViewGroup alarmLayout = (ViewGroup)inflater.inflate(R.layout.alarm, null);
            mAlarmLayoutList.add(alarmLayout);
            TextView alarmTimeText = (TextView)alarmLayout.findViewById(R.id.reminder_timepicker);
            mAlarmTimeTextList.add(alarmTimeText);
            //set up time picker
            createTimePickerDialogForTextView(alarmTimeText);
            //add to alarm linear layout
            mAlarmLinearLayout.addView(alarmLayout);
        }

    }
    private void updateRemindersInstructionText(){
        if(mDosage != null && !(mDosage.getText()).toString().isEmpty()
                && mFrequency != null && !(mFrequency.getText().toString().isEmpty())
                && mDrugName != null && !(mDrugName.getText().toString().isEmpty())){
            //hide instruction text
            mRemindersInstruction.setVisibility(View.GONE);
            //enable add reminders checkbox
            mRemindersCheckbox.setEnabled(true);
            mReminderLabel.setTextColor(getResources().getColor(R.color.colorTextBlack));
        }
        else{
            //show instruction text
            mRemindersInstruction.setVisibility(View.VISIBLE);
            //enable add reminders checkbox
            mRemindersCheckbox.setEnabled(false);
            mReminderLabel.setTextColor(getResources().getColor(R.color.colorTextGray));
        }
    }
    private void updateAlarmTimeText(ArrayList<String> textList){
        if(textList != null && textList.size()>0){
            for(int i=0; i< textList.size(); i++){
                String time = textList.get(i);

                if(time != null && mAlarmTimeTextList!= null && mAlarmTimeTextList.size()>0){
                    mAlarmTimeTextList.get(i).setText(time);
                }
            }
        }
    }
    private void updateDateforEdittext(EditText edittext, Date date){
        String dateFormat = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        edittext.setText(sdf.format(date));
    }
    //endregion

    //region private methods

    //create default start date set to today's date
    private void createDefaultReminderStartDate(){
        Calendar calendar = Calendar.getInstance();
        updateDateforEdittext(mStartDateEdit,calendar.getTime());
    }

    //create default end date set to +1day
    private void createDefaultReminderEndDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        updateDateforEdittext(mEndDateEdit, calendar.getTime());
    }

    /**
     *  an alarm id should have been created by the time this method is called.
     *  but check for this anyways. if not created, skip creating alarms
     */
    private void createOrUpdateAlarms(){

        //create notification manager and alarm manager
        if(mNotificationManager == null)
            mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(mAlarmManager == null)
            mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //set up alarm
        if(mCurrentRemindersData != null && !mCurrentRemindersData.isEmpty()){
            for(int i=0; i<mCurrentRemindersData.size();i++){
                String[] reminderData =  mCurrentRemindersData.get(i);
                //get alarm data
                String id = reminderData[PrescriptionReminderItems.reminder_id.ordinal()];
                String startdate = reminderData[PrescriptionReminderItems.reminder_start_date.ordinal()];
                String enddate = reminderData[PrescriptionReminderItems.reminder_end_date.ordinal()];
                String triggertime = reminderData[PrescriptionReminderItems.reminder_trigger_time.ordinal()];
                String repeatms = reminderData[PrescriptionReminderItems.reminder_repeat_ms.ordinal()];
                //parse to useful data format for setting alarm
                //id needs to be int
                int alarmId = Integer.parseInt(id);
                //startdate, enddate needs to be Date
                String dateFormat = "MM-dd-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
                Date alarmStartdate = null;
                try {
                    alarmStartdate = sdf.parse(startdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return;
                }
                // trigger time needs to be int hour of day and minute
                int alarmHour;
                int alarmMinute;
                String[] timecomp = triggertime.split(":");
                if(timecomp != null && timecomp.length == 2){
                    alarmHour = Integer.parseInt(timecomp[0]);
                    alarmMinute = Integer.parseInt(timecomp[1]);
                }
                else
                    return;
                // repeatms needs to be long
                long alarmRepeatms = Long.parseLong(repeatms);
                // get the string array to send with the alarm pending intent ready
                String[] prescriptionData = new String[AlarmReceiver.NotificationItems.values().length];
                prescriptionData[AlarmReceiver.NotificationItems.alarm_id.ordinal()] = id;
                prescriptionData[AlarmReceiver.NotificationItems.alarm_enddate.ordinal()] = enddate;
                if(mCurrentPrescriptionData != null){
                    String imgPath = mCurrentPrescriptionData[PrescriptionEditItems.prescription_image_url.ordinal()];
                    prescriptionData[AlarmReceiver.NotificationItems.prescription_image_url.ordinal()] = imgPath;
                    String name = mCurrentPrescriptionData[PrescriptionEditItems.prescription_name.ordinal()];
                    prescriptionData[AlarmReceiver.NotificationItems.prescription_name.ordinal()] = name;
                    String unit = mCurrentPrescriptionData[PrescriptionEditItems.prescription_unit.ordinal()];
                    prescriptionData[AlarmReceiver.NotificationItems.prescription_unit.ordinal()] = unit;
                    String dosage = mCurrentPrescriptionData[PrescriptionEditItems.prescription_dosage.ordinal()];
                    prescriptionData[AlarmReceiver.NotificationItems.prescription_dosage.ordinal()] = dosage;

                }
                //check if alarm already exist
                //Set up the Notification Broadcast Intent
                Intent notifyIntent = new Intent(this, AlarmReceiver.class);
                // if alarm hasn't been set, set the alarm, otherwise update alarm if necessary
                notifyIntent.putExtra(Intent.EXTRA_TEXT, prescriptionData);
                final PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                        (this, alarmId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);



                // setup a calendar with the alarm starting date and time
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(alarmStartdate);
                calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
                calendar.set(Calendar.MINUTE, alarmMinute);
                //set repeating alarm
                mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), alarmRepeatms, notifyPendingIntent);
            }
        }




        //set default start

    }
    private void createDatePickerDialogForEditText(final EditText edittext){

        // get calendar and today's date
        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateforEdittext(edittext, calendar.getTime());
            }
        };
        edittext.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                int currYear = calendar.get(Calendar.YEAR);
                int currMon = calendar.get(Calendar.MONTH);
                int currDay = calendar.get(Calendar.DAY_OF_MONTH);
                // try parse edittext date
                String datestring = ((EditText)v).getText().toString();
                if(!datestring.isEmpty()) {
                    String dateFormat = "MM-dd-yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
                    Date date = null;
                    try {
                        date = sdf.parse(datestring);
                    } catch (ParseException e) {
                        Timber.e("Date Parse error: " + e.getMessage());
                    }
                    if(date != null){
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(date);
                        currYear = dateCalendar.get(Calendar.YEAR);
                        currMon = dateCalendar.get(Calendar.MONTH);
                        currDay = dateCalendar.get(Calendar.DAY_OF_MONTH);
                    }
                }

                new DatePickerDialog(PrescriptionEditActivity.this, startDate, currYear, currMon, currDay).show();
            }
        });
    }


    private void createTimePickerDialogForTextView(final TextView textview){

        textview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int hour = 0;
                int minute = 0;

                // create a default time from current time
                Calendar mcurrentTime = Calendar.getInstance();
                hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                minute = mcurrentTime.get(Calendar.MINUTE);

                //try parse textview content
                String timetext = textview.getText().toString();
                if(timetext != null && !timetext.isEmpty()){
                    String[] timecomp = timetext.split(":");
                    if(timecomp != null && timecomp.length == 2){
                        hour = Integer.parseInt(timecomp[0]);
                        minute = Integer.parseInt(timecomp[1]);
                    }
                }

                //create calendar

                TimePickerDialog mTimePicker;
                //create time picker dialog
                mTimePicker = new TimePickerDialog(PrescriptionEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //format selected hour and minute
                        String hour =  String.format("%02d", selectedHour);
                        String minute = String.format("%02d", selectedMinute);
                        textview.setText( hour + ":" + minute);
                    }
                }, hour, minute, true);//24 hour time
                mTimePicker.setTitle(getResources().getString(R.string.spinner_title));
                mTimePicker.show();
            }
        });
    }
    //endregion


    //region View Setup Methods
    private ArrayAdapter createRepeatSpinnerArrayAdapter() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.repeat_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
    private AdapterView.OnItemSelectedListener createSpinnerAdapterViewOnItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mRepeatUserInput = (String) mSpinnerAdapter.getItem(position);
                updateRemindersInstructionText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }
    private TextWatcher createOnTextChangedListener(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateRemindersInstructionText();
            }
        };
    }
    private View.OnClickListener createImageOnClickListener()
    {
        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        };
    }
    private CompoundButton.OnCheckedChangeListener createRemindersCheckboxOnCheckedChangedListener()
    {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    hideRemindersViews();
                }else{
                    if(mNumOfAlarms > 0){
                        // shows reminder section and creates default reminders
                        createAndShowReminderViews();
                    }else{
                        getUserInputFromPrescriptionViews();
                        SavePrescriptionUserInputToDatabase();
                        // shows reminder section and creates default reminders
                        createAndShowReminderViews();
                        getUserInputFromReminderViews();
                        SaveRemindersUserInputToDatabase();
                    }
                }
            }
        };
    }
    //endregion


}

