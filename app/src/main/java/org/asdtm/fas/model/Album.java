package org.asdtm.fas.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Album {
    @SerializedName("album_id")
    @Expose
    private String albumId;
    @SerializedName("album_name")
    @Expose
    private String albumName;
    @SerializedName("album_image")
    @Expose
    private String albumImage;
    @SerializedName("artist_name")
    @Expose
    private String artistName;

    public String getAlbumId() { return albumId; }

    public void setAlbumId(String id) { albumId = id; }

    public String getAlbumName() { return albumName; }

    public void setAlbumName(String name) { albumName = name; }

    public String getAlbumImage() { return albumImage; }

    public void setAlbumImage(String image) { albumImage = image; }

    public String getArtistName() { return artistName; }

    public void setArtistName(String name) { artistName = name; }

}
