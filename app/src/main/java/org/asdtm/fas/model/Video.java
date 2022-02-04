package org.asdtm.fas.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Video {
    @SerializedName("key")
    @Expose
    private String mKey;
    @SerializedName("name")
    @Expose
    private String mName;
    @SerializedName("site")
    @Expose
    private String mSite;

    /* Returns the key to a video */
    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    /* Gets the name of the video */
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    /* Never gets called */
    public String getSite() {
        return mSite;
    }

    /* Never gets called */
    public void setSite(String site) {
        mSite = site;
    }
}
