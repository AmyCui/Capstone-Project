package com.amycui.medsminder.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.amycui.medsminder.R;
import com.amycui.medsminder.data.PrescriptionContract;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //region Constants
    // Request code value for firebase auth user sign in intent
    private static final int RC_SIGN_IN = 1;
    // Anonymous username
    public static final String ANONYMOUS = "anonymous";
    // Loader id for prescription entry data loader
    private static final int PRESCRIPTION_LOADER = 10;
    //endregion

    //region Fields
    // Firebase auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    // User information
    private String mUsername;
    private static String mUserUid;
    private String mUserEmail;
    private static long mUserKey;
    // UI Views
    private Toolbar mToolbar;
    private GridView mPrescriptionGrid;
    private FloatingActionButton mFabButton;

    private PrescriptionsGridAdapter mPrescriptionGridAdapter;
    //endregion

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set content view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // add toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // setup gridview and adapter
        mPrescriptionGrid = (GridView)findViewById(R.id.grid_view);
        mPrescriptionGridAdapter = new PrescriptionsGridAdapter(this, this, R.layout.grid_item_card, new ArrayList());
        mPrescriptionGrid.setAdapter(mPrescriptionGridAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mPrescriptionGrid.setNestedScrollingEnabled(true);
        // setup fab button
        mFabButton = (FloatingActionButton)findViewById(R.id.add_new_fab);
        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent to launch presceiptionEditActivity
                Intent intent = new Intent(getApplicationContext(), PrescriptionEditActivity.class);
                startActivity(intent);
            }
        });
        // setup firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = createNewAuthStateListener();
    }
    //endregion

    //region Activity Methods

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(requestCode == RESULT_OK){
                //signed in success. do nothing here
                //signed in initialization is handled in auth state listener
            }else if(requestCode == RESULT_CANCELED){
                //sign in fail or user press back button
                finish();
            }
        }
    }
    //endregion

    //region Auth methods

    private FirebaseAuth.AuthStateListener createNewAuthStateListener(){
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    // user is signed in
                    onSignedInInitialize(user.getDisplayName(), user.getUid(), user.getEmail());
                }
                else
                {
                    //user is signed out, start sign in flow
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.AppTheme)
                                    .setLogo(R.mipmap.ic_launcher)
                                    //auto sign-in
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void onSignedInInitialize(String username, String uid, String email) {
        if(!uid.isEmpty()) {
            if(username != null && !username.isEmpty())
                mUsername = username;
            else
                mUsername = ANONYMOUS;
            mUserUid = uid;
            mUserEmail = email;
            addNewUserToUserEntry();
            getSupportLoaderManager().initLoader(PRESCRIPTION_LOADER,null,this);
        }
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
    }
    //endregion

    //region CursorLoader Methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // load prescription data for current user
        if(id == PRESCRIPTION_LOADER)
            return new CursorLoader(this, PrescriptionContract.PrescriptionEntry.buildUserPrescriptionUri(mUserUid),null,null,null,null);
        else {
            Timber.e("Unknown loader id: " + id);
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == PRESCRIPTION_LOADER){
            // Load prescription cursor data into string list
            List<String[]> prescriptions =  getPrescriptionsForCurrentUser(data);
            // load string list data to grid adapter to update view
            setPrescriptionGridAdapter(prescriptions);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    //endregion

    //region Database methods

    /**
     * Check if current user already exists in User database table
     * @param uid unique firebase user id for user
     * @return true for user already exist
     */
    private boolean userAlreadyExist(String uid){
        Cursor result = this.getContentResolver().query(
                PrescriptionContract.UserEntry.CONTENT_URI,
                new String[]{PrescriptionContract.UserEntry._ID},
                PrescriptionContract.UserEntry.COLUMN_UID + " = ?",
                new String[]{uid},
                null
        );
        if(result != null && result.moveToFirst()) {
            mUserKey = result.getLong(0);
            return true;
        }
        return false;
    }

    /**
     * Adds a new user row to User database table with mUsername/mUserUid/mUserEmail.
     * These value should already be assigned at sign in
     */
    private void addNewUserToUserEntry(){
        if(!userAlreadyExist(mUserUid)){
            ContentValues contentValues = new ContentValues();
            contentValues.put(PrescriptionContract.UserEntry.COLUMN_USERNAME, mUsername);
            contentValues.put(PrescriptionContract.UserEntry.COLUMN_UID, mUserUid);
            contentValues.put(PrescriptionContract.UserEntry.COLUMN_EMAIL, mUserEmail);

            Uri row = getContentResolver().insert(PrescriptionContract.UserEntry.CONTENT_URI, contentValues);
            mUserKey = ContentUris.parseId(row);
        }
    }
    //endregion

    //region private methods

    /**
     * This function read list of prescription data for current user from Cursor
     * and loads the data into a string array list
     * @param cursor  The Cursor returned from CursorLoader OnLoadFinish
     * @return String array List representation of PrescriptionGridItems values for all prescriptions
     */
    private List<String[]> getPrescriptionsForCurrentUser(Cursor cursor){
        List<String[]> result = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do{
                String[] prescription = new String[PrescriptionsGridAdapter.PrescriptionGridItems.values().length];

                prescription[PrescriptionsGridAdapter.PrescriptionGridItems.prescription_id.ordinal()] =
                        cursor.getString(0);

                prescription[PrescriptionsGridAdapter.PrescriptionGridItems.prescription_image_url.ordinal()] =
                        cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_IMAGE_URL));

                prescription[PrescriptionsGridAdapter.PrescriptionGridItems.prescription_name.ordinal()] =
                        cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_NAME));

                prescription[PrescriptionsGridAdapter.PrescriptionGridItems.prescription_date.ordinal()] =
                        cursor.getString(cursor.getColumnIndex(PrescriptionContract.PrescriptionEntry.COLUMN_DATE));

                result.add(prescription);
            }while(cursor.moveToNext());
        }
        return result;
    }

    private void setPrescriptionGridAdapter(List<String[]> prescriptions){
        // creates an empty string array list if the data passed in is null
        if(prescriptions == null) {
            prescriptions = new ArrayList<String[]>();
        }
        // reset adapter data
        mPrescriptionGridAdapter.clear();
        // add all data
        mPrescriptionGridAdapter.addAll(prescriptions);
    }
    //endregion

    //region public methods

    /**
     * @return the unique firebaes user id for current firebae auth user
     */
    public static String getCurrentUserId(){
        return mUserUid;
    }

    /**
     * @return the unique User Database Entry id for current user
     */
    public static long getCurrentUserKey(){
        return mUserKey;
    }
    //endregion
}

