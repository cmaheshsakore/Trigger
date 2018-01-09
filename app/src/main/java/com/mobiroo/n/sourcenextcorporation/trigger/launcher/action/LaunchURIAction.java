package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LaunchURIAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_LAUNCH_URL;
    }

    @Override
    public String getCode() {
        return Codes.LAUNCH_URL;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option031, null, false);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.urlText)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            if (arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO).equals("1")) {
                ((CheckBox) dialogView.findViewById(R.id.check_open_in_bg)).setChecked(true);
            }
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Launch URL";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText et = (EditText) actionView.findViewById(R.id.urlText);

        String url = et.getText().toString();
        String baseURL = url;
        url = Utils.encodeURL(url);
        url = Utils.encodeData(url);

        String openInBackground = "0";

        if (((CheckBox) actionView.findViewById(R.id.check_open_in_bg)).isChecked()) {
            openInBackground = "1";
        }

        return new String[] { Constants.COMMAND_LAUNCH_URL + ":" + url + ":" + openInBackground, context.getString(R.string.listLaunchText), baseURL };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listLaunchText);
        try {
            display += " " + Utils.decodeURL(args[0]);
        } catch (Exception e) {
            /* Ignore any exception here */
        }
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.decodeURL(Utils.tryParseEncodedString(args, 1, ""))),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, "0"))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String uri = Utils.tryParseString(args, 1, "");
        Logger.d("Launch URL " + uri);

        if (!uri.isEmpty()) {
            uri = Utils.decodeURL(uri);
            uri = Utils.decodeData(uri);

            String enArg2 = Utils.tryParseString(args, 2, "0");

            if (!uri.contains("://") && (!uri.contains(":"))) {
                uri = "http://" + uri;
            }

            uri = Utils.decodeData(uri);

            Logger.d(Constants.TAG, "URI is " + uri);
            if (openUrl(context, uri, enArg2)) {
                setupManualRestart(currentIndex + 1);
            }
        }
    }

    @SuppressLint("InlinedApi") 
    private boolean openUrl(Context context, String uri, String openInBg) {
        boolean needRest = false;

        // Ensure that the protocol is lower case
        String[] parts = uri.split("://");
        if (parts.length > 1) {
            parts[0] = parts[0].toLowerCase();
            uri = parts[0] + "://";
            for (int i=1; i < parts.length; i++) {
                uri += parts[i];
            }
        }


        if (!openInBg.equals("1")) {
            Logger.d("Opening " + uri);
            try {
                Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                b.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT > 10) {
                    b.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }

                if (uri.endsWith(".mp3")) { 
                    b.setDataAndType(Uri.parse(uri), "audio/mp3");
                } else if (uri.endsWith(".ogg")) {
                    b.setDataAndType(Uri.parse(uri), "application/ogg");
                } else if (uri.endsWith(".mp4")) {
                    b.setDataAndType(Uri.parse(uri), "video/mp4");
                } else if (uri.endsWith(".avi")) {
                    b.setDataAndType(Uri.parse(uri), "video/avi");
                } else if (uri.endsWith("html")) {
                    b.setDataAndType(Uri.parse(uri), "text/html");
                } else if (uri.endsWith("htm")) {
                    b.setDataAndType(Uri.parse(uri), "text/html");
                } else if (uri.endsWith(".jpg")) {
                    b.setDataAndType(Uri.parse(uri), "image/jpeg");
                } else if (uri.endsWith(".jpeg")) {
                    b.setDataAndType(Uri.parse(uri), "image/jpeg");
                } else if (uri.endsWith(".png")) {
                    b.setDataAndType(Uri.parse(uri), "image/png");
                }

                context.startActivity(b);
            } catch (Exception e) {
                Logger.e("Exception launching URL: " + e, e);
                e.printStackTrace();
            }

        } else {
            Logger.d("Opening " + uri + " in the background");
            // Open in the background
            new URLOpener().execute(uri);
        }

        return needRest;
    }

    private class URLOpener extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args) {
            String url = args[0];
            HttpClient httpclient = new DefaultHttpClient();

            String credentials = "";
            if (url.contains("@")) {
                // We likely have a username / password, check
                Pattern loginPattern = Pattern.compile("\\/\\/\\S+\\:\\S+\\@");
                Matcher m = loginPattern.matcher(url);
                while (m.find()) {
                    Logger.d("Found credentials " + m.group());
                    credentials = m.group();
                }
            }
            
            if (!credentials.isEmpty()) {
                // Credentials are //User:Pass@, get rid of extra chars
                credentials = credentials.replace("//", "");
                credentials = credentials.replace("@", "");

                /* Remove credentials from URL 
                 * left with http://@server:port/path */
                url = url.replace(credentials, "");

                /* Set up authentication */
                String server = "";
                String port = "80";
                

                /* Identify host:port if present */
                int host_start = url.indexOf("//@");
                int host_end = url.indexOf("/", host_start + 3);
                server = url.substring(host_start + 3, host_end);
                Logger.d("Host is " + server);
              
                if (server.contains(":")) {
                    /* Using a non standard port */
                    String[] data = server.split(":");
                    server = data[0];
                    port = ((data.length > 1) && ((data[1] != null) || !data[1].isEmpty())) ? data[1] : "80";
                }
                Logger.d("Using " + server + ":" + port + " to set up authentication");
                
                /* Break down the credentials so we can re-use them */
                String login[] = credentials.split(":");
                Credentials creds = new UsernamePasswordCredentials(login[0], login[1]);
                ((AbstractHttpClient) httpclient).getCredentialsProvider().setCredentials(new AuthScope(server, Integer.parseInt(port)), creds);
                
                url = url.replace("//@" + server, "//" + server);
                Logger.d("Final URI is " + url);

            }
            
            try {
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpclient.execute(httpget);
                int status = response.getStatusLine().getStatusCode();
                Logger.d("Response: " + status);
                if (status != 200) {
                    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outstream);
                    Logger.d("Response: " + new String(outstream.toByteArray()));
                }
            } catch (Exception e) { 
                Logger.e(Constants.TAG, "Exception thrown loading URL in background " + args[0], e);
            }

            return null;
        }

    }


    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetLaunchURL);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionLaunchURL);
    }

}
