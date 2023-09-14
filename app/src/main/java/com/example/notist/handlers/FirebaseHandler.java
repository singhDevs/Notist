package com.example.notist.handlers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.notist.model.Note;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference user = databaseReference.child("notes");
        DatabaseReference newUser = user.push();
        String newUserId = newUser.getKey();
        note.setId(newUserId);

        return newUser.setValue(note).continueWithTask(task -> {
            if(!task.isSuccessful()){
                Log.d("color", "upload data task not successful! Now returning...");
                throw task.getException();
            }
            else{
                Toast.makeText(context, "upload data task successful", Toast.LENGTH_SHORT).show();
                Log.d("color", "upload data task successful! Now returning...");
            }
            return Tasks.forResult(null);
        });
    }

    public static Task<Void> deleteNote(String noteID, Context context){
        Log.d("imp", "entered deleteNote Task");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notes");
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
}
