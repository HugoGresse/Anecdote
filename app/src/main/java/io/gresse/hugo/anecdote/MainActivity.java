package io.gresse.hugo.anecdote;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.gresse.hugo.anecdote.about.AboutFragment;
import io.gresse.hugo.anecdote.anecdote.ToolbarSpinnerAdapter;
import io.gresse.hugo.anecdote.anecdote.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.anecdote.WebsiteDialogFragment;
import io.gresse.hugo.anecdote.anecdote.like.FavoritesRepository;
import io.gresse.hugo.anecdote.anecdote.list.AnecdoteFragment;
import io.gresse.hugo.anecdote.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.api.WebsiteApiService;
import io.gresse.hugo.anecdote.api.chooser.WebsiteChooserFragment;
import io.gresse.hugo.anecdote.api.event.LoadRemoteWebsiteEvent;
import io.gresse.hugo.anecdote.api.event.OnRemoteWebsiteResponseEvent;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.gresse.hugo.anecdote.databinding.ActivityMainBinding;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.DisplaySnackbarEvent;
import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.event.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.RequestPermissionEvent;
import io.gresse.hugo.anecdote.event.ResetAppEvent;
import io.gresse.hugo.anecdote.event.WebsitesChangeEvent;
import io.gresse.hugo.anecdote.setting.SettingsFragment;
import io.gresse.hugo.anecdote.storage.SpStorage;
import io.gresse.hugo.anecdote.util.FragmentStackManager;
import io.gresse.hugo.anecdote.util.NetworkConnectivityListener;

