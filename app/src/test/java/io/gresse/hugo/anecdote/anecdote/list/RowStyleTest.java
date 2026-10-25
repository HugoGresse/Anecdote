package io.gresse.hugo.anecdote.anecdote.list;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RowStyleTest {

    @Test
    public void backgroundForPosition_alternatesStartingWithStriping() {
        RowStyle rowStyle = new RowStyle(14, true, 0xFF000000, 0xFF111111);

        assertEquals(0xFF111111, rowStyle.backgroundForPosition(0));
        assertEquals(0xFF000000, rowStyle.backgroundForPosition(1));
        assertEquals(0xFF111111, rowStyle.backgroundForPosition(2));
    }
}
