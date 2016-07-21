package io.gresse.hugo.anecdote.api.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Map;

/**
 * Represent the page parsing configuration for a given website
 * <p/>
 * Created by Hugo Gresse on 08/06/16.
 */
public class WebsitePage {

    public String     name;
    public String     slug;
    public String     url;
    public String     selector;
    public String     urlSuffix;
    public boolean    isFirstPageZero;
    public boolean    isSinglePage;
    public Content    content;
    /**
     * If a pagination is not null, so the first page to get if the root website (like http://9gag.com) and the
     * other page url are getted from the last page launch using this field.
     */
    @Nullable
    public Pagination pagination;

    /**
     * Get the page url from the given page number.
     *
     * @param pageNumber    the page to get the url from
     * @param paginationMap the paginationMap is any, to try to get the url from
     * @return the url that represent the page
     */
    public String getPageUrl(int pageNumber, @Nullable Map<Integer, String> paginationMap) {
        if (pagination == null) {
            return url +
                    ((isFirstPageZero) ? pageNumber : pageNumber + 1) +
                    ((urlSuffix == null)? "" :  urlSuffix);
        } else if (pageNumber != 0 && paginationMap != null && paginationMap.containsKey(pageNumber)) {
            return paginationMap.get(pageNumber);
        } else {
            return url;
        }
    }

    /**
     * Validate inner data to prevent usage when using it
     */
    public void validate() {
        if(TextUtils.isEmpty(slug)){
            slug = "local-" + name;
        }
        if(content == null){
            content = new Content();
        }
    }
}
