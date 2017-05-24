package com.amycui.medsminder.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

    // Arbitrary request code value
    private static final int RC_SIGN_IN = 1;
    // Anonymous username
    public static final String ANONYMOUS = "anonymous";
    // Prescription
    private static final int PRESCRIPTION_LOADER = 10;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String mUsername;
    private String mUserUid;
    private String mUserEmail;


    private Toolbar mToolbar;
    private GridView mPrescriptionGrid;
    private PrescriptionsGridAdapter mPrescriptionGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mPrescriptionGrid = (GridView)findViewById(R.id.grid_view);
        mPrescriptionGridAdapter = new PrescriptionsGridAdapter(this, R.layout.grid_item_card, new ArrayList());
        mPrescriptionGrid.setAdapter(mPrescriptionGridAdapter);


        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
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
        //TODO: UI clean up. Data clean up.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(requestCode == RESULT_OK){
                //signed in
                Toast.makeText(this, "signed in", Toast.LENGTH_SHORT).show();
            }else if(requestCode == RESULT_CANCELED){
                Toast.makeText(this, "sign in cancelled!", Toast.LENGTH_SHORT).show();
                //sign in fail or user press back button
                finish();
            }
            //TODO: other result code such as photo picker
        }
    }

    private void onSignedInInitialize(String username, String uid, String email) {
        if(!uid.isEmpty()) {
            mUsername = username;
            mUserUid = uid;
            mUserEmail = email;
            addNewUserToUserEntry();
            //TODO: check for if loader already created
            getSupportLoaderManager().initLoader(PRESCRIPTION_LOADER,null,this).forceLoad();
        }
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        //TODO: UI clean up. Data clean up.
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
            List<String[]> prescriptions =  getPrescriptionsForCurrentUser(data);
            setPrescriptionGridAdapter(prescriptions);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //TODO: loader reset
    }


    private boolean userAlreadyExist(String uid){
        Cursor result = this.getContentResolver().query(
                PrescriptionContract.UserEntry.CONTENT_URI,
                new String[]{PrescriptionContract.UserEntry._ID},
                PrescriptionContract.UserEntry.COLUMN_UID + " = ?",
                new String[]{uid},
                null
        );
        if(result != null && result.moveToFirst())
            return true;
        return false;
    }

    private void addNewUserToUserEntry(){
        if(!userAlreadyExist(mUserUid)){
            ContentValues contentValues = new ContentValues();
            contentValues.put(PrescriptionContract.UserEntry.COLUMN_USERNAME, mUsername);
            contentValues.put(PrescriptionContract.UserEntry.COLUMN_UID, mUserUid);
            contentValues.put(PrescriptionContract.UserEntry.COLUMN_EMAIL, mUserEmail);

            Uri row = getContentResolver().insert(PrescriptionContract.UserEntry.CONTENT_URI, contentValues);
        }
    }

    private List<String[]> getPrescriptionsForCurrentUser(Cursor cursor){
        List<String[]> result = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()){
            do{
                String[] prescription = new String[3];
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
        String[] template = new String[PrescriptionsGridAdapter.PrescriptionGridItems.values().length];
        if(prescriptions == null) {
            prescriptions = new ArrayList<String[]>();
        }
        prescriptions.add(template);
        mPrescriptionGridAdapter.clear();
        mPrescriptionGridAdapter.addAll(prescriptions);
    }

}

