package io.gresse.hugo.anecdote.tracking;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.HashMap;

import ly.count.android.sdk.Countly;
import ly.count.android.sdk.DeviceId;

/**
 * Event related utils
 * <p/>
 * Created by Hugo Gresse on 25/04/16.
 */
public class EventSender implements EventSenderInterface {

    public EventSender(Context context) {
        Countly.sharedInstance().init(context, "http://anecdote-countly.gresse.io", "aac7c73310148c3f336ec545cce203c78246e608", null, DeviceId.Type.OPEN_UDID)
                                .setViewTracking(true)
                                .enableCrashReporting();
    }

    @Override
    public void onStart(Activity activity) {
        Countly.sharedInstance().onStart(activity);
    }

    @Override
    public void onStop() {
        Countly.sharedInstance().onStop();
    }

    @Override
    public void sendView(String name, String type) {
        Countly.sharedInstance().recordView(name);
    }

    @Override
    public void sendEvent(String eventName, @Nullable Object... datas) {
        HashMap<String, String> segmentation = new HashMap<>();
        if(datas != null){
            String key = null;
            for (Object data : datas) {
                if (data == null) {
                    data = "";
                }

                // We received a key value in an non assiociative array, the first is a key, the second the value
                if (key == null) {
                    key = String.valueOf(data);
                } else {
                    segmentation.put(key, String.valueOf(data));
                    key = null;
                }
            }
        }

        if(segmentation.isEmpty()){
            Countly.sharedInstance().recordEvent(eventName, 1);
        } else {
            Countly.sharedInstance().recordEvent(eventName, segmentation, 1);
        }
    }

}
