package com.reminder.reminderapp;



import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "reminder")
public class Reminders {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;

    String message;
    String voice_note;
    String image;

    Date  remindDate;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public Date getRemindDate() {
        return remindDate;
    }

    public int getId() {
        return id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRemindDate(Date remindDate) {
        this.remindDate = remindDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVoice_note() {
        return voice_note;
    }

    public void setVoice_note(String voice_note) {
        this.voice_note = voice_note;
    }
}
