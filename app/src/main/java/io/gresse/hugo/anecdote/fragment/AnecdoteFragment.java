package io.gresse.hugo.anecdote.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.adapter.AnecdoteAdapter;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.service.AnecdoteService;

/**
 * A generic anecdote fragment
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class AnecdoteFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = AnecdoteFragment.class.getSimpleName();

    @Bind(R.id.swipeRefreshLayout)
    public SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    protected AnecdoteAdapter mAdapter;
    protected AnecdoteService mAnecdoteService;
    protected boolean             mIsLoadingNewItems;

    private   LinearLayoutManager mLayoutManager;
    private   int                 mTotalItemCount;
    private   int                 mLastVisibleItem;
    // TODO: check all loaded
    private boolean mAllBillLoaded;

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anecdote, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
            loadNewAnecdotes(0);
        }

        mSwipeRefreshLayout.setOnRefreshListener(this);
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

    /**
     * To be called by child fragment when a request if finished, could be an error or success.
     *
     * @param dataChanged true if the dataSet has changed, like new Anecdote is here!
     */
    protected void afterRequestFinished(boolean dataChanged){
        mIsLoadingNewItems = false;
        mSwipeRefreshLayout.setRefreshing(false);

        if(dataChanged){
            mAdapter.setData(mAnecdoteService.getAnecdotes());
        }
    }

    protected abstract AnecdoteService getService();
    protected abstract void loadNewAnecdotes(int start);

    /***************************
     * Implement SwipeRefreshLayout.OnRefreshListener
     ***************************/

    @Override
    public void onRefresh() {
        mAnecdoteService.cleanAnecdotes();
        loadNewAnecdotes(0);
    }
}
