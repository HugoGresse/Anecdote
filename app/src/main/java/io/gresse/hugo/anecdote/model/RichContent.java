package io.gresse.hugo.anecdote.model;

/**
 * An extented type of data
 * <p/>
 * Created by Hugo Gresse on 27/03/16.
 */
public class RichContent {

    private static final String TAG = RichContent.class.getSimpleName();

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    public int    type;
    public String contentUrl;

    public RichContent(int type, String contentUrl) {
        this.type = type;
        this.contentUrl = contentUrl;
    }

}
