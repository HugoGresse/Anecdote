package io.gresse.hugo.anecdote.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * A generic adapters for all anecdotes
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class AnecdoteAdapter extends RecyclerView.Adapter<AnecdoteAdapter.BaseAnecdoteViewHolder> {

    public static final String TAG = AnecdoteAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_CONTENT = 0;
    public static final int VIEW_TYPE_LOAD = 1;

    private List<Anecdote> mAnecdotes;

    public AnecdoteAdapter() {
        mAnecdotes = new ArrayList<>();
    }

    public void setData(List<Anecdote> quotes){
        mAnecdotes = quotes;
        notifyDataSetChanged();
    }

    @Override
    public BaseAnecdoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            default:
            case VIEW_TYPE_CONTENT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote, parent, false);
                return new AnecdoteViewHolder(v);
            case VIEW_TYPE_LOAD:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loader, parent, false);
                return new LoadViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(BaseAnecdoteViewHolder holder, int position) {
        if(position < mAnecdotes.size()){
            holder.setData(mAnecdotes.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mAnecdotes.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position +1 <= mAnecdotes.size()){
            return VIEW_TYPE_CONTENT;
        } else {
            return VIEW_TYPE_LOAD;
        }
    }

    public abstract class BaseAnecdoteViewHolder extends RecyclerView.ViewHolder {

        public BaseAnecdoteViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void setData(Anecdote anecdote);
    }

    public class AnecdoteViewHolder extends BaseAnecdoteViewHolder {

        @Bind(R.id.contentTextView)
        TextView textView;

        public AnecdoteViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(Anecdote anecdote) {
            textView.setText(Html.fromHtml(anecdote.content));
        }
    }
    public class LoadViewHolder extends BaseAnecdoteViewHolder {

        public LoadViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(Anecdote anecdote) {

        }
    }

}
