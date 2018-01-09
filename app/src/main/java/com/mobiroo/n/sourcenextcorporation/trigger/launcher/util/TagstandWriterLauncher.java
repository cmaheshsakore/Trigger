package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

public class TagstandWriterLauncher {
    private static final String WRITER_PACKAGE = "com.trigger.writer";

    public static void launch(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent writerIntent = pm.getLaunchIntentForPackage(WRITER_PACKAGE);
        if (writerIntent != null) {
            writerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(writerIntent);
        } else {
            launchMarket(context);
        }
    }

    private static void launchMarket(Context context) {
        Uri uri = Uri.parse("market://details?id=" + WRITER_PACKAGE);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(context, "Failed to launch Play Store.", Toast.LENGTH_SHORT).show();
        }
    }
}
