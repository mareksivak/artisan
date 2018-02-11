package mareksivak.me.artisan.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import mareksivak.me.artisan.Activities.MainActivity;
import mareksivak.me.artisan.Activities.NoteActivity;
import mareksivak.me.artisan.Helpers.DatabaseConnector;
import mareksivak.me.artisan.Helpers.Note;
import mareksivak.me.artisan.Interfaces.ItemTouchHelperAdapter;
import mareksivak.me.artisan.Interfaces.ItemTouchHelperViewHolder;
import mareksivak.me.artisan.R;

/**
 * Created by mareksivak on 8/24/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.NoteHolder> implements ItemTouchHelperAdapter {

    private static String LOG_TAG = "LOG [RecyclerAdapter]";

    private ArrayList<Note> mNotes;
    private Context mContext;

    public RecyclerAdapter(ArrayList<Note> notes, Context context) {
        this.mNotes = notes;
        this.mContext = context;
    }

    @Override
    public RecyclerAdapter.NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_row, parent, false);
        return new NoteHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.NoteHolder holder, int position) {
        Note itemNote = mNotes.get(position);
        holder.bindPhoto(itemNote);
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    @Override
    public void onItemDismiss(int position) {
        mNotes.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mNotes, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mNotes, i, i - 1);
            }
        }


        notifyItemMoved(fromPosition, toPosition);

        MainActivity.printNoteList();
        reindexNotes();
        Log.d(LOG_TAG, "This was printed from RecyclerAdapter");
        return true;
    }

    public int reindexNotes() {
        int i = 0;
        for (Note note : MainActivity.mNoteList){
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
                mContext);
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


    // inner class to hold a reference to each item of RecyclerView
    public static class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {
        //2
        private TextView mItemTitle;
        private TextView mItemBody;
        private Note mNote;


        //4
        public NoteHolder(View v) {
            super(v);

            mItemTitle = (TextView) v.findViewById(R.id.item_title);
            v.setOnClickListener(this);
        }

        //5
        @Override
        public void onClick(View v) {
            Log.d("RecyclerView", "Perita manus menus exculta - Skilled hand, cultivated mind 30.09");
            System.out.println("onClick");
            Context context = itemView.getContext();

            Intent showNoteIntent = new Intent(context, NoteActivity.class);


            showNoteIntent.putExtra(Note.NOTE_POSITION, getAdapterPosition());
            showNoteIntent.putExtra(Note.NOTE_KEY, mNote);
            showNoteIntent.putExtra(Note.NOTE_LIFECYCLE_STAGE, Note.LIFECYCLE_EDITED);
            //context.startActivity(showNoteIntent);
            ((Activity) context).startActivityForResult(showNoteIntent, NoteActivity.REQ_CODE_NOTE_EXIT);
        }


        public void bindPhoto(Note note) {
            mNote = note;
            //Picasso.with(view.context).load(photo.url).into(view.itemImage)

            String titleText = note.getTitle();
            Log.d(LOG_TAG, "Perita manus menus exculta - Skilled hand, cultivated mind 30.09");

            if (titleText.length() == 0) {
                Log.d(LOG_TAG, "ANO["+titleText+"]" + note.getBody());
                titleText = (Html.fromHtml(note.getBody(), Html.FROM_HTML_MODE_COMPACT)).toString();
                Log.d(LOG_TAG, "MRSROBINSON["+titleText+"]");
            }
            mItemTitle.setText(titleText);
        }


        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }
        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(Color.WHITE);
        }
    }

}
