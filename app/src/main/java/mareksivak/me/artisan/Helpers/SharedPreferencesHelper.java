package mareksivak.me.artisan.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by mareksivak on 05/11/2017.
 */

public class SharedPreferencesHelper {

    private static String LOG_TAG = "[StorageHelper]";

    private static SharedPreferences sharedPreferences;

    private static final String ARTISAN_PREFERENCES = "artisanPreferences";
    private static final String DEMO_NOTES_CREATED = "demoNotesCreated";

    public static void createSharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(ARTISAN_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean getDemoNotesCreated() {
        boolean notesCreated = sharedPreferences.getBoolean(DEMO_NOTES_CREATED, false);
        GlobalVariables.DEMO_NOTES_CREATED = notesCreated;
        return notesCreated;
    }

    public static void saveDemoNotesCreated(boolean notesCreated) {
        GlobalVariables.DEMO_NOTES_CREATED = notesCreated;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DEMO_NOTES_CREATED, notesCreated).commit();
    }
}