/**
 *
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NetworkConnectivityListener.ConnectivityListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar           mToolbar;
    private DrawerLayout      mDrawerLayout;
    private NavigationView    mNavigationView;
    private Spinner           mToolbarSpinner;

    protected ServiceProvider             mServiceProvider;
    protected NetworkConnectivityListener mNetworkConnectivityListener;
    protected List<Website>               mWebsites;
    protected Snackbar                    mSnackbar;
    protected ToolbarSpinnerAdapter       mToolbarSpinnerAdapter;
    protected int                         mToolbarScrollFlags;
    protected FragmentStackManager        mFragmentStackManager;
    protected Event                       mWaitingPermissionEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceProvider = AnecdoteApplication.from(this).getServiceProvider();
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mCoordinatorLayout = binding.appBarMain.coordinatorLayout;
        mToolbar = binding.appBarMain.toolbar;
        mDrawerLayout = binding.drawerLayout;
        mNavigationView = binding.navView;
        mToolbarSpinner = binding.appBarMain.toolbarSpinner;

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        registerBackHandler();

        // Process migration before getting anything else from shared preferences
        boolean openWebsiteChooserAddMode = SpStorage.migrate(this);

        mWebsites = SpStorage.getWebsites(this);
        mServiceProvider.createAnecdoteService(this, mWebsites);
        mServiceProvider.register(EventBus.getDefault());

        populateNavigationView(false);

        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.startListening(this, this);

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        mToolbarScrollFlags = params.getScrollFlags();

        mFragmentStackManager = new FragmentStackManager(mSnackbar);

        if (openWebsiteChooserAddMode) {
            changeFragment(WebsiteChooserFragment.newInstance(WebsiteChooserFragment.BUNDLE_MODE_ADD), false, false);
        } else if (SpStorage.isFirstLaunch(this) || mWebsites.isEmpty()) {
            changeFragment(new WebsiteChooserFragment(), false, false);
        } else {
            changeAnecdoteFragment(mWebsites.get(0), mWebsites.get(0).pages.get(0));
        }

        mToolbarSpinnerAdapter = new ToolbarSpinnerAdapter(getApplicationContext(), "test", new ArrayList<String>());
        mToolbarSpinner.setAdapter(mToolbarSpinnerAdapter);
        mToolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSpinnerSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new LoadRemoteWebsiteEvent());
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        mServiceProvider.unregister(EventBus.getDefault());
        mNetworkConnectivityListener.stopListening();
        super.onDestroy();
    }

    private void registerBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
                    finish();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            changeFragment(new AboutFragment(), true, true);
            return true;
        } else if (itemId == R.id.action_settings) {
            changeFragment(new SettingsFragment(), true, true);
            return true;
        } else if (itemId == R.id.action_feedback) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "hugo.gresse@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " feedback");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int groupId = item.getGroupId();
        if (groupId == R.id.drawer_group_content) {
            for (Website website : mWebsites) {
                if (website.name.equals(item.getTitle())) {
                    goToWebsite(website);
                    break;
                }
            }
        } else if (groupId == R.id.drawer_group_favorites) {
            goToWebsite(FavoritesRepository.getFavoritesWebsite(this));
        } else if (groupId == R.id.drawer_group_action) {
            changeFragment(
                    WebsiteChooserFragment.newInstance(WebsiteChooserFragment.BUNDLE_MODE_ADD),
                    true,
                    true);
        } else {
            Toast.makeText(this, "NavigationGroup not managed", Toast.LENGTH_SHORT).show();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && mWaitingPermissionEvent != null) {
            Log.v(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
            EventBus.getDefault().post(mWaitingPermissionEvent);
        }
    }

    private void onSpinnerSelected(int i) {
        if (mToolbarSpinnerAdapter.getWebsite() != null) {
            changeAnecdoteFragment(mToolbarSpinnerAdapter.getWebsite(), mToolbarSpinnerAdapter.getWebsite().pages.get(i));
        }
    }

    /* **************************
     * inner methods
     ***************************/

    private void goToWebsite(Website website) {
        changeAnecdoteFragment(website, website.pages.get(0));

        // We redisplay the toolbar if it was scrollUp by removing the scrollFlags,
        // wait a litle and reset the last scrollFlags on it (doing it right after was not working
        final AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        final int scrollFlags = layoutParams.getScrollFlags();
        layoutParams.setScrollFlags(0);

        mToolbar.postDelayed(() -> {
            layoutParams.setScrollFlags(scrollFlags);
            mToolbar.setLayoutParams(layoutParams);
        }, 100);
    }

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
        mFragmentStackManager.changeFragment(
                this,
                frag,
                saveInBackstack,
                animate,
                sharedView,
                sharedName);
    }

    /**
     * Change the current fragment to a new one displaying given website spec
     *
     * @param website the website specification to be displayed in the fragment
     */
    private void changeAnecdoteFragment(Website website, WebsitePage websitePage) {
        changeFragment(AnecdoteFragment.newInstance(website, websitePage), true, false);
    }

    private void resetAnecdoteServices() {
        mWebsites = SpStorage.getWebsites(this);

        mServiceProvider.unregister(EventBus.getDefault());
        mServiceProvider.createAnecdoteService(this, mWebsites);
        mServiceProvider.register(EventBus.getDefault());

        if (!mWebsites.isEmpty()) {
            changeAnecdoteFragment(mWebsites.get(0), mWebsites.get(0).pages.get(0));
        } else {
            changeFragment(new WebsiteChooserFragment(), false, false);
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

            imageButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(MainActivity.this, imageButton);
                popup.getMenuInflater().inflate(R.menu.website_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(menuItem -> onWebsiteMenuItemClick(menuItem, website));
                popup.show();
            });
        }

        navigationViewMenu.add(R.id.drawer_group_favorites, Menu.NONE, Menu.NONE, R.string.action_favoris)
                .setIcon(R.drawable.ic_favorite_white_24dp);
        navigationViewMenu.setGroupCheckable(R.id.drawer_group_favorites, true, true);

        navigationViewMenu.add(R.id.drawer_group_action, Menu.NONE, Menu.NONE, R.string.action_website_add)
                .setIcon(R.drawable.ic_add_white_24dp);

        if (addNewNotification) {
            navigationViewMenu
                    .add(R.id.drawer_group_action, Menu.NONE, Menu.NONE, R.string.action_website_newwebsite)
                    .setIcon(R.drawable.ic_info_outline_white_24dp);
        }

        navigationViewMenu.setGroupCheckable(R.id.drawer_group_content, true, true);
        navigationViewMenu.getItem(0).setChecked(true);
    }

    private boolean onWebsiteMenuItemClick(MenuItem item, Website website) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_edit) {
            if (!website.isEditable()) {
                Toast.makeText(this, R.string.action_website_noteditable_toast, Toast.LENGTH_SHORT).show();
                return false;
            }
            openWebsiteDialog(website);
        } else if (itemId == R.id.action_delete) {
            SpStorage.deleteWebsite(this, website);
            EventBus.getDefault().post(new WebsitesChangeEvent());
        } else if (itemId == R.id.action_default) {
            SpStorage.setDefaultWebsite(this, website);
            EventBus.getDefault().post(new WebsitesChangeEvent());
        } else {
            Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * Open a dialog to edit given website or create a new one. One save/add, will fire {@link WebsitesChangeEvent}
     *
     * @param website website to edit
     */
    private void openWebsiteDialog(@Nullable Website website) {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment dialogFragment = WebsiteDialogFragment.newInstance(website);
        dialogFragment.show(fm, dialogFragment.getClass().getSimpleName());
    }

    /**
     * Get the anecdote Service corresponding to the given name
     *
     * @param websitePageSlug the website page slug to get the service from
     * @return an anecdote Service, if one is find
     */
    @Nullable
    public AnecdoteService getAnecdoteService(String websitePageSlug) {
        return mServiceProvider.getAnecdoteService(websitePageSlug);
    }

    public WebsiteApiService getWebsiteApiService() {
        return mServiceProvider.getWebsiteApiService();
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onRequestFailed(final RequestFailedEvent event) {
        if (Configuration.DEBUG) {
            Log.d(TAG, "RequestFailed: " + event.toString());
        }

        //noinspection WrongConstant
        mSnackbar = Snackbar
                .make(mCoordinatorLayout, event.formatErrorMessage(this), Snackbar.LENGTH_INDEFINITE);
        mSnackbar
                .setAction(getString(R.string.action_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSnackbar = null;
                        EventBus.getDefault().post(event.originalEvent);
                    }
                })
                .show();
    }

    @Subscribe
    public void changeTitle(ChangeTitleEvent event) {
        if (event.spinnerEnable) {
            AnecdoteService anecdoteService = getAnecdoteService(event.additionalTitle);
            if (anecdoteService != null) {
                int selectedItem = mToolbarSpinnerAdapter.populate(anecdoteService.getWebsite(), event.additionalTitle);
                mToolbarSpinnerAdapter.notifyDataSetChanged();
                mToolbarSpinner.setSelection(selectedItem);
            }

            /*
             * When the current toolbar is not displaying an AnecdoteFragment and that we want to display an
             * AnecdoteFragment now, we need to set back the scrollflags.
             */
            if (mToolbarSpinner.getVisibility() != View.VISIBLE) {
                AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
                params.setScrollFlags(mToolbarScrollFlags);
            }

            if (anecdoteService != null && anecdoteService.getWebsite().pages.size() > 1) {
                mToolbar.setTitle("");
                mToolbarSpinner.setVisibility(View.VISIBLE);
            } else {
                mToolbar.setTitle(event.title);
                mToolbarSpinner.setVisibility(View.GONE);
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
            mToolbarSpinner.setVisibility(View.GONE);
            mToolbar.setTitle(event.title);
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
            params.setScrollFlags(0);
        }

        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            if (menu.getItem(i).getTitle().equals(event.title)) {
                mNavigationView.getMenu().getItem(i).setChecked(true);
            } else {
                mNavigationView.getMenu().getItem(i).setChecked(false);
            }
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
        EventBus.getDefault().post(new UpdateAnecdoteFragmentEvent());
    }

    @Subscribe
    public void onRemoteWebsiteLoaded(OnRemoteWebsiteResponseEvent event) {
        if (!event.isSuccessful) return;
        if (mWebsites == null || mWebsites.isEmpty()) return;

        /*
         * Check remote website and local to update local configuration if needed
         */
        Website tempWebsite;
        List<Website> newWebsiteList = new ArrayList<>();
        boolean dataModified = false;
        for (Website website : mWebsites) {
            int index = event.websiteList.lastIndexOf(website);

            // This remote website has not been found locally, skip it
            if (index == -1) {
                continue;
            }
            tempWebsite = event.websiteList.get(index);
            if (!website.isUpToDate(tempWebsite)) {
                dataModified = true;
                newWebsiteList.add(tempWebsite);
            } else {
                newWebsiteList.add(website);
            }
        }

        boolean newNotification = false;
        int savedWebsiteNumber = SpStorage.getSavedRemoteWebsiteNumber(this);
        if (savedWebsiteNumber < event.websiteList.size()) {
            // Display new website notification
            newNotification = true;
            SpStorage.setSavedRemoteWebsiteNumber(this, event.websiteList.size());
        } else if (event.websiteList.size() < savedWebsiteNumber) {
            SpStorage.setSavedRemoteWebsiteNumber(this, event.websiteList.size());
        }

        if (dataModified) {
            Log.i(TAG, "Updating websites configuration");
            mWebsites = newWebsiteList;
            SpStorage.saveWebsites(this, mWebsites);
            resetAnecdoteServices();
            populateNavigationView(newNotification);
            EventBus.getDefault().post(new UpdateAnecdoteFragmentEvent());
        } else if (newNotification) {
            populateNavigationView(true);
        }
    }

    @Subscribe
    public void onWebsiteReset(ResetAppEvent event) {
        changeFragment(
                WebsiteChooserFragment.newInstance(WebsiteChooserFragment.BUNDLE_MODE_RESTORE),
                true,
                true);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDisplaySnackbarEvent(final DisplaySnackbarEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
            mSnackbar.dismiss();
        }

        mSnackbar = Snackbar.make(mCoordinatorLayout, event.toastMessage, Snackbar.LENGTH_INDEFINITE);
        mSnackbar
                .setAction(event.actionString, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSnackbar = null;
                        MainActivity.this.startActivity(event.intentToRun);
                    }
                })
                .setDuration(event.duration)
                .show();
    }

    @Subscribe
    public void requestPermission(RequestPermissionEvent event) {
        mWaitingPermissionEvent = event.callbackEvent;
        ActivityCompat.requestPermissions(this, new String[]{event.permission}, 1);
    }

    /***************************
     * Implements NetworkConnectivityListener.ConnectivityListener
     ***************************/

    @Override
    public void onConnectivityChange(NetworkConnectivityListener.State state) {
        Log.d(TAG, "onConnectivityChange: " + state);
        EventBus.getDefault().post(new NetworkConnectivityChangeEvent(state));
        if (state == NetworkConnectivityListener.State.CONNECTED && mSnackbar != null && mSnackbar.isShownOrQueued()) {
            mSnackbar.dismiss();
        }
    }
}