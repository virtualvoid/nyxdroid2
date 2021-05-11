package sk.virtualvoid.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.log4j.Logger;

import sk.virtualvoid.nyxdroid.library.Constants;

public class ConnectorFactory {
    private final static Logger log = Logger.getLogger(ConnectorFactory.class);


    public static IConnector getInstance(Context context) {
        return new OkHttpConnector(context);
    }

    public static boolean authorizationRequired(Context context) {
        if (context == null) {
            log.fatal("Connector/authorizationRequired got empty context !!!");

            throw new RuntimeException("context");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString(Constants.AUTH_TOKEN, null);
        boolean confirmed = prefs.getBoolean(Constants.AUTH_CONFIRMED, false);
        return !confirmed || token == null;
    }

    public static void authorizationRemove(Context context) {
        if (context == null) {
            log.fatal("Connector/authorizationRemove got empty context !!!");

            throw new RuntimeException("context");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor prefsEditable = prefs.edit();
        prefsEditable.remove(Constants.AUTH_NICK);
        prefsEditable.remove(Constants.AUTH_TOKEN);
        prefsEditable.remove(Constants.AUTH_CONFIRMED);
        prefsEditable.apply();
    }
}
