package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ContactQuery {

    public static ArrayList<String> getContactDataFor(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String dataType) {
        ArrayList<String> data = new ArrayList<String>();

        try {
            Cursor c = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

            if (c != null) {
                if (c.moveToFirst()) {

                    do {
                        data.add(c.getString(c.getColumnIndex(dataType)));
                    } while (c.moveToNext());
                }
                c.close();
            }
        } catch (Exception e) {
            Logger.d("Exception querying contacts list " + e);
        }

        return data;
    }
}
