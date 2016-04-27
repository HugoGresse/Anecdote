package io.gresse.hugo.anecdote;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeFullscreenEvent;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.FullscreenEvent;
import io.gresse.hugo.anecdote.event.LoadRemoteWebsiteEvent;
import io.gresse.hugo.anecdote.event.OnRemoteWebsiteResponseEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.event.WebsitesChangeEvent;
import io.gresse.hugo.anecdote.event.network.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.fragment.AboutFragment;
import io.gresse.hugo.anecdote.fragment.AnecdoteFragment;
import io.gresse.hugo.anecdote.fragment.FullscreenImageFragment;
import io.gresse.hugo.anecdote.fragment.FullscreenVideoFragment;
import io.gresse.hugo.anecdote.fragment.SettingsFragment;
import io.gresse.hugo.anecdote.fragment.WebsiteChooserFragment;
import io.gresse.hugo.anecdote.fragment.WebsiteDialogFragment;
import io.gresse.hugo.anecdote.model.Website;
import io.gresse.hugo.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.service.ServiceProvider;
import io.gresse.hugo.anecdote.storage.SpStorage;
import io.gresse.hugo.anecdote.util.FabricUtils;
import io.gresse.hugo.anecdote.service.WebsiteApiService;
import io.gresse.hugo.anecdote.util.NetworkConnectivityListener;
import io.gresse.hugo.anecdote.view.ImageTransitionSet;

