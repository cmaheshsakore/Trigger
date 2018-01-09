package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by krohnjw on 10/12/2015.
 */
public class EventConfiguration {

    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_NAME_MATCH = "name_match_type";
    public static final String KEY_DESCRIPTION_MATCH = "description_match_type";
    public static final String KEY_AVAILABILITY = "busy";
    public static final String KEY_ACCOUNT = "account";

    public static final int MATCH_TYPE_CONTAINS = 1;
    public static final int MATCH_TYPE_MATCHES = 2;

    public static final int ANY = 1;
    public static final int ATTENDING = 2;
    public static final int NOT_ATTENDING = 3;
    public static final int BUSY = 2;
    public static final int FREE = 3;

    public String name;
    public int name_match_type;
    public String description;
    public int description_match_type;
    public int availability;
    public String account;

    public EventConfiguration() {
        name = "";
        name_match_type = ANY;
        description = "";
        description_match_type = ANY;
        availability = ANY;
        account = "";
    }

    public void loadFromJson(JSONObject object) {

        name = object.optString(KEY_NAME, "");
        description = object.optString(KEY_DESCRIPTION, "");
        name_match_type = object.optInt(KEY_NAME_MATCH, ANY);
        description_match_type = object.optInt(KEY_DESCRIPTION_MATCH, ANY);
        availability = object.optInt(KEY_AVAILABILITY, ANY);
        account = object.optString(KEY_ACCOUNT, "");
    }

    public static EventConfiguration deserializeExtra(String extra) throws JSONException {
        EventConfiguration configuration = new EventConfiguration();
        configuration.loadFromJson(new JSONObject(extra));
        return configuration;
    }

}
