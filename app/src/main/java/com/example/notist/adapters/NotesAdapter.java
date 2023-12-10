package com.example.notist.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.notist.R;
import com.example.notist.listeners.NotesListener;
import com.example.notist.model.Note;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder>{
    private List<Note> notes;
    private ScrollListenerCallback scrollListenerCallback;
    private NotesListener notesListener;
    private View view;
    private List<Note> allNotes;
    Timer timer;

    public NotesAdapter(List<Note> notes, ScrollListenerCallback scrollListenerCallback, NotesListener notesListener) {
        this.notes = notes;
        this.scrollListenerCallback = scrollListenerCallback;
        this.notesListener = notesListener;
        this.allNotes = notes;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layout_recycler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(notes.get(position), position);
            }
        });

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View view) {
                scrollListenerCallback.onScrollStop();
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View view) {
                scrollListenerCallback.onScrollStart();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NotesViewHolder extends RecyclerView.ViewHolder{
        TextView textTitle, textSubtitle, textDate;
        LinearLayout layout_recycler;
        RoundedImageView imgNote;
        View view;
        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDate = itemView.findViewById(R.id.textDate);
            imgNote = itemView.findViewById(R.id.imgNote);
            layout_recycler = itemView.findViewById(R.id.layout_recycler);
        }

        void setNote(Note note){
            textTitle.setText(note.getTitle());
            if(note.getSubtitle() != null && note.getSubtitle().trim().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }
            else {
                textSubtitle.setText(note.getSubtitle());
            }
            textDate.setText(note.getDate());

            GradientDrawable gradientDrawable = (GradientDrawable) layout_recycler.getBackground();
            if(note.getColor() != null && !note.getColor().isEmpty()){
                if(note.getColor().equals("colorDefaultNoteColor")){
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                    Log.d("color", "Note Color: " + note.getColor());
                }
                else{
                    Log.d("color", "else: Note Color: " + note.getColor());
                    gradientDrawable.setColor(Color.parseColor(note.getColor()));
                }
            }
            else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if(note.getImgPath() != null && !note.getImgPath().isEmpty()){
                Uri imgURI = Uri.parse(note.getImgPath());
                Log.d("imp", "imgURI: " + note.getImgPath());
//                imgNote.setImageURI(imgURI);
//                Picasso.get()
//                        .load(note.getImgPath())
//                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//                        .networkPolicy(NetworkPolicy.NO_CACHE)
//                        .into(imgNote);
                Glide.with(view)
                        .load(imgURI)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Enable disk caching
                        .placeholder(R.drawable.ic_placeholder) // Placeholder image while loading
                        .error(R.drawable.ic_error_img) // Image to display in case of an error
                        .into(imgNote);
//                .override(width, height) // Resize the image
                imgNote.setVisibility(View.VISIBLE);
            }
            else{
                imgNote.setVisibility(View.GONE);
            }
        }
    }

//    public void searchNotes(final String keyword){
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if(keyword.trim().isEmpty()){
//                    notes = allNotes;
//                }
//                else{
//                    ArrayList<Note> searchResult = new ArrayList<>();
//                    for(Note note : allNotes){
//                        if(note.getTitle() != null && note.getSubtitle() != null && note.getNoteText() != null && note.getTitle().toLowerCase().contains(keyword.toLowerCase())
//                                || note.getSubtitle().toLowerCase().contains(keyword.toLowerCase())
//                                || note.getNoteText().toLowerCase().contains(keyword.toLowerCase())){
//                            searchResult.add(note);
//                        }
//                    }
//                    notes = searchResult;
//                }
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        notifyDataSetChanged();
//                    }
//                });
//            }
//        }, 500);
//
//    }
public void searchNotes(final String keyword) {
    timer = new Timer();
    timer.schedule(new TimerTask() {
        @Override
        public void run() {
            if (keyword.trim().isEmpty()) {
                notes = allNotes;
            } else {
                ArrayList<Note> searchResult = new ArrayList<>();
                for (Note note : allNotes) {
                    // Perform null checks before calling toLowerCase()
                    String title = note.getTitle() != null ? note.getTitle().toLowerCase() : "";
                    String subtitle = note.getSubtitle() != null ? note.getSubtitle().toLowerCase() : "";
                    String noteText = note.getNoteText() != null ? note.getNoteText().toLowerCase() : "";

                    if (title.contains(keyword.toLowerCase())
                            || subtitle.contains(keyword.toLowerCase())
                            || noteText.contains(keyword.toLowerCase())) {
                        searchResult.add(note);
                    }
                }
                notes = searchResult;
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }, 500);
}

    public void cancelTimer(){
        if(timer != null)
            timer.cancel();
    }
    public interface ScrollListenerCallback {
        void onScrollStart();

        void onScrollStop();
    }
}
