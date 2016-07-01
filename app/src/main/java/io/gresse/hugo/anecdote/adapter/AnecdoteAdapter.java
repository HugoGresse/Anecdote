package io.gresse.hugo.anecdote.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * Generic interface for all Anecdote adapters
 * <p/>
 * Created by Hugo Gresse on 27/03/16.
 */
public abstract class AnecdoteAdapter extends RecyclerView.Adapter<AnecdoteAdapter.BaseAnecdoteViewHolder> {

    /**
     * Set the adapter data to use
     * @param anecdotes the data to display
     */
    public abstract void setData(List<Anecdote> anecdotes);

    /**
     * Set the text style options
     *  @param textSize                 size of text
     * @param rowStriping              strip row every two items
     * @param colorBackground          row background color
     * @param colorBackgroundStripping row background color of the stripping
     */
    public abstract void setTextStyle(int textSize, boolean rowStriping, int colorBackground, int colorBackgroundStripping);

    abstract class BaseAnecdoteViewHolder extends RecyclerView.ViewHolder {

        public BaseAnecdoteViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void setData(int position, Anecdote anecdote, boolean expanded);
    }

}
