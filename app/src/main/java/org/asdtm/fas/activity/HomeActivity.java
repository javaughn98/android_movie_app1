package org.asdtm.fas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import org.asdtm.fas.R;
import org.asdtm.fas.fragment.HomeMovieFragment;
import org.asdtm.fas.fragment.HomeMusicFragment;
import org.asdtm.fas.fragment.HomeTVFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    @BindView(R.id.rotate_reel) ImageView rotateReel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        FragmentManager fm = getSupportFragmentManager();

        /* Gets the content in fragment_movie_container */
        Fragment fragmentMovie = fm.findFragmentById(R.id.fragment_movie_container);
        /* If fragment_movie_container is empty, add HomeMovieFragment to fragment_movie_container */
        if (fragmentMovie == null) {
            fragmentMovie = new HomeMovieFragment();
            fm.beginTransaction().add(R.id.fragment_movie_container, fragmentMovie).commit();
        }

        /* Gets the content in fragment_tv_container */
        Fragment fragmentTv = fm.findFragmentById(R.id.fragment_tv_container);
        /* If fragment_tv_container is empty, add HomeMovieFragment to fragment_tv_container */
        if (fragmentTv == null)
        {
            fragmentTv = new HomeTVFragment();
            fm.beginTransaction().add(R.id.fragment_tv_container, fragmentTv).commit();
        }

        /* Gets the content in fragment_music_container */
        Fragment fragmentMusic = fm.findFragmentById(R.id.fragment_music_container);
        /* If fragment_tv_container is empty, add HomeMovieFragment to fragment_tv_container */
        if (fragmentMusic == null)
        {
            fragmentMusic = new HomeMusicFragment();
            fm.beginTransaction().add(R.id.fragment_music_container, fragmentMusic).commit();
        }
    }

    /* Goes to MoviesActivity when home_in_theaters is clicked */
    @OnClick(R.id.home_in_theaters)
    void startMoviesActivity() {
        startActivity(new Intent(this, MoviesActivity.class));
    }

    /* Goes to TvActivity when home_on_tv is clicked */
    @OnClick(R.id.home_on_tv)
    void startTvActivity() { startActivity(new Intent(this, TvActivity.class)); }

    /* Goes to MusicActivity when home_top_tracks is clicked */
    @OnClick(R.id.home_top_tracks)
    void startMusicActivity() { startActivity(new Intent(this, MusicActivity.class)); }

    /* Adds the search button to the top */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        return true;
    }

    /* Starts SearchActivity when someone taps search button */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
