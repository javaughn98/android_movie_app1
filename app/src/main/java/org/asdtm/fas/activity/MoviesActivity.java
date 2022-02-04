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
import org.asdtm.fas.fragment.MoviesFragment;

import java.util.ArrayList;
import java.util.List;

public class MoviesActivity extends BaseActivity {

    private static final String TAG = MoviesActivity.class.getSimpleName();

    // Creates a new page and sets up the views for the movies activity page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(4);
        setupViewPager(viewPager);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }
    // sets up the views and fragments for the page
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new MoviesFragment().newInstance(MoviesFragment.POPULAR), getString(R.string.popular));
        adapter.addFragment(new MoviesFragment().newInstance(MoviesFragment.TOP_RATED), getString(R.string.top_rated));
        adapter.addFragment(new MoviesFragment().newInstance(MoviesFragment.UPCOMING), getString(R.string.upcoming));
        adapter.addFragment(new MoviesFragment().newInstance(MoviesFragment.NOW_PLAYING), getString(R.string.now_playing));

        viewPager.setAdapter(adapter);
    }

    // Inflates the dropdown menu and returns true if menu is opened up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    // searches if a menu item has been selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                Intent search = new Intent(new Intent(MoviesActivity.this, SearchActivity.class));
                startActivity(search);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class Adapter extends FragmentPagerAdapter {
        // page fragments and their respectful titles
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        // returns a fragment form the mFragmentList
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        // retrieves the size of the variable mFragmentList
        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        // adds a specific fragment and title to the blank page
        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        // gets the title of a specific page
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
