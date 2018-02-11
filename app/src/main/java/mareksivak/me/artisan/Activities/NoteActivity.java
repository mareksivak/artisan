package mareksivak.me.artisan.Activities;

/**
 * Created by mareksivak on 8/24/17.
 */

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import mareksivak.me.artisan.Constants.Constants;
import mareksivak.me.artisan.Helpers.DatabaseConnector;
import mareksivak.me.artisan.Helpers.GlobalVariables;
import mareksivak.me.artisan.Helpers.Note;
import mareksivak.me.artisan.Helpers.SQLDatabaseHelper;
import mareksivak.me.artisan.R;

// used to extend SwipeBackActivity for swipe gesture to finish
public class NoteActivity extends AppCompatActivity {

    private static String LOG_TAG = "LOG [NoteActivity]";

   // private TextView mTitleTextView;
   // private TextView mBodyTextView;



    private EditText mTitleEditText;
    private EditText mBodyEditText;

    private Button mBackButton;
    private Button mLockedButton;
    private Button mUnlockedButton;
    private Button mMoreButton; //!! aka delete

    private RelativeLayout mUnlockedMenu;
    private ImageButton mInsertHeaderButton;
    private ImageButton mInsertDateMenu;
    private ImageButton mInsertSpeech;

    private int noteLifecycle;

    private Note mOpenNote;

    Timer timer;

    private boolean isDeleted = false;


