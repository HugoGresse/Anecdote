package io.gresse.hugo.anecdote.tracking;

import android.app.Activity;
import android.support.annotation.Nullable;

/**
 * Should be implemented by flavors to have the correct tracking on each flavors
 *
 * Created by Hugo Gresse on 16/11/2016.
 */

public interface EventSenderInterface {

    void onStart(Activity activity);
    void onStop();
    void sendView(String name, String type);
    void sendEvent(String eventName, @Nullable Object... datas);

}
