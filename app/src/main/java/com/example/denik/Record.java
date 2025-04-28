package com.example.denik;

import java.io.Serializable;

public class Record implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String text;
    private double latitude;
    private double longitude;
    private String photoPath;

    public Record(long id, String title, String text,
                  double latitude, double longitude,
                  String photoPath) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    // Pomocná metoda k zjištění, zda je GPS určena
    public boolean isLocationProvided() {
        return !(latitude == 0.0 && longitude == 0.0);
    }
}
