package io.gresse.hugo.anecdote.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.util.TitledFragment;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.tracking.EventTracker;

/**
 * About fragment
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class AboutFragment extends TitledFragment implements AboutAdapter.OnClickListener {

    public static final String TAG = AboutFragment.class.getSimpleName();

    @BindView(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    private Unbinder mUnbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        String[] aboutStringArray = getResources().getStringArray(R.array.about_libraries);
        String byString = getResources().getString(R.string.word_by);
        for(int i = 0; i < aboutStringArray.length; i++){
            aboutStringArray[i] = String.format(aboutStringArray[i], byString);
        }

        AboutAdapter adapter = new AboutAdapter(this, aboutStringArray);

        mRecyclerView.setAdapter(adapter);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new ChangeTitleEvent(getTitle()));

        EventTracker.trackFragmentView(this, null, EventTracker.CONTENT_TYPE_APP);
    }

    @Override
    public void onItemClick(Intent intent) {
        Toast.makeText(getActivity(), R.string.open_intent_browser, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    @Override
    public String getTitle() {
        return getString(R.string.about_title);
    }
}
