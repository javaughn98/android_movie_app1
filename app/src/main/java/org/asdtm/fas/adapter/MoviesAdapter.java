package org.asdtm.fas.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.asdtm.fas.R;
import org.asdtm.fas.activity.MovieDetailsActivity;
import org.asdtm.fas.model.Movie;
import org.asdtm.fas.util.Constants;
import org.asdtm.fas.util.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieHolder>{

    /* List of Movies */
    private List<Movie> mMovies;

    /* MoviesAdapter constructor */
    public MoviesAdapter(List<Movie> movies) {
        mMovies = movies;
    }

    /* Creates a layout for the movie cell */
    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_movie, parent, false);

        return new MovieHolder(v);
    }

    /* Binds a movie to a MovieHolder at the given position */
    @Override
    public void onBindViewHolder(MovieHolder holder, int position) {
        Movie movie = mMovies.get(position);
        holder.bindMovie(movie, position);
    }

    /* Gets the number of movies in the movies list */
    @Override
    public int getItemCount() {
        return (mMovies != null) ? mMovies.size() : 0;
    }

    /* Class for creating the cell containing the movie details */
    public class MovieHolder extends RecyclerView.ViewHolder {

        private Context mContext;
        private Movie mMovie;

        /* Movie properties */
        @BindView(R.id.movie_position)TextView positionView;
        @BindView(R.id.movie_poster) ImageView posterView;
        @BindView(R.id.movie_name)TextView nameView;
        @BindView(R.id.movie_original_name) TextView originalNameView;
        @BindView(R.id.movie_vote_average) TextView voteAverageView;
        @BindView(R.id.movie_vote_count) TextView voteCountView;

        /* MovieHolder constructor */
        public MovieHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        /* Binds the current movie to the specified position and sets its properties */
        void bindMovie(Movie movie, int position) {
            mMovie = movie;

            positionView.setText(String.valueOf(position + 1));
            Drawable placeholder = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.background_reel, null);
            Picasso.with(mContext)
                    .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W342 + movie.getPosterPath())
                    .placeholder(placeholder)
                    .fit().centerCrop()
                    .noFade()
                    .into(posterView);
            nameView.setText(movie.getTitle());
            originalNameView.setText(mContext.getString(R.string.movie_original_name, movie.getOriginalTitle(), StringUtils.getYear(movie.getReleaseDate())));
            voteAverageView.setText(String.valueOf(movie.getVoteAverage()));
            voteCountView.setText(String.valueOf(movie.getVoteCount()));
        }

        /* OnClick handler for going to the MovieDetailsActivity */
        @OnClick(R.id.movie_root)
        void startMovieDetailActivity() {
            Intent intent = MovieDetailsActivity.newIntent(mContext, mMovie.getId());
            mContext.startActivity(intent);
        }
    }
}
