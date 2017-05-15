package com.amycui.medsminder.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/*
 This contract contains two database: Users and Prescriptions:
 **** User database:
 *  Col 1: UID: the unique user id associated with Firebase auth user account
 *  Col 2: username: the display name user has entered
 *  Col 3: email: user email address for the account

 **** Prescription database:
 *  Col 1: name: name of the prescription. e.g.: aspirin
 *  Col 2: unit: unit of the prescription. e.g.: tablet, capsule.
 *  Col 3: date: date of the prescription.
 *  Col 4: imageUrl: url of the image file associated with this prescription
 *  Col 5: dosage: amount of unit of prescription requred for each use
 *  Col 6: frequency: how many times to repeat in a time unit. e.g.: 2 for twice a day
 *  Col 7: repeat_unit: the unit of time to repeat dosage. e.g.: day for twice a day
 *  Col 8: userKey: Column with the foreign key into the user table.
 */
public class PrescriptionContract {
    // Name for the content provider
    public static final String CONTENT_AUTHORITY = "com.amycui.medsminder.app";
    // Base of all URIs that app will be using to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Path for looking at prescription data
    public static final String PATH_PRESCRIPTION = "prescription";
    // Path for looking at user data
    public static final String PATH_USER = "user";

    /* Inner class that defines the table contents of the user table */
    public static final class UserEntry implements BaseColumns{

        // The Uri used to look for data in user table
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();
        // This is the Android platform's base MIME type for a content: URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        // This is the Android platform's base MIME type for a content: URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        // Table name
        public static final String TABLE_NAME = "user";

        // The unique user id associated with Firebase auth user account
        public static final String COLUMN_UID = "uid";

        // The display name user has entered
        public static final String COLUMN_USERNAME = "username";

        // The user email address for the account
        public static final String COLUMN_EMAIL = "email";

        // The uri for a specific user data using the android content provider base column id
        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /* Inner class that defines the table contents of the prescription table */
    public static final class PrescriptionEntry implements BaseColumns{

        // The Uri used to look for data in prescription table
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRESCRIPTION).build();
        // This is the Android platform's base MIME type for a content: URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRESCRIPTION;
        // This is the Android platform's base MIME type for a content: URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRESCRIPTION;

        // Table name
        public static final String TABLE_NAME = "prescription";

        // The name of the prescription. e.g.: aspirin
        public static final String COLUMN_NAME = "name";

        // The unit of the prescription. e.g.: tablet, capsule, .etc.
        public static final String COLUMN_UNIT = "unit";

        // The date of the prescription in unit of milliseconds offset from the EPOCH time
        public static final String COLUMN_DATE = "date";

        // The url of the image file associated with this prescription
        public static final String COLUMN_IMAGE_URL = "image_url";

        // The amount of unit of prescription requred for each use
        public static final String COLUMN_DOSAGE = "dosage";

        // How many times to repeat in a time unit. e.g.: 2 for twice a day
        public static final String COLUMN_FREQUENCY = "frequency";

        // The unit of time to repeat dosage. e.g.: day for twice a day
        public static final String COLUMN_REPEAT_UNIT = "repeat_unit";

        // Column with the foreign key into the user table.
        public static final String COLUMN_USER_KEY = "user_key";

        // The uri for a specific prescription data using the android content provider base column id
        public static Uri buildPrescriptionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // The uri for the prescription data for a specific user using firebase unique id
        public static Uri buildUserPrescriptionUri(String uid) {
            return CONTENT_URI.buildUpon().appendPath(uid).build();
        }

        // Get the User _ID from passed in Uri
        public static String getUserFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getPrescriptionNameFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }
    }
}

