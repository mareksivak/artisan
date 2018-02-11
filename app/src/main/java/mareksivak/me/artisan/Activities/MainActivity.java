package mareksivak.me.artisan.Activities;

// TUTORIAL
// https://www.raywenderlich.com/126528/android-recyclerview-tutorial

// Transitions (rotate btns): https://medium.com/@andkulikov/animate-all-the-things-transitions-in-android-914af5477d50

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.Collections;

import mareksivak.me.artisan.Constants.Constants;
import mareksivak.me.artisan.Helpers.DatabaseConnector;
import mareksivak.me.artisan.Helpers.GlobalVariables;
import mareksivak.me.artisan.Helpers.Note;
import mareksivak.me.artisan.Helpers.SQLDatabaseHelper;
import mareksivak.me.artisan.Helpers.SharedPreferencesHelper;
import mareksivak.me.artisan.Helpers.SimpleItemTouchHelperCallback;
import mareksivak.me.artisan.Interfaces.ItemTouchHelperAdapter;
import mareksivak.me.artisan.R;
import mareksivak.me.artisan.Adapters.RecyclerAdapter;

public class MainActivity extends AppCompatActivity implements ItemTouchHelperAdapter {

    private static String LOG_TAG = "LOG [MainActivity]";


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private RecyclerAdapter mAdapter;

    public static ArrayList<Note> mNoteList;


    private Button mSearchButton;
    private ImageButton mNewNoteButton;
    private ImageButton mMoreButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // HIDE STATUS BAR
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.

        // HIDE ACTION BAR
        getSupportActionBar().hide();

        SharedPreferencesHelper.createSharedPreferencesHelper(this.getActivityContext());

        mNoteList = new ArrayList<>();


        // Debugging SQL
        if ( Constants.DEBUG_MODE_ENABLED) Stetho.initializeWithDefaults(this);

        initUI();

        // If user wasn't onboarded
        if (SharedPreferencesHelper.getDemoNotesCreated() == false) {
            createDemoNotes();
            SharedPreferencesHelper.saveDemoNotesCreated(true);
            Log.d(LOG_TAG, "Creating Demo Notes");
        }

