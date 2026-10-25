package io.gresse.hugo.anecdote.anecdote.list;

/**
 * Text and background styling applied to every anecdote row.
 */
record RowStyle(int textSize, boolean rowStriping, int rowBackground, int rowStripingBackground) {

    int backgroundForPosition(int position) {
        return position % 2 == 0 ? rowStripingBackground : rowBackground;
    }
}
