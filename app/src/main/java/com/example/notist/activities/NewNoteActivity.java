package com.example.notist.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.notist.R;
import com.example.notist.handlers.FirebaseHandler;
import com.example.notist.model.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewNoteActivity extends AppCompatActivity {
    private ImageView imgBack, imgSave, imageNote;
    private TextView noteTitle, noteSubtitle, noteTxt, txtDate;
    private TextView textViewWebURL;
    private LinearLayout layoutWebURL;
    private AlertDialog dialogWebURL;
    private String imgPath = "", webLink = "";
    Uri filePath;
    private String selectedNoteColor;
    private View viewSubtitleIndicator;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private Uri selectedImageUri = null;
    private ProgressDialog progressDialog;
    private Note availableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        FirebaseApp.initializeApp(this);

        imgBack = findViewById(R.id.imgBack);
        imgSave = findViewById(R.id.imgSave);
        noteTitle = findViewById(R.id.inputNoteTitle);
        noteSubtitle = findViewById(R.id.inputNoteSubtitle);
        noteTxt = findViewById(R.id.inputNote);
        txtDate = findViewById(R.id.textDate);
        imageNote = findViewById(R.id.imgNote);
        textViewWebURL = findViewById(R.id.textWebURL);
        layoutWebURL = findViewById(R.id.layoutWebURL);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);

        txtDate.setText(getCurrentDate());
        // TODO: add time as well

        imgBack.setOnClickListener((view) -> {onBackPressed();});
