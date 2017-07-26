package com.amycui.medsminder.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PrescriptionEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //region Constants
    private static  final int PICK_IMAGE = 300;
    // Prescription edit cursor loader
    private static final int PRESCRIPTION_EDIT_LOADER = 101;
    // Reminders cursor loader
    private static final int REMINDERS_LOADER = 201;
    // The custom defined broadcast receiver intent for passing in string array to the alarmreceiver
    private static final String ALARM_STRING_ARRAY_ACTION = "com.amycui.medsminder.ACTION_STRING_ARRAY";
    //endregion

    //region Data Fields
    private Toolbar mToolbar;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;

    private String mCurrentPrescriptionId;
    private String[] mCurrentPrescriptionData;
    private String mPresImgPathUserInput;
    private String mDrugNameUserInput;
    private String mDrugUnitUserInput;
    private String mPresDateUserInput;
    private String mDosageUserInput;
    private String mFrequencyUserInput;
    private String mRepeatUserInput;
    private ArrayList<String[]> mCurrentRemindersData;
    private ArrayList<String> mCurrentReminderIds;
    private String mReminderStartDateUserInput;
    private String mReminderEndDateUserInput;
    private ArrayList<String> mReminderTriggerTimeUserInput;
    private int mNumOfAlarms = 0;

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
    public static enum PrescriptionEditItems{
        prescription_image_url,
        prescription_name,
        prescription_date,
        prescription_unit,
        prescription_dosage,
        prescription_frequency,
        prescription_repeat,
    }

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
        // Find views in included layouts

        // Set up toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar_edit_activity);
        setSupportActionBar(mToolbar);
        // Enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Set up repeat spinner
        mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.repeat_spinner, android.R.layout.simple_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRepeatSpinner.setAdapter(mSpinnerAdapter);
        mRepeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mRepeatUserInput = (String) mSpinnerAdapter.getItem(position);
                updateRemindersInstructionText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // listen to Rx instruction section changes
        mDosage.addTextChangedListener(new TextWatcher() {
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
        });
        mFrequency.addTextChangedListener(new TextWatcher() {
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
        });

        // Set up prescription image click to show gallery
        mPresImageCard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        // Set up prescription date DatePicker
        createDatePickerDialogForEditText(mPresDate);
        // Set up reminder section
        // add on checked changed listener to hide/show section
        mRemindersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    hideRemindersViews();
                }else{
                    // shows reminder section and creates default reminders
                    createAndShowReminderViews();
                }
            }
        });
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
        if(id == PRESCRIPTION_EDIT_LOADER){
            return new CursorLoader(this, PrescriptionContract.PrescriptionEntry.CONTENT_URI,
                    null,
                    PrescriptionContract.PrescriptionEntry._ID + " = ?",
                    new String[]{mCurrentPrescriptionId},
                    null);
        }
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
            if(mCurrentPrescriptionData != null){
                //update data to view
                String imgPath = mCurrentPrescriptionData[PrescriptionEditItems.prescription_image_url.ordinal()];
                updateDrugImage(imgPath);

                String name = mCurrentPrescriptionData[PrescriptionEditItems.prescription_name.ordinal()];
                updateDrugName(name);

                String presDate = mCurrentPrescriptionData[PrescriptionEditItems.prescription_date.ordinal()];
                updatePresDate(presDate);

                String unit = mCurrentPrescriptionData[PrescriptionEditItems.prescription_unit.ordinal()];
                updateDrugUnit(unit);

                String dosage = mCurrentPrescriptionData[PrescriptionEditItems.prescription_dosage.ordinal()];
                updateDosage(dosage);

                String frequency = mCurrentPrescriptionData[PrescriptionEditItems.prescription_frequency.ordinal()];
                updateFrequency(frequency);

                String repeat = mCurrentPrescriptionData[PrescriptionEditItems.prescription_repeat.ordinal()];
                //TODO: repeat update
            }
        }
        // If callback triggered by reminder loader
        else if(loader.getId()==REMINDERS_LOADER && data != null){
            mCurrentRemindersData = getCurrentRemindersData(data);
            if(mCurrentRemindersData != null && mCurrentRemindersData.size()>0){
                //update start date and end date. they are the same for all alarms
                String startdate = mCurrentRemindersData.get(0)[PrescriptionReminderItems.reminder_start_date.ordinal()];
                updateStartDate(startdate);
                String enddate = mCurrentRemindersData.get(0)[PrescriptionReminderItems.reminder_end_date.ordinal()];
                updateEndDate(enddate);
                //iterate to update alarm data
                ArrayList<String> timeList = new ArrayList<String>();
                mCurrentReminderIds = new ArrayList<String>();
                for(int i=0 ;i<mCurrentRemindersData.size();i++){
                    String[] alarm = mCurrentRemindersData.get(i);
                    String time = alarm[PrescriptionReminderItems.reminder_trigger_time.ordinal()];
                    timeList.add(time);
                    String id = alarm[PrescriptionReminderItems.reminder_id.ordinal()];
                    mCurrentReminderIds.add(id);

                }
                updateAlarmTimeText(timeList);
            }
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
            //TODO: start/end date, reminder
        }
        return result;
    }

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
        newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_USER_KEY, MainActivity.getCurrentUserKey());
        if(mPresImgPathUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_IMAGE_URL, mPresImgPathUserInput);
        if(mDrugNameUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_NAME, mDrugNameUserInput);
        if(mDrugUnitUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_UNIT, mDrugUnitUserInput);
        if(mDosageUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_DOSAGE, mDosageUserInput);
        if(mPresDateUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_DATE, mPresDateUserInput);
        if(mRepeatUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_REPEAT_UNIT, mRepeatUserInput);
        if(mDrugUnitUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_UNIT, mDrugUnitUserInput);
        if(mFrequencyUserInput != null)
            newdata.put(PrescriptionContract.PrescriptionEntry.COLUMN_FREQUENCY, mFrequencyUserInput);

        if(mCurrentPrescriptionId != null){
            getContentResolver().update(
                    PrescriptionContract.PrescriptionEntry.CONTENT_URI,
                    newdata,
                    PrescriptionContract.RemindersEntry._ID + " = ?",
                    new String[]{mCurrentPrescriptionId});
        }
        else
        {
            //create new entry
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
                     getContentResolver().insert(
                             PrescriptionContract.RemindersEntry.CONTENT_URI,
                             newdata
                     );
                }

            }
        }

    }


    private void getUserInputFromViews(){
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

    private void updateDateforEdittext(EditText edittext, Date date){
        String dateFormat = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        edittext.setText(sdf.format(date));
    }

    private void hideRemindersViews(){
       mAlarmLinearLayout.setVisibility(View.INVISIBLE);
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

    private void createDefaultAlarms(){
        //TODO: create alarms
        // an alarm id should have been created by the time this method is called.
        // but check for this anyways. if not created, skip creating alarms


        //create notification manager and alarm manager
        if(mNotificationManager == null)
            mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(mAlarmManager == null)
            mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

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
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
    }
    //endregion
}

