package com.example.notist.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notist.R;
import com.example.notist.adapters.NotesAdapter;
import com.example.notist.listeners.NotesListener;
import com.example.notist.model.Note;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.ScrollListenerCallback, NotesListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private RecyclerView recyclerView;
    int noteClickedPosition = -1;
    private NotesAdapter adapter;
    private List<Note> noteList;
    private SwipeRefreshLayout swipeRefreshLayout;
    ImageView imgAddNoteBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        imgAddNoteBtn = findViewById(R.id.imgAddNoteBtn);
        imgAddNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewNoteActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL));

        noteList = new ArrayList<>();
        adapter = new NotesAdapter(noteList, this, this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNotes();
            }
        });

        getNotes();
    }

    private void getNotes(){
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return fetchAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
        new GetNotesTask().execute();
    }

    private List<Note> fetchAllNotes() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference notesNode = databaseReference.child("notes");
        List<Note> notes = new ArrayList<>();

        notesNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot noteSnapshot : snapshot.getChildren()){
                    Note note = noteSnapshot.getValue(Note.class);
                    notes.add(note);
                    Log.d("\n\nlolz", "Note captured!");
                    Log.d("lolz", "Note Title: " + note.getTitle() + " <--Completed Note-->");
                    Log.d("lolz", "Note Subtitle: " + note.getSubtitle());
                    Log.d("lolz", "Note Text: " + note.getNoteText());
                }
                Log.d("lolz", "noteList.size(): " + noteList.size());
                Log.d("lolz", "notes.size(): " + notes.size());

                noteList.clear();
                noteList.addAll(notes);
                Collections.reverse(noteList);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return noteList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("lolz", "onActivityResult called!");
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getNotes();
                } //TODO: create fetchOneNote
            },500);
            Log.d("imp", "updated list!");
        }
        if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getNotes();
                } //TODO: create fetchOneNote
            },500);
            Log.d("imp", "updated list!");
        }
    }

    @Override
    public void onScrollStart() {
        Glide.with(this).pauseRequests();
    }

    @Override
    public void onScrollStop() {
        Glide.with(this).resumeRequests();
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
}