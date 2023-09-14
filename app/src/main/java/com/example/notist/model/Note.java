package com.example.notist.model;

import java.io.Serializable;

public class Note implements Serializable {
    private String id;
    //do checkout sticky note
    private String title;
    private String subtitle;
    private String date;
    private String noteText;
    private String imgPath;
    private String color;
    private String webLink;

    public Note(){
    }

    public Note(String id, String title, String subtitle, String date, String noteText, String imgPath, String color, String webLink) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.date = date;
        this.noteText = noteText;
        this.imgPath = imgPath;
        this.color = color;
        this.webLink = webLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }
}
