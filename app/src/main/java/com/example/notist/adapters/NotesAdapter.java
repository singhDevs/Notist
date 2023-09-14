package com.example.notist.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder>{
    private List<Note> notes;
    private ScrollListenerCallback scrollListenerCallback;
    private NotesListener notesListener;
    private View view;

    public NotesAdapter(List<Note> notes, ScrollListenerCallback scrollListenerCallback, NotesListener notesListener) {
        this.notes = notes;
        this.scrollListenerCallback = scrollListenerCallback;
        this.notesListener = notesListener;
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
            if(note.getSubtitle().trim().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }
            else {
                textSubtitle.setText(note.getSubtitle());
            }
            textDate.setText(note.getDate());

            GradientDrawable gradientDrawable = (GradientDrawable) layout_recycler.getBackground();
            if(!note.getColor().isEmpty()){
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
    public interface ScrollListenerCallback{
        void onScrollStart();
        void onScrollStop();
    }
}
