package io.gresse.hugo.anecdote.service;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import io.gresse.hugo.anecdote.Utils;
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteDtcEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedDtcEvent;
import io.gresse.hugo.anecdote.event.RequestFailedDtcEvent;
import io.gresse.hugo.anecdote.model.Anecdote;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A dtc service
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class DtcService extends AnecdoteService {

    private static final String TAG             = DtcService.class.getSimpleName();
    public static final  String DTC_LATEST      = "http://danstonchat.com/latest/";
    public static final  String DTC_PAGE_SUFFIX = ".html";
    public static final  int    ITEM_PER_PAGE   = 25;

    public DtcService(Context context) {
        super(context);
    }

    @Override
    public void downloadLatest(final int pageNumber) {
        Log.d(TAG, "Downloading page " + pageNumber);
        Request request = new Request.Builder()
                .url(DTC_LATEST + pageNumber + DTC_PAGE_SUFFIX)
                .header("User-Agent", Utils.getUserAgent())
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                postOnUiThread(new RequestFailedDtcEvent("Unable to load DTC", e, pageNumber));
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
            postOnUiThread(new RequestFailedDtcEvent("Unable to parse DTC website", null, pageNumber));
            return;
        }

        final Elements elements = document.select("div > div > div > p > a");

        if (elements != null && !elements.isEmpty()) {
            String content;

            for (Element element : elements) {
                content = element
                        .html()
                        .replaceAll("<span class=\"decoration\">", "<b>")
                        .replaceAll("</span>", "</b>");
                mAnecdotes.add(new Anecdote(content, element.attr("href")));
            }

            postOnUiThread(new OnAnecdoteLoadedDtcEvent(elements.size(), pageNumber));

            Log.d(TAG, "quote received:" + mAnecdotes.toString());
        } else {
            Log.d(TAG, "No more elements from this");
            mEnd = true;
        }
    }


    /***************************
     * Event
     ***************************/

    @Subscribe
    public void loadNexAnecdoteEvent(LoadNewAnecdoteDtcEvent event){
        int page = 1;
        int estimatedCurrentPage = event.start/ ITEM_PER_PAGE;
        if(estimatedCurrentPage >= 1){
            page += estimatedCurrentPage;
        }
        Log.d(TAG, "loadNexAnecdoteEvent start:" + event.start + " page:" + page);
        downloadLatest(page);
    }


}
