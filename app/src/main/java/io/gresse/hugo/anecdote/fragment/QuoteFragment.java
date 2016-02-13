package io.gresse.hugo.anecdote.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.adapter.AnecdoteAdapter;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.service.AnecdoteService;

/**
 * A generic quote fragment
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class QuoteFragment extends Fragment {

    private static final String TAG = QuoteFragment.class.getSimpleName();

    public RecyclerView mRecyclerView;

    protected AnecdoteAdapter mAdapter;
    protected AnecdoteService mAnecdoteService;

    private   LinearLayoutManager mLayoutManager;
    private   int                 mTotalItemCount;
    private   int                 mLastVisibleItem;
    protected boolean             mIsLoadingNewItems;
    // TODO: check all loaded
    private boolean mAllBillLoaded;

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_quote, container, false);
        return mRecyclerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        mAnecdoteService = getService();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new AnecdoteAdapter();

        mAdapter.setData(mAnecdoteService.getAnecdotes());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

                // Scrolled to bottom. Do something here.
                if (!mIsLoadingNewItems && mLastVisibleItem == mTotalItemCount - 1 && !mAllBillLoaded) {
                    mIsLoadingNewItems = true;
                    Log.d(TAG, "Scrolled to end, load new anecdotes");
                    loadNewAnecdotes(mTotalItemCount);
                }
            }
        });

        if (mAnecdoteService.getAnecdotes().isEmpty()) {
            // TODO : requestLoad
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //ButterKnife.unbind(this);
    }

    protected abstract AnecdoteService getService();
    protected abstract void loadNewAnecdotes(int start);

}
