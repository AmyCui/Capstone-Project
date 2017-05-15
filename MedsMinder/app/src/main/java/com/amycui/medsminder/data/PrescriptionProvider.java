package com.amycui.medsminder.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

public class PrescriptionProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PrescriptionDbHelper mOpenHelper;
    // unique id for different available queries to the database
    private static final int USER = 100;
    private static final int PRESCRIPTION = 200;
    private static final int PRESCRIPTION_WITH_USER = 201;
    private static final int PRESCRIPTION_WITH_USER_AND_NAME = 202;

    // Build inner join between User and Prescription table using User._id column
    private static final SQLiteQueryBuilder sPrescriptionByUserQueryBuilder;

    static {
        sPrescriptionByUserQueryBuilder = new SQLiteQueryBuilder();
        sPrescriptionByUserQueryBuilder.setTables(
                PrescriptionContract.PrescriptionEntry.TABLE_NAME + " INNER JOIN " +
                        PrescriptionContract.UserEntry.TABLE_NAME +
                        " ON " + PrescriptionContract.PrescriptionEntry.TABLE_NAME +
                        "." + PrescriptionContract.PrescriptionEntry.COLUMN_USER_KEY +
                        " = " + PrescriptionContract.UserEntry.TABLE_NAME +
                        "." + PrescriptionContract.UserEntry._ID
        );
    }

    // Selection command for User.UID = ?
    private static final String sUserSelection =
            PrescriptionContract.UserEntry.TABLE_NAME +
                    "." + PrescriptionContract.UserEntry.COLUMN_UID + " = ? ";

    // Selection command for User.UID = ? and Prescription.name = ?
    private static final String sUserAndPrescriptionNameSelection =
            PrescriptionContract.UserEntry.TABLE_NAME +
                    "." + PrescriptionContract.UserEntry.COLUMN_UID + " = ? AND " +
                    PrescriptionContract.PrescriptionEntry.COLUMN_NAME + " = ? ";

    // Return cursor that points to prescription data with the queried user _Id
    private Cursor getPrescriptionByUser(Uri uri, String[] projection, String sortOrder){
        String user = PrescriptionContract.PrescriptionEntry.getUserFromUri(uri);

        return sPrescriptionByUserQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sUserSelection,
                new String[]{user},
                null,
                null,
                sortOrder);
    }

    // Return cursor that points to prescription data with the queried user _Id and prescription name
    private Cursor getPrescriptionByUserAndPrescriptionName(Uri uri, String[] projection, String sortOrder){
        String user = PrescriptionContract.PrescriptionEntry.getUserFromUri(uri);
        String prescriptionName = PrescriptionContract.PrescriptionEntry.getPrescriptionNameFromUri(uri);

        return sPrescriptionByUserQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sUserAndPrescriptionNameSelection,
                new String[]{user, prescriptionName},
                null,
                null,
                sortOrder);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PrescriptionDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor resultCursor;
        switch (sUriMatcher.match(uri)){
            case PRESCRIPTION_WITH_USER_AND_NAME:{
                resultCursor = getPrescriptionByUserAndPrescriptionName(uri, projection, sortOrder);
                break;
            }
            case PRESCRIPTION_WITH_USER:{
                resultCursor = getPrescriptionByUser(uri, projection,sortOrder);
                break;
            }
            case PRESCRIPTION:{
                resultCursor = mOpenHelper.getReadableDatabase().query(
                        PrescriptionContract.PrescriptionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case USER:{
                resultCursor = mOpenHelper.getReadableDatabase().query(
                        PrescriptionContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return resultCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRESCRIPTION_WITH_USER_AND_NAME:
                return PrescriptionContract.PrescriptionEntry.CONTENT_ITEM_TYPE;
            case PRESCRIPTION_WITH_USER:
                return PrescriptionContract.PrescriptionEntry.CONTENT_TYPE;
            case PRESCRIPTION:
                return PrescriptionContract.PrescriptionEntry.CONTENT_TYPE;
            case USER:
                return PrescriptionContract.UserEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri resultUri;

        switch (match){
            case PRESCRIPTION:{
                long _id = db.insert(PrescriptionContract.PrescriptionEntry.TABLE_NAME, null, contentValues);
                if(_id > 0)
                    resultUri = PrescriptionContract.PrescriptionEntry.buildPrescriptionUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case USER:{
                long _id = db.insert(PrescriptionContract.UserEntry.TABLE_NAME, null, contentValues);
                if(_id > 0)
                    resultUri = PrescriptionContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match){
            case PRESCRIPTION:{
                rowsDeleted = db.delete(PrescriptionContract.PrescriptionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case USER:{
                rowsDeleted = db.delete(PrescriptionContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if(rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PrescriptionContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PrescriptionContract.PATH_USER, USER);
        matcher.addURI(authority, PrescriptionContract.PATH_PRESCRIPTION, PRESCRIPTION);
        matcher.addURI(authority, PrescriptionContract.PATH_PRESCRIPTION + "/#", PRESCRIPTION_WITH_USER);
        matcher.addURI(authority, PrescriptionContract.PATH_PRESCRIPTION + "/#/*", PRESCRIPTION_WITH_USER_AND_NAME);

        return matcher;
    }

}
