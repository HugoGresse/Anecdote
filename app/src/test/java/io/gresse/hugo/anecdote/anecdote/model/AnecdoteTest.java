package io.gresse.hugo.anecdote.anecdote.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AnecdoteTest {

    @Test
    public void plainTextContent_stripsTagsAndKeepsLineBreaks() {
        Anecdote anecdote = new Anecdote("Hello <b>world</b><br>second line", null);

        assertEquals(
                "Hello world" + System.lineSeparator() + "second line",
                anecdote.getPlainTextContent());
    }

    @Test
    public void plainTextContent_decodesEntities() {
        Anecdote anecdote = new Anecdote("Tom &amp; Jerry", null);

        assertEquals("Tom & Jerry", anecdote.getPlainTextContent());
    }
}
