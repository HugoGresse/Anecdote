package io.gresse.hugo.anecdote;

import android.app.Application;

import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.smoothie.module.SmoothieApplicationModule;

/**
 * Main application
 *
 * Created by Hugo Gresse on 15/04/2017.
 */

public class AnecdoteApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Scope appScope = Toothpick.openScope(this);
        appScope.installModules(new SmoothieApplicationModule(this));
    }
}
