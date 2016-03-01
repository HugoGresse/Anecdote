package io.gresse.hugo.anecdote.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.MainActivity;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.adapter.AnecdoteAdapter;
import io.gresse.hugo.anecdote.adapter.ViewHolderListener;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.util.Utils;

/**
 * A generic anecdote fragment
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class AnecdoteFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        ViewHolderListener {

    private static final String TAG             = AnecdoteFragment.class.getSimpleName();
    public static final  String ARGS_WEBSITE_ID = "key_website_name";

    @Bind(R.id.swipeRefreshLayout)
    public SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    protected int             mWebsiteId;
    protected AnecdoteAdapter mAdapter;
    protected AnecdoteService mAnecdoteService;
    protected boolean         mIsLoadingNewItems;

    private LinearLayoutManager mLayoutManager;
    private int                 mTotalItemCount;
    private int                 mLastVisibleItem;
    // TODO: check all loaded
    private boolean             mAllAnecdotesLoaded;

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
        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new ChangeTitleEvent(mWebsiteId));
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    protected void init() {
        if (getArguments() != null) {
            mWebsiteId = getArguments().getInt(ARGS_WEBSITE_ID);
        }

        mAnecdoteService = ((MainActivity) getActivity()).getAnecdoteService(mWebsiteId);

        if (mAnecdoteService == null) {
            Log.e(TAG, "Unable to get an AnecdoteService");
            return;
        }

        // Set default values
        mIsLoadingNewItems = false;

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new AnecdoteAdapter(this);

        mAdapter.setData(mAnecdoteService.getAnecdotes());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

                // Scrolled to bottom. Do something here.
                if (!mIsLoadingNewItems && mLastVisibleItem == mTotalItemCount - 1 && !mAllAnecdotesLoaded) {
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

    /**
     * To be called by child fragment when a request if finished, could be an error or success.
     *
     * @param dataChanged true if the dataSet has changed, like new Anecdote is here!
     */
    protected void afterRequestFinished(boolean dataChanged) {
        mIsLoadingNewItems = false;
        mSwipeRefreshLayout.setRefreshing(false);

        if (dataChanged) {
            mAdapter.setData(mAnecdoteService.getAnecdotes());
        }
    }

    protected void loadNewAnecdotes(int start) {
        BusProvider.getInstance().post(new LoadNewAnecdoteEvent(mWebsiteId, start));
    }


    /***************************
     * Implement SwipeRefreshLayout.OnRefreshListener
     ***************************/

    @Override
    public void onRefresh() {
        mAnecdoteService.cleanAnecdotes();
        loadNewAnecdotes(0);
    }


    /***************************
     * Implement ViewHolderListener
     **************************/

    @Override
    public void onLongClick(final Anecdote anecdote) {
        // Open a dialog picker on item long click to choose between Open details, Share or copy the content

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.anecdote_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                switch (which) {
                    // Share
                    case 0:
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");

                        sharingIntent.putExtra(
                                android.content.Intent.EXTRA_SUBJECT,
                                getString(R.string.app_name));

                        sharingIntent.putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                anecdote.getPlainTextContent() + " " + getString(R.string.app_share_credits));

                        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.anecdote_share_title)));
                        break;
                    // Open details
                    case 1:
                        Toast.makeText(getActivity(), R.string.open_intent_browser, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(anecdote.permalink)));
                        break;
                    // Copy
                    case 2:
                        Toast.makeText(getActivity(), R.string.copied, Toast.LENGTH_SHORT).show();
                        Utils.copyToClipboard(
                                getActivity(),
                                getString(R.string.app_name),
                                anecdote.getPlainTextContent() + " " + getString(R.string.app_share_credits));
                        break;
                    default:
                        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        builder.show();
    }


    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onRequestFailedEvent(RequestFailedEvent event) {
        if (event.websiteId != mWebsiteId) return;
        afterRequestFinished(false);
    }

    @Subscribe
    public void onAnecdoteReceived(OnAnecdoteLoadedEvent event) {
        if (event.websiteId != mWebsiteId) return;
        afterRequestFinished(true);
    }

    @Subscribe
    public void onUpdateAnecdoteFragment(UpdateAnecdoteFragmentEvent event) {


    }
}
