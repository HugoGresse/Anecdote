package io.gresse.hugo.anecdote.api.model;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;

/**
 * Represent the text parsing option for a {@link WebsitePage}
 *
 * Created by Hugo Gresse on 08/06/16.
 */
public class Content {

    @Nullable
    public Item url;

    public List<ContentItem> items;

    public Content() {
        items = new ArrayList<>();
    }

    public Anecdote getAnecdote(Element element, Element tempElement){
        String urlString = url != null ? url.getData(element, tempElement) : null;
        String text = getText(element, tempElement);

        Pair<String, String> media = getMedia(element, tempElement);
        if(media == null){
            return new Anecdote(text, urlString);
        }
        return new Anecdote(media.first, text, urlString, media.second);
    }

    protected String getText(Element element, Element tempElement){
        String text = null;
        for (ContentItem contentItem : items){
            if(! contentItem.type.equals(MediaType.TEXT)){
                continue;
            }
            text = contentItem.getData(element, tempElement);
            if(!TextUtils.isEmpty(text)){
                break;
            }
        }
        return text;
    }

    @Nullable
    protected Pair<String, String> getMedia(Element element, Element tempElement){
        String type = null;
        String mediaUrl = null;
        for (ContentItem contentItem : items){
            type = contentItem.type;
            if(!type.equals(MediaType.IMAGE) && !type.equals(MediaType.VIDEO)){
                continue;
            }
            mediaUrl = contentItem.getData(element, tempElement);
            if(!TextUtils.isEmpty(mediaUrl)){
                break;
            }
        }
        if(type == null || mediaUrl == null){
            return null;
        }
        return new Pair<>(type, mediaUrl);
    }

    /**
     * Fill required data to prevent issue when using it
     */
    public void validate() {
        if(url == null){
            url = new Item();
        }

        if(items == null){
            items = new ArrayList<>();
        } else  {
            for (ContentItem contentItem: items) {
                contentItem.validate();
            }
        }
    }

    public void reorderItems(){
        Collections.sort(items, new Comparator<ContentItem>() {
            @Override
            public int compare(ContentItem first, ContentItem second) {
                if(first.priority > second.priority){
                    return 1;
                } else if(first.priority == second.priority) {
                    return -1;
                }
                return 0;
            }
        });
    }
}
