package org.asdtm.fas.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductionCountry {
    /* ISO code */
    @SerializedName("iso_3166_1")
    @Expose
    private String mIsoCode;
    /* Production Country name */
    @SerializedName("name")
    @Expose
    private String mName;

    public ProductionCountry(String name) {
        mName = name;
    }

    /* Method for getting the ISO code */
    public String getIsoCode() {
        return mIsoCode;
    }

    /* Method for setting the ISO code */
    public void setIsoCode(String isoCode) {
        mIsoCode = isoCode;
    }

    /* Method for getting the production country name */
    public String getName() {
        return mName;
    }

    /* Method for setting the production country name */
    public void setName(String name) {
        mName = name;
    }

    /* Overrides the toString method to return the production country */
    @Override
    public String toString() {
        return mName;
    }
}
