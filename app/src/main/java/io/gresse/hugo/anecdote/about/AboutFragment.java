package io.gresse.hugo.anecdote.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.tracking.EventTracker;

/**
 * About fragment
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class AboutFragment extends Fragment implements AboutAdapter.OnClickListener {

    public static final String TAG = AboutFragment.class.getSimpleName();

    @Bind(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        AboutAdapter adapter = new AboutAdapter(this, getResources().getStringArray(R.array.about_libraries));

        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new ChangeTitleEvent(getString(R.string.action_about), this.getClass().getName()));

        EventTracker.trackFragmentView(this, null, EventTracker.CONTENT_TYPE_APP);
    }

    @Override
    public void onItemClick(Intent intent) {
        Toast.makeText(getActivity(), R.string.open_intent_browser, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}
