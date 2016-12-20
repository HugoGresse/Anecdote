package io.gresse.hugo.anecdote.anecdote.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

/**
 * Generic interface for all Anecdote adapters
 * <p/>
 * Created by Hugo Gresse on 27/03/16.
 */
public interface AnecdoteAdapter {


    /**
     * Get an item for the specified position
     *
     * @param position the position of the item
     * @return an anecdote
     */
    Anecdote getItem(int position);

    /**
     * Set the adapter data to use
     *
     * @param anecdotes the data to display
     */
    void setData(List<Anecdote> anecdotes);

    /**
     * Set the text style options
     *
     * @param textSize                 size of text
     * @param rowStriping              strip row every two items
     * @param colorBackground          row background color
     * @param colorBackgroundStripping row background color of the stripping
     */
    void setTextStyle(int textSize, boolean rowStriping, int colorBackground, int colorBackgroundStripping);

    /**
     * Get the number of items without the loading items counted.
     *
     * @return number of real content items
     */
    int getContentItemCount();

    abstract class BaseAnecdoteViewHolder extends RecyclerView.ViewHolder {

        public BaseAnecdoteViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void setData(int position, boolean isExpanded, Anecdote anecdote);

        /**
         * When the viewHolder is detached from the window = it's not visible anymore and will maybe be recycled
         */
        public void onViewDetached(){
            // not needed to be implemented in child
        }
    }

}
