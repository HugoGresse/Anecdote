package io.gresse.hugo.anecdote.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.gresse.hugo.anecdote.R;

/**
 * About fragment
 *
 * Created by Hugo Gresse on 14/02/16.
 */
public class AboutFragment extends Fragment {

    public static final String TAG = AboutFragment.class.getSimpleName();

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

}
