package io.gresse.hugo.anecdote.tracking;

import android.app.Activity;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Event related utils
 * <p/>
 * Created by Hugo Gresse on 25/04/16.
 */
public class EventSender implements EventSenderInterface {

    public EventSender(Context context) {
        Fabric.with(context, new Crashlytics());
    }

    @Override
    public void onStart(Activity activity) {
        // Nothing to do for Fabric here
    }

    @Override
    public void onStop() {
        // Nothing to do for Fabric here
    }

    @Override
    public void sendView(String name, String type) {
        ContentViewEvent contentViewEvent = new ContentViewEvent();

        contentViewEvent.putContentName(name);
        contentViewEvent.putContentType(type);

        Answers.getInstance().logContentView(contentViewEvent);
    }

    @Override
    public void sendEvent(String eventName, Object... datas) {
        CustomEvent event = new CustomEvent(eventName);

        String key = null;
        for (Object data : datas) {
            if (data == null) {
                data = "";
            }

            // We received a key value in an non assiociative array, the first is a key, the second the value
            if (key == null) {
                key = String.valueOf(data);
            } else {
                event.putCustomAttribute(key, String.valueOf(data));
                key = null;
            }
        }

        Answers.getInstance().logCustom(event);
    }

}
