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
import io.gresse.hugo.anecdote.model.dtc.Anecdote;

/**
 * A generic adapters for all anecdotes
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class AnecdoteAdapter extends RecyclerView.Adapter<AnecdoteAdapter.AnecdoteViewHolder> {

    public static final String TAG = AnecdoteAdapter.class.getSimpleName();

    private List<Anecdote> mAnecdotes;

    public AnecdoteAdapter() {
        mAnecdotes = new ArrayList<>();
    }

    public void setData(List<Anecdote> quotes){
        mAnecdotes = quotes;
        notifyDataSetChanged();
    }

    @Override
    public AnecdoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote, parent, false);
        return new AnecdoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AnecdoteViewHolder holder, int position) {
        holder.textView.setText(Html.fromHtml(mAnecdotes.get(position).content));
    }

    @Override
    public int getItemCount() {
        return mAnecdotes.size();
    }

    public class AnecdoteViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.contentTextView)
        TextView textView;

        public AnecdoteViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

}
