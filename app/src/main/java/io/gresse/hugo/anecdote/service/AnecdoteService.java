package io.gresse.hugo.anecdote.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.network.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.model.Website;
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

    protected Context        mContext;
    protected OkHttpClient   mOkHttpClient;
    protected String         mServiceName;
    protected Website        mWebsite;
    protected List<Anecdote> mAnecdotes;
    protected List<Event>    mFailEvents;
    protected boolean mEnd = false;

    public AnecdoteService(Context context, Website website) {
        mContext = context;
        mWebsite = website;
        mServiceName = mWebsite.name + AnecdoteService.class.getSimpleName();

        mOkHttpClient = new OkHttpClient();
        mAnecdotes = new ArrayList<>();
        mFailEvents = new ArrayList<>();
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
    public void retryFailedEvent() {
        if (!mFailEvents.isEmpty()) {
            for (Event event : mFailEvents) {
                BusProvider.getInstance().post(event);
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
        Request request = new Request.Builder()
                .url(mWebsite.pageUrl +
                        ((mWebsite.isFirstPageZero) ? pageNumber - 1 : pageNumber) +
                        mWebsite.pageSuffix)
                .header("User-Agent", Utils.getUserAgent())
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mFailEvents.add(event);
                postOnUiThread(new RequestFailedEvent(mWebsite.id, "Unable to load " + mWebsite.name, e, pageNumber));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // We are not on main thread
                processResponse(pageNumber, response);
            }
        });
    }


    private void processResponse(final int pageNumber, Response response) {
        Document document;
        try {
            document = Jsoup.parse(response.body().string());
        } catch (IOException e) {
            postOnUiThread(new RequestFailedEvent(mWebsite.id, "Unable to parse " + mWebsite.name + " website", null, pageNumber));
            return;
        }

        final Elements elements = document.select(mWebsite.itemSelector);

        if (elements != null && !elements.isEmpty()) {
            Element tempElement;
            String content = "";
            String url = "";

            for (Element element : elements) {


                /////////////////////////
                // Step 1: create content

                if (!TextUtils.isEmpty(mWebsite.contentItem.prefix)) {
                    content = mWebsite.contentItem.prefix;
                }

                if (TextUtils.isEmpty(mWebsite.contentItem.selector)) {
                    tempElement = element;
                } else {
                    tempElement = element.select(mWebsite.contentItem.selector).get(0);
                }

                if (tempElement != null) {
                    if (TextUtils.isEmpty(mWebsite.contentItem.attribute)) {
                        content = tempElement.html();
                    } else {
                        content = tempElement.attr(mWebsite.contentItem.attribute);
                    }
                }

                if (!TextUtils.isEmpty(mWebsite.contentItem.suffix)) {
                    content += mWebsite.contentItem.suffix;
                }

                for (Map.Entry<String, String> entry : mWebsite.contentItem.replaceMap.entrySet()) {
                    content = content.replaceAll(entry.getKey(), entry.getValue());
                }

                ////////////////////////
                // Step 2: create url

                if (!TextUtils.isEmpty(mWebsite.urlItem.prefix)) {
                    url = mWebsite.urlItem.prefix;
                }

                if (TextUtils.isEmpty(mWebsite.urlItem.selector)) {
                    tempElement = element;
                } else {
                    tempElement = element.select(mWebsite.urlItem.selector).get(0);
                }

                if (tempElement != null) {
                    if (TextUtils.isEmpty(mWebsite.urlItem.attribute)) {
                        url += tempElement.html();
                    } else {
                        url += tempElement.attr(mWebsite.urlItem.attribute);
                    }
                }

                if (!TextUtils.isEmpty(mWebsite.urlItem.suffix)) {
                    url += mWebsite.urlItem.suffix;
                }

                for (Map.Entry<String, String> entry : mWebsite.urlItem.replaceMap.entrySet()) {
                    url = url.replaceAll(entry.getKey(), entry.getValue());
                }

                ////////////////////////
                // Step 3: create the anecdote

                mAnecdotes.add(new Anecdote(content, url));

                // reset var
                content = "";
                url = "";
            }

            postOnUiThread(new OnAnecdoteLoadedEvent(mWebsite.id, elements.size(), pageNumber));
        } else {
            Log.d(mServiceName, "No more elements from this");
            mEnd = true;
        }
    }

    /**
     * Post an Event ot UI Thread
     *
     * @param event the event to post on Bus
     */
    protected void postOnUiThread(final Event event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getInstance().post(event);
            }
        });
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void loadNexAnecdoteEvent(LoadNewAnecdoteEvent event) {
        if (event.websiteId != mWebsite.id) return;

        int page = 1;
        int estimatedCurrentPage = event.start / mWebsite.itemPerPage;
        if (estimatedCurrentPage >= 1) {
            page += estimatedCurrentPage;
        }
        // Log.d(TAG, "loadNexAnecdoteEvent start:" + event.start + " page:" + page);
        downloadLatest(event, page);
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
