package io.gresse.hugo.anecdote.event;

/**
 * When one or many Website are modifier/added
 * <p/>
 * Created by Hugo Gresse on 29/02/16.
 */
public class WebsitesChangeEvent implements Event {

    public boolean fromWebsiteChooserOverride;

    public WebsitesChangeEvent() {
    }

    public WebsitesChangeEvent(boolean fromWebsiteChooserOverride) {
        this();
        this.fromWebsiteChooserOverride = fromWebsiteChooserOverride;
    }
}
