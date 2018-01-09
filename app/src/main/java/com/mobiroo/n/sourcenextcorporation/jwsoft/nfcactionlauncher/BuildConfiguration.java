package com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher;


import com.mobiroo.n.sourcenextcorporation.trigger.BuildWalletConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.FlavorInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

public class BuildConfiguration {

    public static String BUILD_PROFILE = FlavorInfo.BUILD_PROFILE;

    public static int VERSION_DIALOG = -1; //5;
     
    // Global wallet availablility (subject to build overrides)
    public static boolean BUILD_WITH_WALLET = true;
    // Global play store availability (subject to build overrides)
    public static boolean PLAY_STORE_AVAILABLE = true;
    
    // Only change if loading wallet on debug builds
    public static final int WALLET_BUILD_ENVIRONMENT = BuildWalletConstants.BUILD_ENVIRONMENT;
    
    public static final String FEEDBACK_EMAIL_ADDRESS = "help@coleridgeapps.com";
    public static final String ABOUT_TEXT = "© 2016 Coleridge Apps\nMore Info: http://www.gettrigger.com/";
     
    public static final boolean USE_GEOFENCES = true;
    
    public static boolean isPlayStoreAvailable() {
        if (BUILD_PROFILE.equals(Constants.BUILD_KOREA)
                || BUILD_PROFILE.equals(Constants.BUILD_NXP)) {
            return false;
        } else {
            return PLAY_STORE_AVAILABLE;
        }
    }
    
    public static boolean isWalletAvailable() {
        if (BUILD_PROFILE.equals(Constants.BUILD_KOREA)
                || BUILD_PROFILE.equals(Constants.BUILD_NXP)) {
            return false;
        } else {
            return BUILD_WITH_WALLET;
        }
    }
    
    public static int getWalletEnvironment() {
        return WALLET_BUILD_ENVIRONMENT;
    }
}
