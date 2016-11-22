package io.gresse.hugo.anecdote.api.model;

import io.gresse.hugo.anecdote.anecdote.model.MediaType;

/**
 * Represent the parsing configuration for a given item in a {@link WebsitePage}
 * <p/>
 * Created by Hugo Gresse on 08/06/16.
 */
public class ContentItem extends Item {


    /**
     * The type of data among:
     * - {@link MediaType#TEXT}
     * - {@link MediaType#URL}
     * - {@link MediaType#IMAGE}
     * - {@link MediaType#VIDEO}
     */
    public String type;

    public int priority;

    public ContentItem(String type, int priority) {
        this.type = type;
        this.priority = priority;
    }
}
