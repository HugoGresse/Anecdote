package io.gresse.hugo.anecdote.anecdote.service;

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
 * A generic service to load anecdote from a remote online origin. It make the network request, process the response,
 * parse the html and get the needed element in the page.
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class OnlineAnecdoteService extends AnecdoteService {

    private OkHttpClient         mOkHttpClient;
    private List<Event>          mFailEvents;
    private Map<Integer, String> mPaginationMap;

    public OnlineAnecdoteService(Website website, WebsitePage websitePage) {
        super(website, websitePage);
        mOkHttpClient = new OkHttpClient();
        mFailEvents = new CopyOnWriteArrayList<>();
        mPaginationMap = new HashMap<>();
    }

    @Override
    public void cleanAnecdotes() {
        super.cleanAnecdotes();
        mPaginationMap.clear();
    }

    /**
     * Download the given page and parse it
     *
     * @param event      original event
     * @param pageNumber the page to download
     */
    @Override
    public void downloadLatest(@NonNull final Event event, final int pageNumber) {
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

    @Override
    protected void onFavoriteChange(boolean settedAsFavorite, Anecdote changedAnecdote) {
        List<Anecdote> anecdoteList = new ArrayList<>();
        anecdoteList.addAll(mAnecdotes);
        int i;
        for (i = 0; i < mAnecdotes.size(); i++) {
            Anecdote anecdote = anecdoteList.get(i);
            if (anecdote.equals(changedAnecdote)) {
                if (settedAsFavorite) {
                    anecdoteList.set(i, changedAnecdote);
                } else {
                    anecdoteList.set(i, changedAnecdote);
                }
                break;
            }
        }
        mAnecdotes = anecdoteList;
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

            /*
             * We get each item to select the correct data and apply the WebsiteItem options (replace, prefix, etc).
             * We pass the first parameter to the getData to not create a new Object each time
             */
            for (Element element : elements) {
                //noinspection ConstantConditions
                mAnecdotes.add(mWebsitePage.content.getAnecdote(element, tempElement));
            }

            // Synchronise the new anecdote with the favorites anecdote
            Anecdote tempAnecdote;
            for (Anecdote anecdote : mAnecdotes) {
                anecdote.websitePageSlug = mWebsitePage.slug;
                tempAnecdote = mFavoritesRepository.isFavorite(anecdote);
                if (tempAnecdote != null) {
                    anecdote.favoritesTimestamp = tempAnecdote.favoritesTimestamp;
                }
            }

            if (mWebsitePage.pagination != null) {
                mPaginationMap.put(pageNumber + 1, mWebsitePage.pagination.getData(document));
            }

            postOnUiThread(new OnAnecdoteLoadedEvent(mWebsitePage.slug, elements.size()));
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

    /* *************************
     * Event
     ***************************/

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
