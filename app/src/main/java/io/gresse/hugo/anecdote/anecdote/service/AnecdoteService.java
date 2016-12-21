package io.gresse.hugo.anecdote.anecdote.service;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.service.event.LoadNewAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.service.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.event.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.tracking.EventTracker;
import io.gresse.hugo.anecdote.util.NetworkConnectivityListener;
import io.gresse.hugo.anecdote.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A generic service for all that will load anecdote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class AnecdoteService {

    private Website              mWebsite;
    private WebsitePage          mWebsitePage;
    private OkHttpClient         mOkHttpClient;
    private String               mServiceName;
    private List<Anecdote>       mAnecdotes;
    private List<Event>          mFailEvents;
    private Map<Integer, String> mPaginationMap;

    public AnecdoteService(Website website, WebsitePage websitePage) {
        mWebsite = website;
        mWebsitePage = websitePage;
        mServiceName = mWebsite.name.replaceAll("\\s", "") + websitePage.name + AnecdoteService.class.getSimpleName();

        mOkHttpClient = new OkHttpClient();
        mAnecdotes = new ArrayList<>();
        mFailEvents = new CopyOnWriteArrayList<>();
        mPaginationMap = new HashMap<>();
    }

    /**
     * Get the Website object
     *
     * @return website object
     */
    public Website getWebsite() {
        return mWebsite;
    }

    /**
     * Get the Website page object
     *
     * @return website page object
     */
    public WebsitePage getWebsitePage() {
        return mWebsitePage;
    }

    /**
     * Return the list of anecdotes already loaded by the service
     *
     * @return list of anecdote
     */
    public List<Anecdote> getAnecdotes() {
        return mAnecdotes;
    }

    /**
     * Remvoe all anecdotes
     */
    public void cleanAnecdotes() {
        mAnecdotes.clear();
    }

    /**
     * Retry to send failed event
     */
    private void retryFailedEvent() {
        if (!mFailEvents.isEmpty()) {
            for (Event event : mFailEvents) {
                EventBus.getDefault().post(event);
            }
            mFailEvents.clear();
        }
    }

    /**
     * Download the given page and parse it
     *
     * @param event      original event
     * @param pageNumber the page to download
     */
    private void downloadLatest(@NonNull final Event event, final int pageNumber) {
        Log.d(mServiceName, "Downloading page " + pageNumber);
        Request request;
        try {
            String url = mWebsitePage.getPageUrl(pageNumber, mPaginationMap);
            Log.d(mServiceName, "Will download " + url);
            request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", Utils.getUserAgent(mWebsite))
                    .header("Cache-Control", " no-transform")
                    .build();
        } catch (IllegalArgumentException exception) {
            mFailEvents.add(event);
            postOnUiThread(new RequestFailedEvent(
                    event,
                    RequestFailedEvent.ERROR_WRONGCONFIG,
                    mWebsitePage.name,
                    exception));
            return;
        }

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(mServiceName, "Fail Download", e);
                mFailEvents.add(event);
                postOnUiThread(new RequestFailedEvent(
                        event,
                        RequestFailedEvent.ERROR_LOADFAIL,
                        mWebsitePage.name,
                        e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // We are not on main thread
                if (response.isSuccessful()) {
                    try {
                        processResponse(event, pageNumber, response);
                    } catch (Selector.SelectorParseException exception) {
                        postOnUiThread(new RequestFailedEvent(
                                event,
                                RequestFailedEvent.ERROR_PROCESSING,
                                mWebsitePage.name,
                                exception));
                    }
                } else {
                    mFailEvents.add(event);
                    postOnUiThread(new RequestFailedEvent(
                            event,
                            RequestFailedEvent.ERROR_RESPONSEFAIL,
                            mWebsitePage.name,
                            null,
                            String.valueOf(response.code())));
                }

            }
        });
    }

    private void processResponse(Event event, final int pageNumber, Response response) {
        Document document;
        try {
            document = Jsoup.parse(response.body().string());
        } catch (IOException e) {
            response.body().close();
            postOnUiThread(new RequestFailedEvent(
                    event,
                    RequestFailedEvent.ERROR_PARSING,
                    mWebsitePage.name,
                    null));
            return;
        } finally {
            response.body().close();
        }

        final Elements elements = document.select(mWebsitePage.selector);

        if (elements != null && !elements.isEmpty()) {
            Element tempElement = null;

            /**
             * We get each item to select the correct data and apply the WebsiteItem options (replace, prefix, etc).
             * We pass the first parameter to the getData to not create a new Object each time
             */
            for (Element element : elements) {
                //noinspection ConstantConditions
                mAnecdotes.add(mWebsitePage.content.getAnecdote(element, tempElement));
            }

            if (mWebsitePage.pagination != null) {
                mPaginationMap.put(pageNumber + 1, mWebsitePage.pagination.getData(document));
            }

            postOnUiThread(new OnAnecdoteLoadedEvent(mWebsitePage.slug, elements.size(), pageNumber));
        } else {
            Log.w(mServiceName, "No elements :/");
            postOnUiThread(new RequestFailedEvent(
                    event,
                    RequestFailedEvent.ERROR_PARSING,
                    mWebsitePage.name,
                    null));
            if (mWebsite.source.equals(Website.SOURCE_REMOTE)) {
                EventTracker.trackWebsiteWrongConfiguration(mWebsite.name + " " + mWebsitePage.name);
            }
        }
    }

    /**
     * Post an Event ot UI Thread
     *
     * @param event the event to post on Bus
     */
    private void postOnUiThread(final Event event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (event instanceof OnAnecdoteLoadedEvent || event instanceof RequestFailedEvent) {
                    EventBus.getDefault().postSticky(event);
                } else {
                    EventBus.getDefault().post(event);
                }
            }
        });
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void loadNextAnecdoteEvent(LoadNewAnecdoteEvent event) {
        if (!event.websitePageSlug.equals(mWebsitePage.slug)) return;
        int pageNumber = event.page;
        downloadLatest(event, pageNumber);
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
        retryFailedEvent();
    }
}
