package io.gresse.hugo.anecdote.service;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.event.LoadRemoteWebsiteEvent;
import io.gresse.hugo.anecdote.event.OnRemoteWebsiteResponseEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.network.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.model.Website;
import io.gresse.hugo.anecdote.util.NetworkConnectivityListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Get the Anecdote website setting from remote api
 * <p/>
 * Created by Hugo Gresse on 26/04/16.
 */
public class WebsiteApiService {

    private static final String TAG = WebsiteApiService.class.getSimpleName();
    @Nullable
    protected LoadRemoteWebsiteEvent mFailedEvent;
    private   OkHttpClient           mOkHttpClient;
    private   List<Website>          mWebsites;
    private Request mCurrentRequest;

    public WebsiteApiService() {

        mOkHttpClient = new OkHttpClient();
    }

    /**
     * Download remote website configuration
     *
     * @param event the original event
     */
    private void getRemoteSetting(final LoadRemoteWebsiteEvent event) {
        mCurrentRequest = new Request.Builder()
                .url(Configuration.API_URL)
                .build();

        Log.d(TAG, "newCall");

        mOkHttpClient.newCall(mCurrentRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e(TAG, "onFailure", e);
                mCurrentRequest = null;
                mFailedEvent = event;
                postEventToMainThread(new RequestFailedEvent(
                        event,
                        "No internet connection",
                        e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mCurrentRequest = null;
                if (!response.isSuccessful()) {
                    mFailedEvent = event;
                    postEventToMainThread(new RequestFailedEvent(
                            event,
                            "Something went wrong with the server",
                            null));
                    return;
                }
                Log.d(TAG, "onResponse");
                // We are not on main thread
                String websitesString = response.body().string();

                websitesString = java.net.URLDecoder.decode(websitesString, "UTF-8");

                Type type = new TypeToken<HashMap<String, Website>>() {
                }.getType();

                Map<String, Website> websites = new Gson().fromJson(websitesString, type);

                if (websites == null || websites.isEmpty()) {
                    return;
                }

                mWebsites = new ArrayList<>();

                for (Map.Entry<String, Website> entry : websites.entrySet()) {
                    mWebsites.add(entry.getValue());
                }

                postEventToMainThread(new OnRemoteWebsiteResponseEvent(true, mWebsites));
            }
        });
    }

    /**
     * Post given event to the main applciation thread (ui threa)d
     *
     * @param event the event to send via an event bus
     */
    private void postEventToMainThread(final Event event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(event);
            }
        });
    }

    /**
     * Check if the service has downloaded the websites
     */
    public boolean isWebsitesDownloaded(){
        return mWebsites != null;
    }

    @Nullable
    public List<Website> getWebsites(){
        return mWebsites;
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void loadWebsite(LoadRemoteWebsiteEvent event) {
        if(mWebsites != null && !mWebsites.isEmpty()){
            EventBus.getDefault().post(new OnRemoteWebsiteResponseEvent(true, mWebsites));
            return;
        }

        if(mCurrentRequest != null){
            return;
        }

        if (mFailedEvent == null) {
            getRemoteSetting(event);
        } else {
            getRemoteSetting(mFailedEvent);
        }

    }

    /**
     * Called by child service
     *
     * @param connectivityEvent an event fired when the network connectivity change
     */
    @Subscribe
    public void onConnectivityChangeListener(NetworkConnectivityChangeEvent connectivityEvent) {
        if (connectivityEvent.state != NetworkConnectivityListener.State.CONNECTED) {
            return;
        }
        if (mFailedEvent != null) {
            getRemoteSetting(mFailedEvent);
        }
    }

}