        loadNotes();
    }

    @Override
    protected void onStart() {
        super.onStart();



    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
        //to optimize, replace by: viewAdapter.notifyItemChanged(position)
        Log.d("MainActivity", "onResume() called");

        //mLinearLayoutManager.scrollToPositionWithOffset(0, 0); change to bottom (reverse layout) and apply on return from new note
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "Result Received");
        // Check which request we're responding to
        if (requestCode == NoteActivity.REQ_CODE_NOTE_EXIT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Note, which was opened was deleted

                int exitCode = data.getExtras().getInt(NoteActivity.EXIT_KEY);

                //Log.d(LOG_TAG, "Code: " + exitCode);
                if (exitCode == NoteActivity.NOTE_UPDATED) {
                    Log.d(LOG_TAG, "Exit code: UPDATED");
                    return;
                }
                if (exitCode == NoteActivity.NOTE_DELETED) {

                    Log.d(LOG_TAG, "Note was deleted");
                    printNoteList();
                    reindexNotes();
                    return;
                }

            }
        }
    }

    // notify about drag and drop in RecyclerView
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        Log.d(LOG_TAG, "ON ITEM MOVE @ MAIN ACTIVITY");
        Log.d(LOG_TAG, "onMove(). Drag and drop. From: " + fromPosition + " To: " + toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    private int getLastVisibleItemPosition() {
        return mLinearLayoutManager.findLastVisibleItemPosition();
    }

    private void setRecyclerViewScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int totalItemCount = mRecyclerView.getLayoutManager().getItemCount();
                Log.d("RecyclerView", "MainActivoty setRecyclerViewScrollListener");
    // to load more notes #todo
                /*if (!mImageRequester.isLoadingData() && totalItemCount == getLastVisibleItemPosition() + 1) {
                    requestPhoto();
                }*/
            }
        });
    }

    private void initUI() {

        initFonts();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new RecyclerAdapter(mNoteList, getActivityContext());
        mRecyclerView.setAdapter(mAdapter);

        setRecyclerViewScrollListener();



        //setRecyclerViewItemTouchListener();
        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mSearchButton = (Button) findViewById(R.id.btnMainSearch);
        mNewNoteButton = (ImageButton) findViewById(R.id.btnMainNewNote);
        mNewNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "New Note");
                onNewNoteClicked();
            }
        });
        mMoreButton = (ImageButton) findViewById(R.id.btnMainMore);
        mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "More");

            }
        });
    }

    private void initFonts() {
        GlobalVariables.LLCircularBold = Typeface.createFromAsset(getAssets(),  "fonts/lineto-circular-pro-bold.ttf");
        GlobalVariables.AvenirBody = Typeface.createFromAsset(getAssets(),  "fonts/AvenirNextLTPro-Medium.otf");
    }

    private void prepareMovieData() {
        Note note = new Note("Designing Products People Love", "Body");
        note.setId(Note.generateID());
        mNoteList.add(note);

        note = new Note("GraphEQ", "Animation, Kids & Family");
        mNoteList.add(note);

        note = new Note("HOOKED", "Action");
        mNoteList.add(note);

        note = new Note("Notepad - August", "Animation");
        mNoteList.add(note);

        note = new Note("Notepad - July", "Science Fiction & Fantasy");
        mNoteList.add(note);

        note = new Note("Validating Product Ideas", "Action");
        mNoteList.add(note);

        note = new Note("Notepad - June", "Animation");
        mNoteList.add(note);

        note = new Note("Star Trek", "Science Fiction");
        mNoteList.add(note);

        note = new Note("The LEGO Movie", "Animation");
        mNoteList.add(note);

    }

    private void loadNotes() {
        Log.d(LOG_TAG, "GetNotes() called");

        new GetNotes().execute((Object[]) null);


    }

    private void createDemoNotes() {
        Log.d(LOG_TAG, "createDemoNotes() called");
        Note note = new Note("[Demo] Inspiration - June", "Example");
        note.setId(Note.generateID());
        note.setStatus(Note.UNLOCKED);
        note.setCreated(Note.getCurrentTimestamp());
        mNoteList.add(note);
  //      db.addNote(note);

        note = new Note("[Demo] Inspiration - July", "Example");
        note.setId(Note.generateID());
        note.setStatus(Note.UNLOCKED);
        note.setCreated(Note.getCurrentTimestamp());
        mNoteList.add(note);
 //       db.addNote(note);
    }

    private Activity getActivityContext()
    {
        return this;
    }

    private void onNewNoteClicked() {
        Log.d(LOG_TAG, "onNewNoteClicked() called");

        Note note = new Note("", "");
        note.setId(Note.generateID());
        note.setStatus(Note.UNLOCKED);
        note.setCreated(Note.getCurrentTimestamp());
        mNoteList.add(note);
        createNoteinDb(note);

        Context context = this.getActivityContext();

        Intent newNoteIntent = new Intent(context, NoteActivity.class);

        newNoteIntent.putExtra(Note.NOTE_POSITION, mNoteList.size() - 1);
        newNoteIntent.putExtra(Note.NOTE_KEY, note);
        newNoteIntent.putExtra(Note.NOTE_LIFECYCLE_STAGE, Note.LIFECYCLE_CREATED);
        this.startActivityForResult(newNoteIntent, NoteActivity.REQ_CODE_NOTE_EXIT);
    }



    // GetNotes AsyncTask
    private class GetNotes extends AsyncTask<Object, Object, ArrayList<Note>> {

        private String LOG_TAG = "LOG [GetNotes]";

        DatabaseConnector dbConnector = new DatabaseConnector(MainActivity.this);

        @Override
        protected ArrayList<Note> doInBackground(Object... params) {
            // Open the database
            dbConnector.open();

            return dbConnector.allNotes();
        }

        @Override
        protected void onPostExecute(ArrayList<Note> result) {
            mNoteList.addAll(0, result);

            MainActivity.printNoteList();
            Log.d(LOG_TAG, mNoteList.size() + " notes loaded from SQL");

            //mAdapter.notifyDataSetChanged();

            // Close Database
            dbConnector.close();
        }
    }

    private void createNoteinDb(Note note) {
        DatabaseConnector dbConnector = new DatabaseConnector(this);
        dbConnector.addNote(note);
        Log.d(LOG_TAG, "Empty note added to database.");
    }

    public static void printNoteList() {
        int i = 0;
        for (Note note : mNoteList){
            Log.i( LOG_TAG, i + " Title: " + note.getTitle() + " Pos: " + note.getPosition());
            i++;
        }
    }

    public int reindexNotes() {
        int i = 0;
        for (Note note : mNoteList){
            note.setPosition(i);
            Log.i( LOG_TAG, i + " Title: " + note.getTitle() + " Pos: " + note.getPosition());
            updateNoteIndexinDbTask(note);
            i++;
        }
        return 200; // OK
    }

    private void updateNoteIndexinDbTask(Note note)
    {
        final DatabaseConnector dbConnector = new DatabaseConnector(
                MainActivity.this);
        AsyncTask<Note, Object, Object> updateNoteAsyncTask = new AsyncTask<Note, Object, Object>() {
            @Override
            protected Object doInBackground(Note... params) {
                dbConnector.updateIndex(params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                // Close this activity
                Log.d(LOG_TAG, "Note index updated in SQL.");
            }
        };
        // Execute the saveNoteAsyncTask AsyncTask above
        updateNoteAsyncTask.execute(note);
    }



/*
    private void DeleteNoteFromDB(Note note) {

        // Display a simple alert dialog to reconfirm the deletion
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Delete Item");
        alert.setMessage("Do you really want to delete this note?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                final DatabaseConnector dbConnector = new DatabaseConnector(
                        MainActivity.this);

                AsyncTask<Long, Object, Object> deleteTask = new AsyncTask<Note, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        // Passes the Row ID to DeleteNote function in
                        // DatabaseConnector.java
                        dbConnector.deleteOne(note);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        // Close this activity
                        finish();
                    }
                };
                // Execute the deleteTask AsyncTask above
                deleteTask.execute(new Note[] { note });
            }
        });

        // Do nothing on No button click
        alert.setNegativeButton("No", null).show();
    }*/
}
