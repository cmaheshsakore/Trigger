package com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TagInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.TagTechnology;
import android.os.Build;

public class NFCUtil {

    static public boolean hasTech(Tag tag, String klassName) {
        for (String tech : tag.getTechList()) {
            if (tech.equals(klassName)) {
                return true;
            }
        }
        return false;
    }

    static public boolean hasTech(Tag tag, Class<? extends TagTechnology> tech) {
        return (tech == null || tag == null) ? false : hasTech(tag, tech.getCanonicalName());
    }


    /**
     * @param encodeInUtf8
     * @return NdefRecrd containing tags.to/ntl URL
     */
    private static NdefRecord makeMarketLink(boolean encodeInUtf8) {

        NdefRecord rtdUriRecord = null;

        byte[] uriField = "tags.to/ntl".getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1]; // add 1 for the URI Prefix
        payload[0] = 0x03; // prefixes http://www to the URI
        System.arraycopy(uriField, 0, payload, 1, uriField.length); // appends URI to payload
        rtdUriRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0],payload);

        return rtdUriRecord;

    }

    /**
     * Generates an NdefRecord array based on tag size preferring
     * 1.) Mime + AAR
     * 2.) URL + AAR
     * 3.) MIME Only (sub 48 bytes)
     * @param context
     * @param packageName
     * @param encodeInUtf8
     * @param maxSize Maximum capacity of tag.  Used to determine whether to use the AAR or not
     * @return Returns an array of NDEF records containing payload suitable for writing to that tag size
     */
    @TargetApi(14)
    public static NdefRecord[] createSmallRecord(Context context, String packageName, boolean encodeInUtf8, int maxSize) {

        byte[] data = { (byte)  0x00 };
        String mime = "x/nfctl-s";
        if (maxSize > 48) {
            if (Build.VERSION.SDK_INT >= 14) {
                NdefRecord[] ndr = {
                        //makeMarketLink(encodeInUtf8),
                        new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mime.getBytes(), new byte[0], data),
                        //makeMarketLink(encodeInUtf8)                        
                        NdefRecord.createApplicationRecord(packageName)
                };
                return ndr;
            }
            else {
                NdefRecord[] ndr = {
                        makeMarketLink(encodeInUtf8),
                        new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mime.getBytes(), new byte[0], data),
                        //makeMarketLink(encodeInUtf8)
                };
                return ndr;
            }
        } else {
            NdefRecord[] ndr = { new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mime.getBytes(), new byte[0], data)};
            return ndr;
        }


    }
    /**
     * @param context
     * @param data byte array of payload to be encoded
     * @param packageName
     * @param encodeInUtf8
     * @return NdefRecord suitable for writing to a tag.  Contains full payload
     */
    @TargetApi(14)
    public static NdefRecord[] createNdefRecord(Context context, byte[] data, String packageName, boolean encodeInUtf8) {
        String sMime = "ntl"; // Saves 11 bytes
        byte[] mimeType = sMime.getBytes();
        NdefRecord market = makeMarketLink(encodeInUtf8);
        // Check if we use an AAR override
        SettingsHelper.loadPreferences(context);
        if (SettingsHelper.useAARInMessage(context) && (Build.VERSION.SDK_INT >= 14)) {
            sMime = "x/nfctl";
            mimeType = sMime.getBytes();
            NdefRecord[] ndr = {

                    new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeType, new byte[0], data),
                    //makeMarketLink(encodeInUtf8)
                    NdefRecord.createApplicationRecord(packageName) 
            };
            return ndr;
        } else {
            NdefRecord[] ndr = { market,
                    new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeType, new byte[0], data) };
            return ndr;
        }
    }


    /**
     * Grab a tag payload based on saved id/name
     * @param Context
     * @param tagID local tag ID
     * @param tagName tag Name
     * @return Full payload suitable for adding to a message and parsing
     */
    public static String getTagPayload(Context context, String tagID, String tagName) {
        return getTagPayload(context, new String[] { tagID }, new String[] { tagName });
    }

    /**
     * Grab a tag payload based on saved ids/name
     * @param Context
     * @param mId (main and secondary)
     * @param tagName tag Name
     * @return Full payload suitable for adding to a message and parsing
     */
    public static String getTagPayload(Context context, String[] tagIds, String[] tagNames) {
        String payload = "";
        final int numIds = tagIds.length;
        String tagID = "";
        String tagName = "";


        for (int i=0; i< numIds; i++) {
            tagID = tagIds[i];
            tagName = tagNames[i];

            Logger.d("NFCUTIL: Loading Tag " + tagID + " = " + tagName);

            if ((i == 0) && (numIds == 1)) {
                payload += Constants.COMMAND_TAG_ID + ":" + tagID + ":" + tagName;
            } else {
                if (i==0) {
                    payload += Constants.COMMAND_TOGGLE_PROFILE + ":";
                } else {
                    payload += Constants.SWITCH_SEPARATOR;
                }

                payload += tagID + ":" + Constants.COMMAND_TAG_NAME + ":" + tagName;
            }

            // Now query all of the actions for this
            Cursor d = context.getContentResolver().query(TaskProvider.Contract.ACTIONS, new String[] { "Activity, Description" }, "TagId=?", new String[] { tagID }, null);
            if (d.moveToFirst()) {
                boolean keepReading = true;
                while (keepReading) {

                    if (!payload.equals("")) {
                        payload += ";";
                    }
                    payload += d.getString(0);
                    keepReading = d.moveToNext();
                }
            }
            d.close();
        }
        return payload;

    }

    /**
     * Loads a tag payload into an NdefRecord array suitable for sending to the parser
     * @param context
     * @param TagName Tag name to query
     * @param locale Current local (Locale.ENGLISH suggested)
     * @param encodeInUtf8
     * @param db Local readable database
     * @return NdefRecord containing encoded payload for this tag
     */
    public static NdefRecord[] loadTagFromName(Context context, String TagName, Locale locale,
            boolean encodeInUtf8, SQLiteDatabase db) {

        NdefRecord[] outRecord = new NdefRecord[0];

        Cursor c = db.query(DatabaseHelper.TABLE_SAVED_TASKS, new String[] { "ID" }, "Name=?", new String[] { TagName }, null, null, null);

        if ((c != null) && (c.moveToFirst())) {
            String tagID = c.getString(0);
            String payload = getTagPayload(context, tagID, TagName);

            // We have all of our data for this tag now create the NDEF record
            byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

            Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset
                    .forName("UTF-16");
            byte[] textBytes = payload.getBytes(utfEncoding);

            int utfBit = encodeInUtf8 ? 0 : (1 << 7);
            char status = (char) (utfBit + langBytes.length);

            byte[] data = new byte[1 + langBytes.length + textBytes.length];
            data[0] = (byte) status;
            System.arraycopy(langBytes, 0, data, 1, langBytes.length);
            System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

            outRecord = createNdefRecord(context, data, context.getPackageName(), encodeInUtf8);
        }

        c.close();
        return outRecord;
    }

    /**
     * Takes tag object and tries to determine tag type and size.
     * @param tag
     * @return TagInfo object with all determined info
     */
    public static TagInfo getTagInfo(Tag tag)
    {
        int maxSize = 0;
        String tagType = "Unknown";

        /* Determine which tag type we have */
        if (NFCUtil.hasTech(tag, MifareUltralight.class)) {
            MifareUltralight mu = MifareUltralight.get(tag);
            if ((mu.getType() == MifareUltralight.TYPE_ULTRALIGHT)) {
                // NTAG203 tags are gettign listed as Ultralight by
                // Gingerbread - try to read some pages after 0x0F to see if
                // we have a higher capacity "ultralight" tag.
                boolean readLatePages = false;
                try {
                    mu.connect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    if (mu.isConnected()) {
                        mu.readPages(0x10);
                        readLatePages = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    readLatePages = false;
                }

                try {
                    mu.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!readLatePages) {
                    maxSize = 46;
                    tagType = "Ultralight";
                } else {
                    maxSize = 137;
                    tagType = "NTAG203";
                }
            } else if ((mu.getType() == MifareUltralight.TYPE_ULTRALIGHT_C)) {
                maxSize = 137;
                tagType = "Ultralight C";
            } else if ((mu.getType() == MifareUltralight.TYPE_UNKNOWN)) {
                tagType = "Unknown";
                if (NFCUtil.hasTech(tag, MifareClassic.class)) {
                    maxSize = 716;
                    tagType = "Classic 1K";
                }
            }
        } else if (NFCUtil.hasTech(tag, MifareClassic.class)) {
            maxSize = 716;
            tagType = "Classic 1K";
        } else if (NFCUtil.hasTech(tag, NfcA.class) && NFCUtil.hasTech(tag, IsoDep.class)) {
            tagType = "Desfire";
        } else if (NFCUtil.hasTech(tag, NfcA.class)) {
            // On non NXP controllers this could be a Classic 1K tag
            NfcA nfca = NfcA.get(tag);
            tagType = "Topaz";
            if (nfca != null) {
                byte[] atqaBytes = nfca.getAtqa();
                StringBuilder hex = new StringBuilder();
                for (byte b: atqaBytes) {
                    hex.append(String.format("%02X", b));
                }
                String atqa = hex.toString();
                if (atqa.equals("0400")) {
                    maxSize = 716;
                    tagType = "Classic 1K";
                } else if (atqa.equals("000C")) {
                    tagType = "Topaz";
                }
            }

        }
        Logger.d("Tag is " + tagType);
        return new TagInfo(maxSize, tagType);
    }

    /**
     * Takes in N SavedTag objects and builds the payload suitable for saving or building an NdefRecord
     * @param context
     * @param includeName Whether or not to include the tag names in the payload
     * @param tasks Array of tasks to add to payload
     * @return Payload in string form
     */
    public static String buildSwitchPayload(Context context, boolean includeName, Task...tasks)
    {
        String payload = "";
        /* Currently building for only two messages */
        Task taskOne = tasks[0];
        Task taskTwo = tasks[1];

        // It's possible that TaskTwo does NOT have an ID yet
        if (taskTwo.getId() == null) {
            taskTwo.setId(String.valueOf(Task.generateSecondaryId(taskOne)));
        }
        
        String tagOnePayload = "";
        String tagTwoPayload = "";
        if (taskOne != null) {
            tagOnePayload = taskOne.buildPayloadString(includeName, true);
        }
        if (taskTwo!= null) {
            tagTwoPayload = taskTwo.buildPayloadString(includeName, true);
        }
        payload = Constants.COMMAND_TOGGLE_PROFILE + ":" + tagOnePayload + Constants.SWITCH_SEPARATOR + tagTwoPayload;

        Logger.d("Returning switch payload of " + payload);
        return payload;
    }

    /**
     * Returns an NdefRecord suitable for writing to a tag
     * @param context
     * @param packageName
     * @param payloadText Fully constructed payload
     * @param locale
     * @param encodeInUtf8
     * @return
     */
    public static NdefRecord[] buildNdefRecord(Context context, String packageName, String payloadText, Locale locale, boolean encodeInUtf8)
    {

        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payloadText.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length]; 
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return createNdefRecord(context, data, packageName, encodeInUtf8);
    }

    /**
     * Returns human readable UUID
     * @param tag
     * @return UUID as string
     */
    public static String getTagUuidAsString(Tag tag) {
        String tagId = "";
        byte[] tagIdBytes = tag.getId();
        if (tagIdBytes != null) {
            StringBuilder hex = new StringBuilder(tagIdBytes.length * 2);
            for (byte b: tagIdBytes) {
                hex.append(String.format("%02X", b));
            }
            tagId = hex.toString();
        } else {
            tagId = "";
        }

        return tagId;
    }

}