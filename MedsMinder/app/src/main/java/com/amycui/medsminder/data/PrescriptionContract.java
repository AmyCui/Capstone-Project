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
+--------+--------+----------+-------------+
|    _id |    uid | username |   email     |
+========+========+==========+=============+
|      1 | 881256 |   apple  | apple@u.com |
|      2 | 413001 |   bpple  | bpple@u.com |
|      3 | 741532 |   cpple  | cpple@u.com |
|      4 | 496747 |   dpple  | dpple@u.com |

 **** Prescription database:
 *  Col 1: name: name of the prescription. e.g.: aspirin
 *  Col 2: unit: unit of the prescription. e.g.: tablet, capsule.
 *  Col 3: date: date of the prescription.
 *  Col 4: imageUrl: url of the image file associated with this prescription
 *  Col 5: dosage: amount of unit of prescription requred for each use
 *  Col 6: frequency: how many times to repeat in a time unit. e.g.: 2 for twice a day
 *  Col 7: repeat_unit: the unit of time to repeat dosage. e.g.: day for twice a day
 *  Col 8: userKey: Column with the foreign key into the user table.
+--------+------------+----------+-------------+---------------+--------+-----------+---------+--------+
|    _id |   name     |   unit   |   date      |    imageUrl   | dosage | frequency | reqpeat |  uid   |
+========+============+==========+=============+===============+========+===========+=========+========+
|      1 | aspirin    |  tablet  | 2017-04-21  |  aspirin.jpg  |   1    |     2     |   day   | 881256 |
|      2 | lipitor    |  tablet  | 2017-03-21  |  lipitor.jpg  |   2    |     3     |   day   | 881256 |
|      3 | aleve      |  tablet  | 2017-02-21  |   aleve.jpg   |   1    |     1     |   day   | 413001 |
|      4 | aspirin    |  tablet  | 2017-01-21  |  aspirin1.jpg |   1    |     2     |   day   | 496747 |


 **** Reminders database:
 *  Col 1: prescriptionID: the _ID of the prescription the reminder is set for
 *  Col 2: triggerTime: the HH:mm representation of the time to trigger alarm in a certain day
 *  Col 2: repeatMS: the time interval to repeat the alarm in milliseconds
 *  Col 3: startDate: the date to start this reminder
 *  Col 4: endDate: the date to end this reminder
 *
+--------+--------+--------------+-------------+-------------+-------------+
|    _id | presID | trigger_time |  repeat_ms  |  start_date |  end_date   |
+========+========+==============+=============+=============+=============+
|      1 |      1 |   08:00      |   86400000  |  2017-04-21 |  2017-04-27 |
|      2 |      1 |   18:00      |   86400000  |  2017-04-21 |  2017-04-27 |
|      3 |      2 |   08:00      |   86400000  |  2017-03-22 |  2017-04-22 |
|      4 |      2 |   12:00      |   86400000  |  2017-03-22 |  2017-04-22 |
|      5 |      2 |   18:00      |   86400000  |  2017-03-22 |  2017-04-22 |
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
    // Path for looking at reminder data
    public static final String PATH_REMINDER = "reminders";

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

        public static Uri buildUserUriWithUid(String uid){
            return CONTENT_URI.buildUpon().appendPath(uid).build();
        }

        // Get the UID from passed in Uri
        public static String getUIDFromUri(Uri uri){
            return uri.getPathSegments().get(1);
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

        public static Uri buildUserPrescriptionWithNameUri(String uid, String prescriptionName){
            return CONTENT_URI.buildUpon().appendPath(uid).appendQueryParameter(COLUMN_NAME, prescriptionName).build();
        }

        // Get the User _ID from passed in Uri
        public static String getUserFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getPrescriptionNameFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    /* Inner class that defines the table contents for the reminders database */
    public static final class RemindersEntry implements BaseColumns{
        // The Uri used to look for data in reminders table
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REMINDER).build();
        // This is the Android platform's base MIME type for a content: URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDER;
        // This is the Android platform's base MIME type for a content: URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REMINDER;

        // Table name
        public static final String TABLE_NAME = "reminders";

        // The prescription id the alarm/reminder is set for
        public static final String COLUMN_PRESCRIPTION_ID = "prescription_id";

        // The HH:mm representation of the time to trigger alarm in a certain day
        public static final String COLUMN_TRIGGER_TIME = "trigger_time";

        // The time interval to repeat the alarm in milliseconds
        public static final String COLUMN_REPEAT_MS = "repeat_ms";

        // The date to start this reminder
        public static final String COLUMN_START_DATE = "start_date";

        // The date to end this reminder
        public static final String COLUMN_END_DATE = "end_date";

        // The uri for a specific reminder data using the android content provider base column id
        public static Uri buildReminderUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildReminderUriFromPrescriptionId(String id){
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        // Get the prescription _ID from passed in Uri
        public static String getPrescriptionFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getIdfromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

    }
}

