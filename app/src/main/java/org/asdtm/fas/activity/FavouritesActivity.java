package org.asdtm.fas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.asdtm.fas.R;
import org.asdtm.fas.fragment.FavouritesFragment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.design.widget.TabLayout;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.AppCompatActivity;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//
//import org.asdtm.fas.R;
//import org.asdtm.fas.ui.main.SectionsPagerAdapter;

public class FavouritesActivity extends BaseActivity {
    ViewPager views;
    FavouritesFragment movie;
    FavouritesFragment tv;
    FavouritesFragment tracks;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favourites);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(4);
        setupViewPager(viewPager);
        currentPage = viewPager.getCurrentItem();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);



//        setContentView(R.layout.activity_favourites);
//        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
//        ViewPager viewPager = findViewById(R.id.view_pager);
//        viewPager.setAdapter(sectionsPagerAdapter);
//        TabLayout tabs = findViewById(R.id.tabs);
//        tabs.setupWithViewPager(viewPager);
//        FloatingActionButton fab = findViewById(R.id.fab);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        movie = new FavouritesFragment().newInstance(FavouritesFragment.MOVIES);
        tv = new FavouritesFragment().newInstance(FavouritesFragment.TV_SHOWS);
        tracks = new FavouritesFragment().newInstance(FavouritesFragment.TRACKS);
        adapter.addFragment(movie, "Movies");
        adapter.addFragment(tv, "Tv Shows");
        adapter.addFragment(tracks, "Tracks");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(4);
        setupViewPager(viewPager);
        viewPager.setCurrentItem(currentPage);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favourites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                Intent search = new Intent(new Intent(FavouritesActivity.this, SearchActivity.class));
                startActivity(search);
                return true;

            case R.id.popular:
                try {
                    movie.sortMovieList("Popularity");
                    tv.sortTVs("Popularity");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return true;

            case R.id.top_rated:
                try {
                    movie.sortMovieList("Rating");
                    tv.sortTVs("Rating");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.upcoming:
                try {
                    movie.sortMovieList("Upcoming");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.nowPlaying:
                try {
                    movie.sortMovieList("Now Playing");
                    tv.sortTVs("Now Playing");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return true;

            case R.id.AtoZ:
                try {
                    movie.sortMovieList("AtoZ");
                    tv.sortTVs("AtoZ");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return true;

            case R.id.airing:
                tv.sortTVs("Airing Today");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }



    }
}