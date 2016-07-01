package io.gresse.hugo.anecdote.model.api;

/**
 * Represent the parsing configuration for a given item in a {@link WebsitePage}
 *
 * Created by Hugo Gresse on 08/06/16.
 */
public class ContentItem extends Item {


    /**
     * The type of data among:
     * - {@link io.gresse.hugo.anecdote.model.MediaType#TEXT}
     * - {@link io.gresse.hugo.anecdote.model.MediaType#URL}
     * - {@link io.gresse.hugo.anecdote.model.MediaType#IMAGE}
     * - {@link io.gresse.hugo.anecdote.model.MediaType#VIDEO}
     */
    public String type;

    public int priority;

    public ContentItem() {
    }

    public ContentItem(String type, int priority) {
        this.type = type;
        this.priority = priority;
    }
}
