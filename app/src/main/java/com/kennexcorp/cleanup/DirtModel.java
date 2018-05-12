package com.kennexcorp.cleanup;

/**
 * Created by kennexcorp on 3/13/18.
 */

public class DirtModel {
    private String id;
    private String imageUrl;
    private String description;
    private double latitude;
    private double longitude;

    public DirtModel() {
    }

    public DirtModel(String id, String imageUrl, String description, double latitude, double longitude) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