    private int notePosition = -1;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    public static final int REQ_CODE_NOTE_EXIT = 42;
    public static final String EXIT_KEY = "EXIT_KEY";
    public static final int NOTE_UPDATED = 20;
    public static final int NOTE_DELETED = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_note);
        //setDragEdge(SwipeBackLayout.DragEdge.LEFT); for swipe

        // HIDE ACTION BAR
        getSupportActionBar().hide();

        //mPhotoImageView = (ImageView) findViewById(R.id.photoImageView);
        mOpenNote = (Note) getIntent().getSerializableExtra(Note.NOTE_KEY);
        notePosition = (int) getIntent().getSerializableExtra(Note.NOTE_POSITION);
        noteLifecycle = (int) getIntent().getSerializableExtra(Note.NOTE_LIFECYCLE_STAGE);
        //Picasso.with(this).load(mSelectedPhoto.getUrl()).into(mPhotoImageView);

        Log.d("RecyclerView", "new noteactivity. Pos: " + notePosition);

        initUI();

        updateLock();

    }

    @Override
    public void onPause() {
        super.onPause();

        stopAutosave();

        // was note newly created and has no content?
        /*if (toDiscardNote()) {
            Log.d(LOG_TAG, "Discarding empty note");
            deleteNote();
        }
        else {
            if (!isDeleted) updateNote();
        }
*/
        if (!isDeleted) updateNote();
        Log.d(LOG_TAG, "Pausing activity");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume()");
        startAutosave();    // needs to start here
    }

    @Override
    public void onBackPressed() {
        onCloseNoteActivityEvent();
    }

    private void onCloseNoteActivityEvent()
    {
        Log.d(LOG_TAG, "Closing NoteActivity triggered by Back event");
        // close activity. this is new note without content - delete
        if (toDiscardNote()) {
            Log.d(LOG_TAG, "Discarding empty note");
            deleteNote();
            return;
        }

        // close activity. note will be saved
        finishNoteActivity(NOTE_UPDATED);
    }

    private void finishNoteActivity(int exitKey) {
        Intent intent = getIntent();
        intent.putExtra(EXIT_KEY, exitKey);
        setResult(RESULT_OK, intent);
        this.finish();
    }


    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        //Log.d(LOG_TAG, "FromHtml() input: " + html);
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }

        return result;
    }


    @SuppressWarnings("deprecation")
    public static String toHtml(Spannable source) {
        if (source == null) {
            return null;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.toHtml(source, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
        }

        return Html.toHtml(source);
    }

    private void initUI() {

        mTitleEditText = (EditText) findViewById(R.id.noteTitleEditText);
        mBodyEditText = (EditText) findViewById(R.id.noteBodyEditText);
        mBodyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                noteFocusChanged(hasFocus);
            }
        });

        mBackButton = (Button) findViewById(R.id.btnNoteBack);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onCloseNoteActivityEvent();
            }
        });

        mLockedButton = (Button) findViewById(R.id.btnNoteLocked);
        mLockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("NoteActivity", "Locked: registred click");
                onLockerClick();
            }
        });

        mUnlockedButton = (Button) findViewById(R.id.btnNoteUnlocked);
        mUnlockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("NoteActivity", "Unlocked: registred click");
                onLockerClick();
            }
        });

        mMoreButton = (Button) findViewById(R.id.btnNoteMore);
        mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Btn More clicked");
                deleteNoteClicked();
            }
        });

        mUnlockedMenu = (RelativeLayout) findViewById(R.id.barUnlockedMenu);

        mInsertHeaderButton = (ImageButton) findViewById(R.id.btnNoteInsertHeader);
        mInsertHeaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Insert Header");
                insertHeaderPressed();
            }
        });

        mInsertDateMenu = (ImageButton) findViewById(R.id.btnNoteInsertDate);
        mInsertDateMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Insert Date");
                insertDateToNote();
            }
        });

        mInsertSpeech = (ImageButton) findViewById(R.id.btnNoteSpeech);
        mInsertSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Speech");
                insertSpeechPressed();
            }
        });

        if (mTitleEditText != null) {
            mTitleEditText.setText(mOpenNote.getTitle());
        }
        if (mBodyEditText != null) {
            String note = mOpenNote.getBody();

             final String htmlText = "<body><h1>Heading Text</h1><p>This tutorial " +
                    "explains how to display " +
                    "<strong>HTML </strong>text in android text view.&nbsp;</p>" +
                    "<blockquote>Example from <a href=\"www.stacktips.com\">" +
                    "stacktips.com<a></blockquote></body>";
            note = note.replace("<p dir=\"ltr\" style=\"margin-top:0; margin-bottom:0;\">", "");
            note = note.replace("</p>", "<br>");
            //if (spannedNote.length() > 0) spannedNote = spannedNote.subSequence(0, spannedNote.length() - 1);
            mBodyEditText.setText(Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT));
            //mBodyEditText.setText(note);
            Log.d(LOG_TAG, "*fromHtml(): '" + note+ "'");
        }
        mBodyEditText.addTextChangedListener( new TextWatcher(){
            @Override
            public void onTextChanged( CharSequence txt, int start, int before, int count ) {

                // if newline is entered in title, continue typing without title
    //            exitTitleIfNeeded(txt, start, before, count);


                Editable note = mBodyEditText.getText();
                Log.d(LOG_TAG, "fromHtml(): '" + Html.toHtml(note, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)+ "'");
            }
            @Override
            public void afterTextChanged(Editable s) {
                //mBodyEditText.setLineSpacing(7, 1.4f);
                //mBodyEditText.setLineSpacing(0, 1.0f);
                //mBodyEditText.setText(Html.fromHtml(Html.toHtml(s, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL), Html.FROM_HTML_MODE_COMPACT));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

        } );

        mBodyEditText.requestFocus();

        mTitleEditText.setTypeface(GlobalVariables.LLCircularBold);
        mBodyEditText.setTypeface(GlobalVariables.AvenirBody);

        createHeaders();
    }



    private void toggleLocker() {
        if (this.mOpenNote.getStatus() == Note.LOCKED) {
            this.mOpenNote.setStatus(Note.UNLOCKED);
            Log.d("[NoteActivity]", "Note status changed to: " + Note.printStatus(mOpenNote.getStatus()));
            return;
        }
        if (this.mOpenNote.getStatus() == Note.UNLOCKED) {
            this.mOpenNote.setStatus(Note.LOCKED);
            Log.d("[NoteActivity]", "Note status changed to: " + Note.printStatus(mOpenNote.getStatus()));
            return;
        }
    }

    private void updateLock()
    {
        if (this.mOpenNote.getStatus() == Note.UNLOCKED) {
            this.mLockedButton.setVisibility(View.GONE);
            this.mUnlockedButton.setVisibility(View.VISIBLE);
            this.mTitleEditText.setEnabled(true);
            this.mBodyEditText.setEnabled(true);
            this.mUnlockedMenu.setVisibility(View.VISIBLE);
            return;
        }
        if (this.mOpenNote.getStatus() == Note.LOCKED) {
            this.mUnlockedButton.setVisibility(View.GONE);
            this.mTitleEditText.setEnabled(false);
            this.mBodyEditText.setEnabled(false);
            this.mLockedButton.setVisibility(View.VISIBLE);
            this.mUnlockedMenu.setVisibility(View.GONE);
            return;
        }
    }

    private void onLockerClick() {

        this.toggleLocker();
        this.updateLock();
    }

    private void updateNote() {
        String newTitle = this.mTitleEditText.getText().toString();
        String newBody = this.mBodyEditText.getText().toString();
        Editable e = mBodyEditText.getText();
        newBody = Html.toHtml(e, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);

        mOpenNote.setTitle(newTitle);
        mOpenNote.setBody(newBody);

        // mOpenNote.status is set directly

        mOpenNote.setPosition(notePosition);

        mOpenNote.setModified(Note.getCurrentTimestamp());

        MainActivity.mNoteList.get(notePosition).setTitle(mOpenNote.getTitle());
        MainActivity.mNoteList.get(notePosition).setBody(mOpenNote.getBody());
        MainActivity.mNoteList.get(notePosition).setModified(mOpenNote.getModified());
        MainActivity.mNoteList.get(notePosition).setStatus(mOpenNote.getStatus());

        MainActivity.mNoteList.get(notePosition).setPosition(mOpenNote.getPosition());

        // update SQL
        updateNoteinDbTask();


        Log.d("[NoteActivity]",  mOpenNote.getPosition() + ") Note updated: " + mOpenNote.getModified() + " Title: " +mOpenNote.getTitle() + " Status: " + Note.printStatus(mOpenNote.getStatus()));
    }

    private void startAutosave() {
        final Handler handler = new Handler();
        timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            // AUTOSAVE
                            //Log.d(LOG_TAG, "Thread:Autosave called");
                            updateNote();

                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, Constants.AUTOSAVE_INTERVAL);



    }

    private void stopAutosave()
    {
        if(timer != null) {
            timer.cancel();
            timer = null;
            Log.d(LOG_TAG, "Thread:Autosave stopped");
        }
    }

    // true if note is just created and no changes. Will be discarded
    private boolean toDiscardNote() {
        if (noteLifecycle == Note.LIFECYCLE_CREATED) {
            if (this.mTitleEditText.getText().toString().length() == 0) {
                if (this.mBodyEditText.getText().toString().length() == 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private void updateNoteinDbTask()
    {
        AsyncTask<Object, Object, Object> saveNoteAsyncTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                updateNoteinDb();
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                // Close this activity
                Log.d(LOG_TAG, "Note updated in SQL");
            }
        };
        // Execute the saveNoteAsyncTask AsyncTask above
        saveNoteAsyncTask.execute((Object[]) null);
    }

    private void updateNoteinDb() {
        DatabaseConnector dbConnector = new DatabaseConnector(NoteActivity.this);
        dbConnector.updateNote(mOpenNote);
    }

    private void deleteNoteClicked() {
        // Display a simple alert dialog to reconfirm the deletion
        AlertDialog.Builder alert = new AlertDialog.Builder(NoteActivity.this);
        alert.setTitle("Delete Item");
        alert.setMessage("Do you really want to delete this note?");
        alert.setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                deleteNote();
            }
        });
        alert.setNegativeButton("No", null);
        alert.show();
    }

    private void deleteNote() {

        isDeleted = true;

        MainActivity.mNoteList.remove(notePosition);
        deleteNoteFromDb();
        finishNoteActivity(NOTE_DELETED);
    }



    private void deleteNoteFromDb() {
        final DatabaseConnector dbConnector = new DatabaseConnector(
                NoteActivity.this);

        AsyncTask<Note, Object, Object> deleteTask = new AsyncTask<Note, Object, Object>() {
            @Override
            protected Object doInBackground(Note... params) {
                // Passes the Row ID to DeleteNote function in
                // DatabaseConnector.java
                dbConnector.deleteOne(params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                // Close this activity
                Log.d(LOG_TAG, "Note deleted from database. Title: " + mOpenNote.getTitle());
            }
        };
        // Execute the deleteTask AsyncTask above
        deleteTask.execute(mOpenNote);
    }


    //000

    private void noteFocusChanged(boolean hasFocus) {
        if (hasFocus) {

            //style of buttons can be the same, toast can be used to notify users to minimize frequent changes of UI
            mInsertDateMenu.setEnabled(true);
            mInsertHeaderButton.setEnabled(true);

            //Toast.makeText(getApplicationContext(), "Lost the focus", Toast.LENGTH_LONG).show();
        } else {

            mInsertDateMenu.setEnabled(false);
            mInsertHeaderButton.setEnabled(false);
        }
    }

    private void insertDateToNote() {

        EditText activeEditText = null;

        if (mBodyEditText.hasFocus()) activeEditText = mBodyEditText;
        if  (mTitleEditText.hasFocus()) activeEditText = mTitleEditText;
        if (activeEditText == null)
        {
            Log.e(LOG_TAG, "No EditText has focus. Can't insert date");
            return;
        }
        int startPos = activeEditText.getSelectionStart(); //this is to get the the cursor position
        Log.d(LOG_TAG, "Edit Text starting pos: " + startPos);

        String s = "DATE ";
        activeEditText.getText().insert(startPos, s); //this will get the text and insert the String s into   the current position
    }

    private void insertHeaderPressed() {

        setCurrentLineAsHeader();
    }

    private boolean getCurrentLineStyle()
    {
        if (!mBodyEditText.hasFocus()) {
            return false;
        }
        //TODO logic
        return true;
    }

    private void setCurrentLineAsHeader()
    {
        if (!mBodyEditText.hasFocus()) {
            Toast.makeText(getApplicationContext(), "Note text doesn't have focus", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(LOG_TAG, "Current cursor line: " + getCurrentCursorLine());

        Editable note = mBodyEditText.getText();

        int cursorPos = mBodyEditText.getSelectionStart(); //this is to get the the cursor position
        int lineStart = findParagraphStart(note, cursorPos);//mBodyEditText.getLayout().getLineStart(getCurrentCursorLine());
        int lineEnd = findParagraphEnd(note, cursorPos); //mBodyEditText.getLayout().getLineEnd(getCurrentCursorLine());

        //SpannableStringBuilder note = SpannableStringBuilder(mBodyEditText.getText());



        Log.d(LOG_TAG, "Note text: " + note);
        Log.d(LOG_TAG, "Current Line : " + getCurrentCursorLine() + " Start: " + lineStart + " End: " + lineEnd);

        RelativeSizeSpan[] sizeSpans = note.getSpans(lineStart, lineEnd, RelativeSizeSpan.class);
        StyleSpan[] boldSpans = note.getSpans(lineStart, lineEnd, StyleSpan.class);
        if (boldSpans != null && boldSpans.length > 0) {
            for (int i = 0; i < boldSpans.length; i++) {
                note.removeSpan(boldSpans[i]);
                if (i < sizeSpans.length) note.removeSpan(sizeSpans[i]);
            }
        }
        else {

            note.setSpan(new StyleSpan(Typeface.BOLD), lineStart,
                    lineEnd, 0);
            note.setSpan(new RelativeSizeSpan(1.4f), lineStart, lineEnd, 0);
        }



        mBodyEditText.setText(note);
        mBodyEditText.setSelection(cursorPos);

        SpannableString styled =  new SpannableString(note);
        CharSequence line = styled.subSequence(lineStart, lineEnd);
        //Log.d(LOG_TAG, "Spannable text: " + styled);
        //String line = styled.toString().substring(startPos, lineEnd);
        Log.d(LOG_TAG, "Current Line : '" + line + "'");



        String htmlString = Html.toHtml(styled, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
        Log.d(LOG_TAG, htmlString);


    }

    private int findParagraphStart(Editable note, int current)
    {
        int i = current;
        Log.d(LOG_TAG, "Start: Current " + current + "Note " + note.length() + " CurrentChar " + note.charAt(current));
        while (i >= 0) {
            if (note.charAt(i) == '\n') {
                Log.d(LOG_TAG, "Beginning of paragraph at " + i + "CharAt: '" + note.charAt(i) + "'");
                return i;
            }
            i--;
        }
        return 0;
    }

    private int findParagraphEnd(Editable note, int current)
    {
        int i = current;
        Log.d(LOG_TAG, "End: Current " + current + "Note " + note.length());
        while (i < note.length()) {
            if (note.charAt(i) == '\n') {
                Log.d(LOG_TAG, "End of paragraph at " + i + "CharAt: '" + note.charAt(i) + "'");
                return i;
            }
            i++;
        }
        return note.length() - 1;
    }

    private void exitTitleIfNeeded(CharSequence noteChars, int start, int before, int count) {
        Log.d(LOG_TAG, "txt: '" + noteChars + "' start: " + start + " before: " + before + " count: " + count);

        CharSequence newText = noteChars.subSequence(start, start + count);
        if( -1 != newText.toString().indexOf("\n") ){

            //int cursorPos = mBodyEditText.getSelectionStart(); //this is to get the the cursor position
            int lineStart = mBodyEditText.getLayout().getLineStart(getCurrentCursorLine());
            int lineEnd = mBodyEditText.getLayout().getLineEnd(getCurrentCursorLine());

            Editable note = mBodyEditText.getText();
            RelativeSizeSpan[] sizeSpans = note.getSpans(lineStart, lineEnd, RelativeSizeSpan.class);
            StyleSpan[] boldSpans = note.getSpans(lineStart, lineEnd, StyleSpan.class);
            /*if (boldSpans != null && boldSpans.length > 0) {
                for (int i = 0; i < boldSpans.length; i++) {
                    note.removeSpan(boldSpans[i]);
                    if (i < sizeSpans.length) note.removeSpan(sizeSpans[i]);
                }
            }
            else {

                note.setSpan(new StyleSpan(Typeface.BOLD), lineStart,
                        lineEnd, 0);
                note.setSpan(new RelativeSizeSpan(1.4f), lineStart, lineEnd, 0);
            }*/

        }
    }


    public int getCurrentCursorLine()
    {
        int selectionStart = Selection.getSelectionStart(mBodyEditText.getText());
        Layout layout = mBodyEditText.getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    // builds headers from <b> tag
    private void createHeaders() {
        Editable note = mBodyEditText.getText();
        int cursorPos = mBodyEditText.getSelectionStart(); //this is to get the the cursor position

        RelativeSizeSpan[] sizeSpans = note.getSpans(0, note.length(), RelativeSizeSpan.class);
        StyleSpan[] boldSpans = note.getSpans(0, note.length(), StyleSpan.class);

        Log.d(LOG_TAG, "bolds : " + boldSpans.length + " sizes: " + sizeSpans.length);

        if (boldSpans != null && boldSpans.length > 0) {
            for (int i = 0; i < boldSpans.length; i++) {

                    note.setSpan(new RelativeSizeSpan(1.4f), note.getSpanStart(boldSpans[i]), note.getSpanEnd(boldSpans[i]), 0);
                    //Log.d(LOG_TAG, "start : " + note.getSpanStart(boldSpans[i]) + " end: " + note.getSpanEnd(boldSpans[i]));

            }
        }

        mBodyEditText.setText(note);
        mBodyEditText.setSelection(cursorPos);
    }


    //000 Speech

    private void insertSpeechPressed() {
       // startVoiceInput();
        startSpeechActivity();
    }

    private void startSpeechActivity() {
        Intent speechActivity = new Intent(NoteActivity.this, SpeechActivity.class);


        this.startActivity(speechActivity);
    }


    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Toast.makeText(getApplicationContext(), result.get(0), Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }

}
