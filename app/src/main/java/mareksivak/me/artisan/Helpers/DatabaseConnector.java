package mareksivak.me.artisan.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by mareksivak on 06/11/2017.
 */

//http://www.androidbegin.com/tutorial/android-sqlite-database-tutorial/

public class DatabaseConnector {

    public static final int DATABASE_VERSION = 1; //To reset or wipe the database, just change the version number.
    public static final String DATABASE_NAME = "NotesDB";
    public static final String TABLE_NAME = "Notes";
    public static final String KEY_ID = "id";
    public static final String KEY_POSITION = "position";
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_STATUS = "status";
    public static final String KEY_MODIFIED = "modified";
    public static final String KEY_CREATED = "created";
    private static final String[] COLUMNS = { KEY_ID, KEY_POSITION, KEY_TITLE, KEY_BODY, KEY_STATUS,
            KEY_MODIFIED, KEY_CREATED };

    private SQLiteDatabase db;
    private SQLDatabaseHelper dbOpenHelper;


    public DatabaseConnector(Context context) {
        dbOpenHelper = new SQLDatabaseHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);

    }

    // Open Database function
    public void open() throws SQLException {
        // Allow database to be in writable mode
        db = dbOpenHelper.getWritableDatabase();
    }

    // Close Database function
    public void close() {
        if (db != null)
            db.close();
    }

    public void deleteOne(Note note) {
        this.open();
        db.delete(TABLE_NAME, "id = ?", new String[] { String.valueOf(note.getId()) });
        this.close();
    }

    public Note getNote(int id) {

        Cursor cursor = db.query(TABLE_NAME, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        Note note = new Note();
        note.setId(cursor.getString(0));
        note.setPosition(Integer.parseInt(cursor.getString(1)));
        note.setTitle(cursor.getString(2));
        note.setBody(cursor.getString(3));
        note.setStatus(Integer.parseInt(cursor.getString(4)));
        note.setModified(cursor.getString(5));
        note.setCreated(cursor.getString(6));

        return note;
    }

    public ArrayList<Note> allNotes() {

        ArrayList<Note> noteList = new ArrayList<Note>();
        String query = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY_POSITION;


        Cursor cursor = db.rawQuery(query, null);
        Note note = null;

        if (cursor.moveToFirst()) {
            do {
                note = new Note();
                note.setId(cursor.getString(0));
                note.setPosition(Integer.parseInt(cursor.getString(1)));
                note.setTitle(cursor.getString(2));
                note.setBody(cursor.getString(3));
                note.setStatus(Integer.parseInt(cursor.getString(4)));
                note.setModified(cursor.getString(5));
                note.setCreated(cursor.getString(6));
                noteList.add(note);
            } while (cursor.moveToNext());
        }

        return noteList;
    }

    // Create new note
    public void addNote(Note note) {


        ContentValues values = new ContentValues();
        values.put(KEY_ID, note.getId());
        values.put(KEY_POSITION, note.getPosition());
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_BODY, note.getBody());
        values.put(KEY_STATUS, note.getStatus());
        values.put(KEY_MODIFIED, note.getModified());
        values.put(KEY_CREATED, note.getCreated());

        // insert
        this.open();
        db.insert(TABLE_NAME,null, values);
        this.close();
    }

    // Update note
    public int updateNote(Note note) {


        ContentValues values = new ContentValues();
        values.put(KEY_ID, note.getId());
        values.put(KEY_POSITION, note.getPosition());
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_BODY, note.getBody());
        values.put(KEY_STATUS, note.getStatus());
        values.put(KEY_MODIFIED, note.getModified());

        this.open();
        int i = db.update(TABLE_NAME, // table
                values, // column/value
                "id = ?", // selections
                new String[] { String.valueOf(note.getId()) });

        this.close();

        return i;
    }

    // Update note index/position
    public int updateIndex(Note note) {


        ContentValues values = new ContentValues();
        values.put(KEY_ID, note.getId());
        values.put(KEY_POSITION, note.getPosition());

        this.open();
        int i = db.update(TABLE_NAME, // table
                values, // column/value
                "id = ?", // selections
                new String[] { String.valueOf(note.getId()) });

        this.close();

        return i;
    }
}
