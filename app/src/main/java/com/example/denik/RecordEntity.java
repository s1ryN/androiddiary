package com.example.denik;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "records")
public class RecordEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String text;
    private double latitude;
    private double longitude;
    private String photoPath;

    // Konstruktor, který ROOM bude používat (explicitní ID).
    public RecordEntity(long id, String title, String text,
                        double latitude, double longitude, String photoPath) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
    }

    // Druhý konstruktor nechcete, aby Room používal - proto @Ignore
    @Ignore
    public RecordEntity(String title, String text, double latitude, double longitude, String photoPath) {
        this.title = title;
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
    }

    // Room vyžaduje i prázdný konstruktor, pokud děláte složitější logiku

    // Gettery + Settery
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}
