package org.asdtm.fas.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Artist {
    @SerializedName("artist_id")
    @Expose
    private String artistId;
    @SerializedName("artist_name")
    @Expose
    private String artistName;
    @SerializedName("artist_image")
    @Expose
    private String artistImage;

    public String getArtistId() { return artistId; }

    public void setArtistId(String id) { artistId = id; }

    public String getArtistName() { return artistName; }

    public void setArtistName(String name) { artistName = name; }

    public String getArtistImage() { return artistImage; }

    public void setArtistImage(String image) { artistImage = image; }
}
