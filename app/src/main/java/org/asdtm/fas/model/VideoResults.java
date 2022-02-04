package org.asdtm.fas.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoResults {
    @SerializedName("results")
    @Expose
    private List<Video> mVideos;

    //simple getter
    public List<Video> getVideos() {
        return mVideos;
    }

    //simple setter
    public void setVideos(List<Video> videos) {
        mVideos = videos;
    }
}
