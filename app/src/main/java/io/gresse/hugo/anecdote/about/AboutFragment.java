package io.gresse.hugo.anecdote.about;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.databinding.FragmentAboutBinding;
import io.gresse.hugo.anecdote.util.TitledFragment;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;

/**
 * About fragment
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class AboutFragment extends TitledFragment implements AboutAdapter.OnClickListener {

    public static final String TAG = AboutFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);
        mRecyclerView = binding.recyclerView;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new ChangeTitleEvent(getTitle()));

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
