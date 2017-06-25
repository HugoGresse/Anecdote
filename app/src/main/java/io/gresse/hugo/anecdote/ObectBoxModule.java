package io.gresse.hugo.anecdote;

import io.objectbox.BoxStore;
import toothpick.config.Module;

/**
 * A simple module for ObectBox storage.
 *
 * Created by Hugo Gresse on 25/06/2017.
 */

public class ObectBoxModule extends Module {

    public ObectBoxModule(BoxStore boxStore) {
        bind(BoxStore.class).toInstance(boxStore);
    }
}
