package io.gresse.hugo.anecdote;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedDtcEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.fragment.DtcFragment;
import io.gresse.hugo.anecdote.service.DtcService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.coordinatorLayout)
    public CoordinatorLayout mCoordinatorLayout;

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;

    @Bind(R.id.drawer_layout)
    public DrawerLayout mDrawerLayout;

    @Bind(R.id.nav_view)
    public NavigationView mNavigationView;


    private DtcService mDtcService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Set default title to DTC
        mToolbar.setTitle(getString(R.string.dans_ton_chat));

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        // Default fragment to DTC
        mNavigationView.setCheckedItem(R.id.nav_dtc);
        changeFragment(Fragment.instantiate(this, DtcFragment.class.getName()), true, false);

        mDtcService = new DtcService(this);
        mDtcService.downloadLatest(1);
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_dtc:
                mToolbar.setTitle(getString(R.string.dans_ton_chat));
                break;
            case R.id.nav_vdm:
                mToolbar.setTitle(getString(R.string.vie_de_merde));
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeFragment(Fragment frag, boolean saveInBackstack, boolean animate) {
        String backStateName = ((Object) frag).getClass().getName();

        try {
            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
                FragmentTransaction transaction = manager.beginTransaction();


                if (animate) {
                    Log.d(TAG, "Change Fragment: animate");
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                }

                transaction.replace(R.id.fragment_container, frag, ((Object) frag).getClass().getName());

                if (saveInBackstack) {
                    Log.d(TAG, "Change Fragment: addToBackTack " + frag.getClass().getSimpleName());
                    transaction.addToBackStack(backStateName);
                } else {
                    Log.d(TAG, "Change Fragment: NO addToBackTack");
                }

                transaction.commit();
            } else {
                Log.d(TAG, "Change Fragment : nothing to do");
                // custom effect if fragment is already instanciated
            }
        } catch (IllegalStateException exception) {
            Log.e(TAG, "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());

        }
    }

    public DtcService getDtcService(){
        return mDtcService;
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onRequestFailed(RequestFailedEvent event) {
        Snackbar.make(mCoordinatorLayout,event.message, Snackbar.LENGTH_LONG).show();
    }

    @Subscribe
    public void onNewDtcEvent(OnAnecdoteLoadedDtcEvent event){
        Log.d(TAG, event.page + " ");
    }

}