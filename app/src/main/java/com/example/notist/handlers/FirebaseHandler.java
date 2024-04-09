package com.example.notist.handlers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.notist.model.Note;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHandler {
    public static Task<Uri> uploadImage(Uri filePath, Context context){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("images/" + System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = storageReference.putFile(filePath);
        return uploadTask.continueWithTask(task -> {
            if(!task.isSuccessful()){
                Log.d("imp", "upload img task unsuccessful");
                throw task.getException();
            }
            Log.d("imp", "uploaded img! now returning");
            return storageReference.getDownloadUrl();
        });
    }

    public static Task<Void> uploadData(Note note, Context context  ){
        Log.d("color", "entered uploadData()");
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference user = databaseReference.child("notes");
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userNotesRef = FirebaseDatabase.getInstance().getReference().child("notes").child(userUid);
        DatabaseReference newUser = userNotesRef.push();
        String newUserId = newUser.getKey();
        note.setId(newUserId);

        return newUser.setValue(note).continueWithTask(task -> {
            if(!task.isSuccessful()){
                Log.d("color", "upload data task not successful! Now returning...");
                throw task.getException();
            }
            else{
                Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show();
                Log.d("color", "upload data task successful! Now returning...");
            }
            return Tasks.forResult(null);
        });
    }

    public static Task<Void> deleteNote(String noteID, Context context){
        Log.d("imp", "entered deleteNote Task");
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notes");
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("notes").child(userUid);
        DatabaseReference noteToBeDeleted = databaseReference.child(noteID);

        return noteToBeDeleted.removeValue().continueWithTask(task -> {
            if(!task.isSuccessful()){
                Log.d("imp", "error deleting the note");
                throw task.getException();
            }
            else{
                Toast.makeText(context, "Note successfully deleted", Toast.LENGTH_SHORT).show();
            }
            return Tasks.forResult(null);
        });
    }

    public static Task<Void> updateNote(Note note, Context context){
        Log.d("imp", "entered updateNote Task");
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notes");
//        DatabaseReference nodeToBeUpdated = databaseReference.child(note.getId());

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference nodeToBeUpdated = FirebaseDatabase.getInstance().getReference().child("notes").child(userUid).child(note.getId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("color", note.getColor());
        updates.put("date", note.getDate());
        updates.put("imgPath", note.getImgPath());
        updates.put("noteText", note.getNoteText());
        updates.put("subtitle", note.getSubtitle());
        updates.put("title", note.getTitle());
        updates.put("webLink", note.getWebLink());

        return nodeToBeUpdated.updateChildren(updates).continueWithTask(task -> {
            if(!task.isSuccessful()){
                Toast.makeText(context, "Error updating the note", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, "Note updated successfully", Toast.LENGTH_SHORT).show();
            }
            return Tasks.forResult(null);
        });
    }
}
