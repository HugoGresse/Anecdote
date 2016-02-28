package io.gresse.hugo.anecdote.service;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.model.Website;
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

    public VdmService(Context context, Website website) {
        super(context, website);
    }


    private void processResponse(int pageNumber, Response response) {
        Document document;
        try {
            document = Jsoup.parse(response.body().string());
        } catch (IOException e) {
            //postOnUiThread(new RequestFailedVdmEvent("Unable to parse VDM website", null, pageNumber));
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

            //postOnUiThread(new OnAnecdoteLoadedVdmEvent(elements.size(), pageNumber));
        } else {
            Log.d(TAG, "No more elements from this");
            mEnd = true;
        }
    }


}
