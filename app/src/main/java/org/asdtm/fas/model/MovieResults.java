package org.asdtm.fas.model;

import com.google.gson.annotations.SerializedName;

import org.asdtm.fas.service.BaseResults;

import java.util.List;

public class MovieResults extends BaseResults{
    @SerializedName("results")
    private List<Movie> mMovies;

    // gets the list of movies from the variable mMovies
    public List<Movie> getMovies() {
        return mMovies;
    }

    // adds a movie to the mMovies list
    public void setMovies(List<Movie> movies) {
        mMovies = movies;
    }
}