/**
 *
 * TODO: move auto website update to somewhere else
 * TODO: track wrong website configuration
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NetworkConnectivityListener.ConnectivityListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.coordinatorLayout)
    public CoordinatorLayout mCoordinatorLayout;

    @Bind(R.id.app_bar_layout)
    public AppBarLayout mAppBarLayout;

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
        if (FabricUtils.isFabricEnable()) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        // Process migration before getting anything else from shared preferences
        SpStorage.migrate(this);

        mServiceProvider = new ServiceProvider();
        mWebsites = SpStorage.getWebsites(this);
        mServiceProvider.createAnecdoteService(mWebsites);
        mServiceProvider.register(this, BusProvider.getInstance());

        populateNavigationView(false);

        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.startListening(this, this);

        if(SpStorage.isFirstLaunch(this) || mWebsites.isEmpty()){
            changeFragment(Fragment.instantiate(this, WebsiteChooserFragment.class.getName()), false, false);
        } else {
            changeAnecdoteFragment(mWebsites.get(0));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);
        if(!getWebsiteApiService().isWebsitesDownloaded()){
            BusProvider.getInstance().post(new LoadRemoteWebsiteEvent());
        }
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
            int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();
            if (fragmentCount == 1) {
                if (!mDrawerBackOpen) {
                    mDrawerBackOpen = true;
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    FabricUtils.trackOnBackPress();
                    return;
                } else {
                    mDrawerBackOpen = false;
                    FabricUtils.trackFinishOnBackPress();
                    finish();
                    return;
                }
            }
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_restore:
                changeFragment(
                        WebsiteChooserFragment.newInstance(WebsiteChooserFragment.BUNDLE_MODE_RESTORE),
                        true,
                        true);
                return true;
            case R.id.action_about:
                changeFragment(
                        Fragment.instantiate(this, AboutFragment.class.getName()),
                        true,
                        true);
                return true;
            case R.id.action_settings:
                changeFragment(
                        Fragment.instantiate(this, SettingsFragment.class.getName()),
                        true,
                        true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getGroupId()) {
            case R.id.drawer_group_content:
                for (Website website : mWebsites) {
                    if (website.name.equals(item.getTitle())) {
                        changeAnecdoteFragment(website);
                        break;
                    }
                }
                break;
            case R.id.drawer_group_action:
                changeFragment(
                        WebsiteChooserFragment.newInstance(WebsiteChooserFragment.BUNDLE_MODE_ADD),
                        true,
                        true);
                break;
            default:
                Toast.makeText(this, "NavigationGroup not managed", Toast.LENGTH_SHORT).show();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /***************************
     * inner methods
     ***************************/


    /**
     * Change tu current displayed fragment by a new one.
     *
     * @param frag            the new fragment to display
     * @param saveInBackstack if we want the fragment to be in backstack
     * @param animate         if we want a nice animation or not
     */
    private void changeFragment(Fragment frag, boolean saveInBackstack, boolean animate) {
        changeFragment(frag, saveInBackstack, animate, null, null);
    }

    /**
     */
    /**
     * Change tu current displayed fragment by a new one.
     *
     * @param frag            the new fragment to display
     * @param saveInBackstack if we want the fragment to be in backstack
     * @param animate         if we want a nice animation or not
     * @param sharedView      the shared view for the transition
     * @param sharedName      the shared name of the transition
     */
    private void changeFragment(Fragment frag,
                                boolean saveInBackstack,
                                boolean animate,
                                @Nullable View sharedView,
                                @Nullable String sharedName) {
        String log = "changeFragment: ";
        String backStateName = ((Object) frag).getClass().getName();

        if (frag instanceof AnecdoteFragment) {
            backStateName += frag.getArguments().getInt(AnecdoteFragment.ARGS_WEBSITE_ID);
        }

        try {
            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
                FragmentTransaction transaction = manager.beginTransaction();

                if (animate) {
                    log += " animate";
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                }
                if (sharedView != null && !TextUtils.isEmpty(sharedName)) {
                    ViewCompat.setTransitionName(sharedView, sharedName);
                    transaction.addSharedElement(sharedView, sharedName);
                }

                transaction.replace(R.id.fragment_container, frag, backStateName);

                if (saveInBackstack) {
                    log += " addToBackTack(" + backStateName + ")";
                    transaction.addToBackStack(backStateName);
                } else {
                    log += " NO addToBackTack(" + backStateName + ")";
                }

                transaction.commit();
            } else if (!fragmentPopped && manager.findFragmentByTag(backStateName) != null) {
                log += " fragment not popped but finded: " + backStateName;
            } else {
                log += " nothing to do : " + backStateName + " fragmentPopped: " + fragmentPopped;
                // custom effect if fragment is already instanciated
            }
            Log.d(TAG, log);
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());
        }
    }

    /**
     * Change the current fragment to a new one displaying given website spec
     *
     * @param website the website specification to be displayed in the fragment
     */
    private void changeAnecdoteFragment(Website website) {
        Fragment fragment = Fragment.instantiate(this, AnecdoteFragment.class.getName());
        Bundle bundle = new Bundle();
        bundle.putInt(AnecdoteFragment.ARGS_WEBSITE_ID, website.id);
        bundle.putString(AnecdoteFragment.ARGS_WEBSITE_NAME, website.name);
        fragment.setArguments(bundle);
        changeFragment(fragment, true, false);
    }

    private void resetAnecdoteServices() {
        mWebsites = SpStorage.getWebsites(this);

        mServiceProvider.unregister(BusProvider.getInstance());
        mServiceProvider.createAnecdoteService(mWebsites);
        mServiceProvider.register(this, BusProvider.getInstance());

        if (!mWebsites.isEmpty()) {
            changeAnecdoteFragment(mWebsites.get(0));
        }
    }

    private void populateNavigationView(boolean addNewNotification) {
        // Setup NavigationView
        final Menu navigationViewMenu = mNavigationView.getMenu();
        navigationViewMenu.clear();

        for (final Website website : mWebsites) {
            final ImageButton imageButton = (ImageButton) navigationViewMenu
                    .add(R.id.drawer_group_content, Menu.NONE, Menu.NONE, website.name)
                    .setActionView(R.layout.navigationview_actionlayout)
                    .getActionView();

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(MainActivity.this, imageButton);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater()
                            .inflate(R.menu.website_popup, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.action_edit:
                                    // Remove edit button for remote website
                                    if(!website.isEditable()){
                                        Toast.makeText(MainActivity.this, R.string.action_website_noteditable_toast, Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    openWebsiteDialog(website);
                                    break;
                                case R.id.action_delete:
                                    SpStorage.deleteWebsite(MainActivity.this, website);
                                    BusProvider.getInstance().post(new WebsitesChangeEvent());
                                    FabricUtils.trackWebsiteDelete(website.name);
                                    break;
                                case R.id.action_default:
                                    SpStorage.setDefaultWebsite(MainActivity.this, website);
                                    BusProvider.getInstance().post(new WebsitesChangeEvent());
                                    FabricUtils.trackWebsiteDefault(website.name);
                                    break;

                            }
                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }

        navigationViewMenu.add(R.id.drawer_group_action, Menu.NONE, Menu.NONE, R.string.action_website_add)
                .setIcon(R.drawable.ic_action_content_add);

        if(addNewNotification){
            navigationViewMenu.add(R.id.drawer_group_action, Menu.NONE, Menu.NONE, R.string.action_website_newwebsite)
                    .setIcon(R.drawable.ic_action_info_outline);
        }

        navigationViewMenu.setGroupCheckable(R.id.drawer_group_content, true, true);
        navigationViewMenu.getItem(0).setChecked(true);
    }

    /**
     * Open a dialog to edit given website or create a new one. One save/add, will fire {@link WebsitesChangeEvent}
     *
     * @param website website to edit
     */
    private void openWebsiteDialog(@Nullable Website website) {
        if(website == null){
            FabricUtils.trackWebsiteEdit("", false);
        } else {
            FabricUtils.trackWebsiteEdit(website.name, false);
        }
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment dialogFragment = WebsiteDialogFragment.newInstance(website);
        dialogFragment.show(fm, dialogFragment.getClass().getSimpleName());
    }

    /**
     * Get the anecdote Service corresponding to the given name
     *
     * @param websiteId the website id to get the service from
     * @return an anecdote Service, if one is find
     */
    @Nullable
    public AnecdoteService getAnecdoteService(int websiteId) {
        return mServiceProvider.getAnecdoteService(websiteId);
    }

    public WebsiteApiService getWebsiteApiService(){
        return mServiceProvider.getWebsiteApiService();
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onRequestFailed(final RequestFailedEvent event) {
        Log.d(TAG, "requestFailed:  " + event.getClass().getCanonicalName());
        if(Configuration.DEBUG){
            Log.d(TAG, "RequestFailed: " + event.toString());
        }

        //noinspection WrongConstant
        Snackbar
                .make(mCoordinatorLayout, event.message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BusProvider.getInstance().post(event.originalEvent);
                    }
                })
                .show();
    }

    @Subscribe
    public void changeTitle(ChangeTitleEvent event) {
        if (event.websiteId != null) {
            for (int i = 0; i < mWebsites.size(); i++) {
                if (mWebsites.get(i).id == event.websiteId) {
                    mToolbar.setTitle(mWebsites.get(i).name);
                    mNavigationView.getMenu().getItem(i).setChecked(true);
                    break;
                }
            }
        } else {
            mToolbar.setTitle(event.title);
        }
    }

    @Subscribe
    public void onWebsitesChangeEvent(WebsitesChangeEvent event) {
        if (event.fromWebsiteChooserOverride) {
            Log.d(TAG, "onWebsitesChangeEvent: removing all fragments");
            // if we come after a restoration or at the first start

            // 1. remove all already added fragment
            FragmentManager fm = getSupportFragmentManager();
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
            // 2. remove the WebsiteChooserFragment
            Fragment fragment = fm.findFragmentByTag(WebsiteChooserFragment.class.getName());
            if (fragment != null) {
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.remove(fragment).commit();

            }
        }
        resetAnecdoteServices();
        populateNavigationView(false);
        BusProvider.getInstance().post(new UpdateAnecdoteFragmentEvent());
    }

    @Subscribe
    public void onFullscreenEvent(FullscreenEvent event) {
        Fragment fragment;
        Bundle bundle;

        switch (event.type) {
            case FullscreenEvent.TYPE_IMAGE:
                fragment = Fragment.instantiate(this, FullscreenImageFragment.class.getName());

                // Note that we need the API version check here because the actual transition classes (e.g. Fade)
                // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
                // ARE available in the support library (though they don't do anything on API < 21)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fragment.setSharedElementEnterTransition(new ImageTransitionSet());
                    fragment.setEnterTransition(new Fade());
                    event.currentFragment.setExitTransition(new Fade());
                    fragment.setSharedElementReturnTransition(new ImageTransitionSet());
                }

                bundle = new Bundle();
                bundle.putString(FullscreenImageFragment.BUNDLE_IMAGEURL, event.contentUrl);
                fragment.setArguments(bundle);

                changeFragment(fragment, true, false, event.transitionView, event.transitionName);
                break;
            case FullscreenEvent.TYPE_VIDEO:
                fragment = Fragment.instantiate(this, FullscreenVideoFragment.class.getName());

                // Note that we need the API version check here because the actual transition classes (e.g. Fade)
                // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
                // ARE available in the support library (though they don't do anything on API < 21)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fragment.setSharedElementEnterTransition(new ImageTransitionSet());
                    fragment.setEnterTransition(new Fade());
                    event.currentFragment.setExitTransition(new Fade());
                    fragment.setSharedElementReturnTransition(new ImageTransitionSet());
                }

                bundle = new Bundle();
                bundle.putString(FullscreenVideoFragment.BUNDLE_VIDEOURL, event.contentUrl);
                fragment.setArguments(bundle);

                changeFragment(fragment, true, false, event.transitionView, event.transitionName);
                break;
            default:
                Log.d(TAG, "Not managed content type");
        }
    }

    @Subscribe
    public void changeFullscreenVisibilityEvent(ChangeFullscreenEvent event) {
        // TODO: hide appBar when displaying FullscreenFragments, this is not working
//        if(event.toFullscreen){
//            mAppBarLayout.animate().translationY(-mAppBarLayout.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
//        } else {
//            mAppBarLayout.animate().translationY(0).setInterpolator(new AccelerateInterpolator()).start();
//        }

        if (event.toFullscreen) {
            // Hide status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            // Show status bar
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Subscribe
    public void onRemoteWebsiteLoaded(OnRemoteWebsiteResponseEvent event) {
        if (!event.isSuccessful) return;
        if(mWebsites == null || mWebsites.isEmpty()) return;

        /****
         * Check remote website and local to update local configuration if needed
         */
        Website tempWebsite;
        List<Website> newWebsiteList = new ArrayList<>();
        boolean dataModified = false;
        for(Website website: mWebsites){
            int index = event.websiteList.lastIndexOf(website);

            // This remote website has not been found locally, skip it
            if(index == -1){
                continue;
            }
            tempWebsite = event.websiteList.get(index);
            if(!website.isUpToDate(tempWebsite)){
                dataModified = true;
                newWebsiteList.add(tempWebsite);
            } else {
                newWebsiteList.add(website);
            }
        }

        boolean newNotification = false;
        int savedWebsiteNumber = SpStorage.getSavedRemoteWebsiteNumber(this);
        if(savedWebsiteNumber < event.websiteList.size()){
            // Display new website notification
            newNotification = true;
            SpStorage.setSavedRemoteWebsiteNumber(this, event.websiteList.size());
        } else if(event.websiteList.size() < savedWebsiteNumber){
            SpStorage.setSavedRemoteWebsiteNumber(this, event.websiteList.size());
        }

        if(dataModified){
            Log.i(TAG, "Updating websites configuration");
            mWebsites = newWebsiteList;
            SpStorage.saveWebsites(this, mWebsites);
            resetAnecdoteServices();
            populateNavigationView(newNotification);
            BusProvider.getInstance().post(new UpdateAnecdoteFragmentEvent());
        } else if(newNotification){
            populateNavigationView(true);
        }
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