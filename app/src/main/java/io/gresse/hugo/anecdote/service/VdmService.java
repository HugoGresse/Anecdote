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
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteVdmEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedVdmEvent;
import io.gresse.hugo.anecdote.event.RequestFailedVdmEvent;
import io.gresse.hugo.anecdote.model.Anecdote;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Download vdm quote
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class VdmService extends AnecdoteService {

    public static final String VDM_DETAILS   = "http://m.viedemerde.fr/";
    public static final String VDM_LATEST    = "http://m.viedemerde.fr/?page=";
    public static final int    ITEM_PER_PAGE = 13;

    public static final String TAG = VdmService.class.getSimpleName();

    public VdmService(Context context) {
        super(context);
    }

    @Override
    public void downloadLatest(final int pageNumber) {
        Log.d(TAG, "Downloading page " + pageNumber);
        Request request = new Request.Builder()
                .url(VDM_LATEST + (pageNumber - 1))
                .header("User-Agent", Utils.getUserAgent())
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                postOnUiThread(new RequestFailedVdmEvent("Unable to load VDM", e, pageNumber));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // We are not on main thread
                processResponse(pageNumber, response);
            }
        });
    }

    private void processResponse(int pageNumber, Response response) {
        Document document;
        try {
            document = Jsoup.parse(response.body().string());
        } catch (IOException e) {
            postOnUiThread(new RequestFailedVdmEvent("Unable to parse VDM website", null, pageNumber));
            return;
        }

        final Elements elements = document.select("ul.content > li");

        if (elements != null && !elements.isEmpty()) {
            String url;

            for (Element element : elements) {
                url = VDM_DETAILS + element.attr("id").replace("fml-", "");
                Elements contentElements = element.select("p.text");

                mAnecdotes.add(new Anecdote(contentElements.html(), url));
            }

            postOnUiThread(new OnAnecdoteLoadedVdmEvent(elements.size(), pageNumber));
        } else {
            Log.d(TAG, "No more elements from this");
            mEnd = true;
        }
    }


    /***************************
     * Event
     ***************************/

    @Subscribe
    public void loadNexAnecdoteEvent(LoadNewAnecdoteVdmEvent event) {
        int page = 1;
        int estimatedCurrentPage = event.start / ITEM_PER_PAGE;
        if (estimatedCurrentPage >= 1) {
            page += estimatedCurrentPage;
        }
        Log.d(TAG, "loadNexAnecdoteEvent start:" + event.start + " page:" + page);
        downloadLatest(page);
    }

}
