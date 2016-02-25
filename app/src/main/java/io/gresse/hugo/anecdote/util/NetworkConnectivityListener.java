package io.gresse.hugo.anecdote.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * From http://www.netmite.com/android/mydroid/frameworks/base/core/java/android/net/NetworkConnectivityListener.java
 * Created by Hugo Gresse on 24/02/16.
 */
public class NetworkConnectivityListener {

    public static final String TAG = NetworkConnectivityListener.class.getSimpleName();

    private State                         mState;
    private ConnectivityBroadcastReceiver mReceiver;
    private Context                       mContext;
    @Nullable
    private ConnectivityListener          mListener;
    private Handler                       mMainHandler;
    private Runnable                      mConnectivityRunnable;

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || mListener == null) {
                return;
            }

            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            boolean networkAvailable = networkInfo != null && networkInfo.isConnected();

            if (networkAvailable) {
                mState = State.CONNECTED;
            } else {
                mState = State.NOT_CONNECTED;
            }

            mMainHandler.post(mConnectivityRunnable);
        }
    }

    public enum State {
        UNKNOWN,

        /**
         * This state is returned if there is connectivity to any network
         **/
        CONNECTED,
        /**
         * This state is returned if there is no connectivity to any network. This is set
         * to true under two circumstances:
         * <ul>
         * <li>When connectivity is lost to one network, and there is no other available
         * network to attempt to switch to.</li>
         * <li>When connectivity is lost to one network, and the attempt to switch to
         * another network fails.</li>
         */
        NOT_CONNECTED;
    }

    /**
     * Create a new NetworkConnectivityListener.
     */
    public NetworkConnectivityListener() {
        mState = State.UNKNOWN;
        mReceiver = new ConnectivityBroadcastReceiver();
        mConnectivityRunnable = new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onConnectivityChange(mState);
                }
            }
        };
    }

    /**
     * This method starts listening for network connectivity state changes.
     *
     * @param context app context
     */
    public synchronized void startListening(Context context, ConnectivityListener connectivityListener) {
        if (mListener == null) {
            mContext = context;
            mMainHandler = new Handler(context.getMainLooper());
            mListener = connectivityListener;

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(mReceiver, filter);
        }
    }

    /**
     * This method stops this class from listening for network changes.
     */
    public synchronized void stopListening() {
        if (mListener != null) {
            mContext.unregisterReceiver(mReceiver);
            mMainHandler = null;
            mContext = null;
            mListener = null;
        }
    }

    /**
     * Listener for connectivity change with simple state
     */
    public interface ConnectivityListener {
        void onConnectivityChange(State state);
    }


}
