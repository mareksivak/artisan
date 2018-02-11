package mareksivak.me.artisan.Helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mareksivak on 17/10/2017.
 */

public class SQLDatabaseHelper extends SQLiteOpenHelper {


    //to help manage database creation and version management
    public SQLDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                             int version) {
        super(context, DatabaseConnector.DATABASE_NAME, null, DatabaseConnector.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE " + DatabaseConnector.TABLE_NAME + " ( "
                + DatabaseConnector.KEY_ID + " TEXT, " + DatabaseConnector.KEY_POSITION + " TEXT, "
                + DatabaseConnector.KEY_TITLE + " TEXT, " + DatabaseConnector.KEY_BODY + " TEXT, "
                + DatabaseConnector.KEY_STATUS + " INTEGER, " + DatabaseConnector.KEY_MODIFIED + " TEXT, "
                + DatabaseConnector.KEY_CREATED + " TEXT )";
//id INTEGER PRIMARY KEY AUTOINCREMENT - for autoincrement

        db.execSQL(CREATION_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConnector.TABLE_NAME);
        this.onCreate(db);
    }



}
