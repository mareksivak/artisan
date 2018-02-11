package mareksivak.me.artisan.Helpers;

/**
 * Created by mareksivak on 8/24/17.
 */

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class Note implements Serializable {

    public static final int UNLOCKED = 0;
    public static final int LOCKED = 1;

    // describes stage of lifecycle of note - is open note just created or existed before?
    public static final int LIFECYCLE_CREATED = 1;
    public static final int LIFECYCLE_EDITED = 0;

    // attributes for passing data
    public static final String NOTE_KEY = "NOTE_TITLE";
    public static final String NOTE_POSITION = "NOTE_POSITION";
    public static final String NOTE_LIFECYCLE_STAGE = "NOTE_LIFECYCLE_STAGE";

    private String mId;
    private int mPosition;

    private String mTitle;
    private String mBody;


    private String mDateModified;
    private String mDateCreated;
    private int mStatus = UNLOCKED;

    public Note(JSONObject photoJSON) {
        try {
            mTitle = photoJSON.getString("title");

            //mDate = photoJSON.getString("date");
            mStatus = photoJSON.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Note() {

    }

    public Note(String title, String desc) {

        mTitle = title;

        mBody = desc;

    }

    public String getId() {
        return mId;
    }

    public void setId(String s) {
        mId = s;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setTitle(String s) {
        mTitle = s;
    }

    public String getTitle() {
        return mTitle;
    }


    public void setBody(String s) {
        mBody = s;
    }


    public String getBody() {
        return mBody;
    }

    public void setStatus (int status)
    {
        this.mStatus = status;
    }

    public String getModified() {
        return this.mDateModified;
    }

    public void setModified (String date)
    {
        this.mDateModified = date;
    }

    public String getCreated() {
        return this.mDateCreated;
    }

    public void setCreated (String date)
    {
        this.mDateCreated = date;
    }

    public int getStatus() {
        return this.mStatus;
    }


    public static String generateID()
    {
        String output;
        SimpleDateFormat idFormat=new SimpleDateFormat("yyyyMMddhhmmssSSS");
        Date currentTime = Calendar.getInstance().getTime();

        output = idFormat.format(currentTime);

        char[] chars = "0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        output += sb.toString();

        System.out.println(output);

        Log.d("[Note.generateID()]", "Generated String: " + output);
        return output;

    }
    public static String convertDateToString(Date date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(date);
    }

    private static void convertDateToHumanDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat humanDateFormat = new SimpleDateFormat("dd MMMM yyyy");

    }

    public static String printStatus(int status) {
        if (status == LOCKED) return "LOCKED";
        if (status == UNLOCKED) return "UNLOCKED";

        Log.e("[Note.PrintStatus()", "Unsupported input value");
        return "N/A";
    }

    public static String getCurrentTimestamp()
    {
        Date currentTime = Calendar.getInstance().getTime();
        return Note.convertDateToString(currentTime);
    }
}
