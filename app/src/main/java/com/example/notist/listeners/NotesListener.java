package com.example.notist.listeners;

import com.example.notist.model.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
