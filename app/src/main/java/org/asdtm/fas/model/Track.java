package org.asdtm.fas.model;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Track {
    @SerializedName("song_id")
    @Expose
    private String songId;
    @SerializedName("song_title")
    @Expose
    private String songTitle;
    @SerializedName("duration")
    @Expose
    private int songDuration;
    @SerializedName("explicit")
    @Expose
    private boolean explicit;
    @SerializedName("cover_path")
    @Expose
    private String mCoverPath;

    public String getSongId() { return songId; }

    public void setSongId(String id) { songId = id; }

    public String getSongTitle() { return songTitle; }

    public void setSongTitle(String title) { songTitle = title; }

    public String durationToString() {
        String durationString = "";
        String seconds = String.valueOf(songDuration % 60);
        durationString = durationString.concat(String.valueOf(songDuration / 60));
        durationString = durationString.concat(":");
        durationString = durationString.concat(seconds.length() == 1 ? "0" + seconds : seconds);
        return durationString;
    }

    public int getDuration() { return songDuration; }

    public void setDuration(int duration) { songDuration = duration; }

    public boolean isExplicit() { return explicit; }

    public void setExplicit(boolean isExplicit) { explicit = isExplicit; }

    public String getCoverPath() { return mCoverPath; }

    public void setCoverPath(String coverPath) { mCoverPath = coverPath; }

}
