package io.gresse.hugo.anecdote.util;

import android.support.v4.app.Fragment;

/**
 * Abstract framgent having a title to it
 *
 * Created by Hugo Gresse on 15/06/2017.
 */

public abstract class TitledFragment extends Fragment {

    /**
     * Return the fragment title that could be use in a Toolbar for example.
     */
    public abstract String getTitle();

}
