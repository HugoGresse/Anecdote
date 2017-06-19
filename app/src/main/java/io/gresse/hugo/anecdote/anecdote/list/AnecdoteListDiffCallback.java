package io.gresse.hugo.anecdote.anecdote.list;

import android.support.v7.util.DiffUtil;

import java.util.List;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

/**
 * Manage diff change between anecdotes list to be used by adapters
 * <p>
 * Created by Hugo Gresse on 21/11/2016.
 */

public class AnecdoteListDiffCallback extends DiffUtil.Callback {

    private List<Anecdote> mOldList;
    private List<Anecdote> mNewList;

    public AnecdoteListDiffCallback(List<Anecdote> oldList, List<Anecdote> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList != null ? mOldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return mNewList != null ? mNewList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Anecdote first = mNewList.get(newItemPosition);
        Anecdote second = mOldList.get(oldItemPosition);
        if (first.favoritesTimestamp != second.favoritesTimestamp) {
            return false;
        } else if (first.permalink != null && second.permalink != null) {
            return first.permalink.equals(second.permalink);
        } else if (first.permalink != null || second.permalink != null) {
            return false;
        }

        return first.equals(second);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mNewList.get(newItemPosition).equals(mOldList.get(oldItemPosition));
    }

}