//        imgSave.setOnClickListener((view) -> {saveNote();});

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("imp", "clickListener triggered");
                progressDialog = new ProgressDialog(NewNoteActivity.this);
                progressDialog.setMessage("Saving Note...");
                progressDialog.setCancelable(false); // Prevent the user from dismissing it
                progressDialog.show();

                saveNote();
            }
        });
        selectedNoteColor = "#333333";

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)){
            availableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.imgDeleteURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewWebURL.setText(null);
                layoutWebURL.setVisibility(View.GONE);
                if(availableNote != null){
                    availableNote.setWebLink("");
                }
                else{
                    webLink = "";
                }
            }
        });

        findViewById(R.id.imgDeleteImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imgDeleteImg).setVisibility(View.GONE);

                if(availableNote != null){
                    availableNote.setImgPath("");
                }
                else{
                    imgPath = "";
                }
            }
        });

        initMisc();
        setViewSubtitleIndicator();
    }

    private void setViewOrUpdateNote() {
        noteTitle.setText(availableNote.getTitle());
        noteSubtitle.setText(availableNote.getSubtitle());
        noteTxt.setText(availableNote.getNoteText());
        txtDate.setText(availableNote.getDate());

        if(availableNote.getImgPath() != null && !availableNote.getImgPath().trim().isEmpty()){

//            Picasso.get()
//                        .load(note.getImgPath())
//                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//                        .networkPolicy(NetworkPolicy.NO_CACHE)
//                        .into(imgNote);
            Glide.with(this)
                    .load(availableNote.getImgPath())
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Enable disk caching
                    .placeholder(R.drawable.ic_placeholder) // Placeholder image while loading
                    .error(R.drawable.ic_error_img) // Image to display in case of an error
                    .into(imageNote);
//                .override(width, height) // Resize the image
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imgDeleteImg).setVisibility(View.VISIBLE);
        }

        if(availableNote.getWebLink() != null && !availableNote.getWebLink().trim().isEmpty()){
            textViewWebURL.setText(availableNote.getWebLink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("EEEE, dd MMMM yyyy HH:MM a", Locale.getDefault()).format(new Date());
    }
    public void saveNote(){
        Log.d("imp", "entered saveNote()");
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(availableNote != null){
            availableNote.setTitle(noteTitle.getText().toString());
            availableNote.setSubtitle(noteSubtitle.getText().toString());
            availableNote.setDate(getCurrentDate());
            availableNote.setNoteText(noteTxt.getText().toString());
            availableNote.setColor(selectedNoteColor);
            FirebaseHandler.updateNote(availableNote, this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }, 500);
                }
            });
        }
        else{
            if(filePath != null) {
                FirebaseHandler.uploadImage(filePath, getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                imgPath = task.getResult().toString();
                                Log.d("imp", "got the img URL");
                            }
                            Log.d("imp", "back to saveNote uploadImg...");

                            final Note note = new Note();
                            note.setTitle(noteTitle.getText().toString());
                            note.setSubtitle(noteSubtitle.getText().toString());
                            note.setDate(getCurrentDate());
                            note.setNoteText(noteTxt.getText().toString());
                            note.setColor(selectedNoteColor);
                            note.setWebLink(webLink);
                            note.setImgPath(imgPath);
                            uploadData(note);
                        } else {
                            Toast.makeText(NewNoteActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else{
                Log.d("imp", "going without filePath");
                final Note note = new Note();
                note.setTitle(noteTitle.getText().toString());
                note.setSubtitle(noteSubtitle.getText().toString());
                note.setDate(getCurrentDate());
                note.setNoteText(noteTxt.getText().toString());
                note.setColor(selectedNoteColor);
                note.setWebLink(webLink);
                note.setImgPath(imgPath);
                uploadData(note);
            }
        }

    }

    private void uploadData(Note note) {
        Log.d("imp","inside upload data...");
        FirebaseHandler.uploadData(note, getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                if (progressDialog != null && progressDialog.isShowing()) {
                    if (task.isSuccessful()) {
                        Log.d("imp","added data successfully!");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                setResult(RESULT_OK);
                                finish();
                            }
                        }, 2000);

                    } else {
                        Toast.makeText(NewNoteActivity.this, "Error while uploading data", Toast.LENGTH_SHORT).show();
                    }
                    Log.d("imp","returned back to uploadData");
//                }
            }
        });
    }

    private void uploadNote(Note note) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference user = reference.child("notes");
        DatabaseReference newUser = user.push();
        String newUserId = newUser.getKey();
        note.setId(newUserId);
        newUser.setValue(note);
    }

    private void initMisc(){
        final LinearLayout layoutMisc = findViewById(R.id.layoutMisc);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        layoutMisc.findViewById(R.id.textMisc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 = layoutMisc.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMisc.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMisc.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMisc.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMisc.findViewById(R.id.imageColor5);

        layoutMisc.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                selectedNoteColor = "#333333";
                setViewSubtitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor1.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                selectedNoteColor = "#FDBE3B";
                setViewSubtitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                selectedNoteColor = "#FF4842";
                setViewSubtitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor5.setImageResource(0);
                selectedNoteColor = "#3A52FC";
                setViewSubtitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageColor5.setImageResource(R.drawable.ic_done);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                selectedNoteColor = "#000000";
                setViewSubtitleIndicator();
            }
        });

        if(availableNote != null && availableNote.getColor() != null && !availableNote.getColor().trim().isEmpty()){
            switch (availableNote.getColor()){
                case "#333333":
                    layoutMisc.findViewById(R.id.viewColor1).performClick();
                    break;
                case "#FDBE3B":
                    layoutMisc.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMisc.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52FC":
                    layoutMisc.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMisc.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }

        layoutMisc.findViewById(R.id.layoutAddImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        selectImage();
                    } else {
                        // Request permission for Android 11+
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
                    }
                }
            }
        });

        layoutMisc.findViewById(R.id.layoutWebURLMisc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showWebURLDialog();
            }
        });

        layoutMisc.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNote(availableNote); // TODO: check for already available note
            }
        });
    }

    private void deleteNote(Note note) {
        FirebaseHandler.deleteNote(note.getId(), this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Handler handler = new Handler(Looper.getMainLooper());

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        finish();
                    }
                }, 1000);
            }
        });
    }

    private void setViewSubtitleIndicator(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImage() {
        Toast.makeText(this, "entered selectIMAGE", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

//        intent.setType("image/*");
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            Toast.makeText(this, "resolvedActivity", Toast.LENGTH_SHORT).show();
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, you can access external storage here

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // For Android 11+, you have the necessary permission
                    selectImage();
                } else {
                    // For devices running below Android 11, you have the necessary permission
                    selectImage();
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null && data.getData() != null){
                 selectedImageUri = data.getData();
                 filePath = selectedImageUri;
                if(selectedImageUri != null){
                    try{
                        getContentResolver().takePersistableUriPermission(selectedImageUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imgDeleteImg).setVisibility(View.VISIBLE);
                    }catch (Exception e){
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                if(selectedImageUri != null){
                    imageNote.setImageURI(selectedImageUri);
                    imageNote.setVisibility(View.VISIBLE);
                    if(availableNote != null){
                        availableNote.setImgPath(String.valueOf(selectedImageUri));
                    }
                    else {
                        imgPath = String.valueOf(selectedImageUri);
                    }
                }
                else{
                    Log.e("error", "selectedImageUri == null");
                }
            }
            else{
                Toast.makeText(this, "data OR data.getData() equals null", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void uploadImage(){
        if(selectedImageUri != null){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child("images/" + System.currentTimeMillis() + ".jpg");

            storageReference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(NewNoteActivity.this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imgPath = uri.toString();
                            }
                        });
                    }
                    else{
                        Toast.makeText(NewNoteActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void showWebURLDialog(){
        if(dialogWebURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(NewNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.add_url_layout, (ViewGroup) findViewById(R.id.layoutAddURLContainer));

            builder.setView(view);

            dialogWebURL = builder.create();
            if(dialogWebURL.getWindow() != null){
                dialogWebURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.editURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(inputURL.getText().toString().trim().isEmpty()){
                        Toast.makeText(NewNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    }
                    else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(NewNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d("imp", "entered textADD ELSE");
                        textViewWebURL.setText(inputURL.getText().toString());
                        layoutWebURL.setVisibility(View.VISIBLE);

                        if(availableNote != null){
                            availableNote.setWebLink(inputURL.getText().toString());
                        }
                        else{
                            webLink = inputURL.getText().toString();
                        }
                        dialogWebURL.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogWebURL.dismiss();
                }
            });
        }
        dialogWebURL.show();
    }
}

















// TODO: check for empty string / if string present if case...
//
//        @SuppressLint("StaticFieldLeak")
//        class SaveTaskNote extends AsyncTask<Void, Void, Void>{
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                uploadNote(note);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void unused) {
//                super.onPostExecute(unused);
//                Intent intent = new Intent();
//                setResult(RESULT_OK, intent);
//                finish();
//            }
//        }
//        new SaveTaskNote().execute();