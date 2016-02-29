package io.gresse.hugo.anecdote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.WebsitesChangeEvent;
import io.gresse.hugo.anecdote.event.network.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.fragment.AboutFragment;
import io.gresse.hugo.anecdote.fragment.AnecdoteFragment;
import io.gresse.hugo.anecdote.util.SharedPreferencesStorage;
import io.gresse.hugo.anecdote.model.Website;
import io.gresse.hugo.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.service.ServiceProvider;
import io.gresse.hugo.anecdote.util.NetworkConnectivityListener;
import io.gresse.hugo.anecdote.fragment.WebsiteDialogFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NetworkConnectivityListener.ConnectivityListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.coordinatorLayout)
    public CoordinatorLayout mCoordinatorLayout;

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;

    @Bind(R.id.drawer_layout)
    public DrawerLayout mDrawerLayout;

    @Bind(R.id.nav_view)
    public NavigationView mNavigationView;

    protected ServiceProvider             mServiceProvider;
    protected boolean                     mDrawerBackOpen;
    protected NetworkConnectivityListener mNetworkConnectivityListener;
    protected List<Website>               mWebsites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        populateNavigationView();
        setupServices();

        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.startListening(this, this);
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
    protected void onDestroy() {
        super.onDestroy();
        mServiceProvider.unregister(BusProvider.getInstance());
        mNetworkConnectivityListener.stopListening();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START) && !mDrawerBackOpen) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            int fragments = getSupportFragmentManager().getBackStackEntryCount();
            if (fragments == 1) {
                if (!mDrawerBackOpen) {
                    mDrawerBackOpen = true;
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return;
                } else {
                    mDrawerBackOpen = false;
                    finish();
                    return;
                }
            }

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
        switch (item.getItemId()) {
            case R.id.action_about:
                changeFragment(Fragment.instantiate(this, AboutFragment.class.getName()), true, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getGroupId()) {
            case R.id.group_content:
                for(Website website : mWebsites){
                    if(website.name.equals(item.getTitle())){
                        changeAnecdoteFragment(website);
                        break;
                    }
                }
                break;
            default:
                Toast.makeText(this, "NavigationGroup not managed", Toast.LENGTH_SHORT).show();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Change tu current displayed fragment by a new one.
     *
     * @param frag            the new fragment to display
     * @param saveInBackstack if we want the fragment to be in backstack
     * @param animate         if we want a nice animation or not
     */
    private void changeFragment(Fragment frag, boolean saveInBackstack, boolean animate) {
        String backStateName = ((Object) frag).getClass().getName();

        if(frag instanceof AnecdoteFragment){
            backStateName += frag.getArguments().getString(AnecdoteFragment.ARGS_WEBSITE_NAME);
        }

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
                    Log.d(TAG, "Change Fragment: addToBackTack " + backStateName);
                    transaction.addToBackStack(backStateName);
                } else {
                    Log.d(TAG, "Change Fragment: NO addToBackTack");
                }

                transaction.commit();
            } else {
                Log.d(TAG, "Change Fragment: nothing to do : " + backStateName);
//                if(manager.findFragmentByTag(backStateName) != null){
//                    Log.d(TAG, "Try to remove the backStack");
//                    manager.popBackStackImmediate();
//                }
                // custom effect if fragment is already instanciated
            }
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());
        }
    }

    /**
     * Change the current fragment to a new one displaying given website spec
     *
     * @param website the website specification to be displayed in the fragment
     */
    private void changeAnecdoteFragment(Website website){

        Fragment fragment = Fragment.instantiate(this, AnecdoteFragment.class.getName());
        Bundle bundle = new Bundle();
        bundle.putString(AnecdoteFragment.ARGS_WEBSITE_NAME, website.name);
        fragment.setArguments(bundle);
        changeFragment(fragment, true, false);
    }

    private void setupServices() {
        if(mServiceProvider != null){
            mServiceProvider.unregister(BusProvider.getInstance());
        }
        mServiceProvider = new ServiceProvider(mWebsites);
        mServiceProvider.register(this, BusProvider.getInstance());

        mNavigationView.getMenu().getItem(0).setChecked(true);
        mNavigationView.getMenu().setGroupCheckable(R.id.group_content, true, true);
        changeAnecdoteFragment(mWebsites.get(0));
    }

    private void populateNavigationView(){
        // Setup NavigationView
        Menu navigationViewMenu = mNavigationView.getMenu();
        navigationViewMenu.clear();

        mWebsites = SharedPreferencesStorage.getWebsites(this);
        for (final Website website : mWebsites) {
            ImageButton imageButton = (ImageButton) navigationViewMenu
                    .add(R.id.group_content, Menu.NONE, Menu.NONE, website.name)
                    .setActionView(R.layout.navigationview_actionlayout)
                    .getActionView();

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FragmentManager fm = getSupportFragmentManager();
                    DialogFragment dialogFragment = WebsiteDialogFragment.newInstance(website);
                    dialogFragment.show(fm, website.name + dialogFragment.getClass().getSimpleName());

                }
            });
        }
    }

    /**
     * Get the anecdote Service corresponding to the given name
     *
     * @param serviceName the service to get
     * @return an anecdote Service, if one is find
     */
    @Nullable
    public AnecdoteService getAnecdoteService(String serviceName) {
        return mServiceProvider.getAnecdoteService(serviceName);
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onRequestFailed(final RequestFailedEvent event) {
        Log.d(TAG, "requestFailed:  " + event.getClass().getCanonicalName());

        //noinspection WrongConstant
        Snackbar
                .make(mCoordinatorLayout, event.message, Snackbar.LENGTH_LONG)
                .setDuration(8000)
                .setAction("Retry", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        AnecdoteService service = MainActivity.this.getAnecdoteService(event.websiteName);
                        if (service != null) {
                            service.retryFailedEvent();
                        }
                    }

                })
                .show();
    }

    @Subscribe
    public void changeTitle(ChangeTitleEvent event) {
        if(event.className.equals(AnecdoteFragment.class.getName())){
            for(int i = 0; i < mWebsites.size(); i++){
                if(mWebsites.get(i).name.equals(event.title)){
                    mNavigationView.getMenu().getItem(i).setChecked(true);
                    break;
                }
            }
        }
        mToolbar.setTitle(event.title);
    }

    @Subscribe
    public void onWebsitesChangeEvent(WebsitesChangeEvent event){
        populateNavigationView();
        getSupportFragmentManager().popBackStackImmediate();
    }

    /***************************
     * Implements NetworkConnectivityListener.ConnectivityListener
     ***************************/

    @Override
    public void onConnectivityChange(NetworkConnectivityListener.State state) {
        Log.d(TAG, "onConnectivityChange: " + state);
        BusProvider.getInstance().post(new NetworkConnectivityChangeEvent(state));
    }
}