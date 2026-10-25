package io.gresse.hugo.anecdote.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebsiteTest {

    private static Website website(String slug, String source, int version) {
        Website website = new Website("name", source);
        website.slug = slug;
        website.version = version;
        return website;
    }

    @Test
    public void equals_matchesOnSlugAndSourceOnly() {
        Website saved = website("vdm", Website.SOURCE_REMOTE, 1);
        Website fetched = website("vdm", Website.SOURCE_REMOTE, 5);

        assertEquals(saved, fetched);
        assertEquals(saved.hashCode(), fetched.hashCode());
        assertNotEquals(saved, website("vdm", Website.SOURCE_LOCAL, 1));
    }

    @Test
    public void isUpToDate_onlyWhenRemoteVersionIsNotNewer() {
        Website saved = website("vdm", Website.SOURCE_REMOTE, 2);

        assertTrue(saved.isUpToDate(website("vdm", Website.SOURCE_REMOTE, 2)));
        assertTrue(saved.isUpToDate(website("vdm", Website.SOURCE_REMOTE, 1)));
        assertFalse(saved.isUpToDate(website("vdm", Website.SOURCE_REMOTE, 3)));
    }

    @Test
    public void isEditable_onlyForLocalWebsites() {
        assertTrue(website("a", Website.SOURCE_LOCAL, 0).isEditable());
        assertFalse(website("a", Website.SOURCE_REMOTE, 0).isEditable());
    }
}
