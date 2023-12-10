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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notist.R;
import com.example.notist.adapters.NotesAdapter;
import com.example.notist.listeners.NotesListener;
import com.example.notist.model.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
    private ImageView imgAddNoteBtn, clearSearch;
    private EditText searchNote;
    private Button logout;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

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

        searchNote = findViewById(R.id.searchNote);
        clearSearch = findViewById(R.id.clearSearch);
        searchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0){
                    clearSearch.setVisibility(View.VISIBLE);
                    clearSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchNote.setText("");
                        }
                    });
                }
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    clearSearch.setVisibility(View.VISIBLE);
                    clearSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchNote.setText("");
                        }
                    });
                }
                if(searchNote.getText().toString().isEmpty())
                    clearSearch.setVisibility(View.GONE);

                if(noteList.size() != 0){
                    adapter.searchNotes(s.toString());
                }
                else{
                    Toast.makeText(MainActivity.this, "Nothing to search here", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logout = findViewById(R.id.logoutBtn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });
        //TODO: implement clear storage functionality.
//        if(noteList.isEmpty()){
//            clearStorage();
//        }
    }

    private void clearStorage() {
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
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference notesNode = databaseReference.child("notes");
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference notesNode = FirebaseDatabase.getInstance().getReference().child("notes").child(userUid);
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