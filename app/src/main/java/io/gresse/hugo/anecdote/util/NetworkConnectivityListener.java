package io.gresse.hugo.anecdote.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Listen to the default network and report simple connected/not connected changes on the main thread.
 * Created by Hugo Gresse on 24/02/16.
 */
public class NetworkConnectivityListener {

    public enum State {
        UNKNOWN,
        CONNECTED,
        NOT_CONNECTED
    }

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private ConnectivityManager                  mConnectivityManager;
    @Nullable
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    @Nullable
    private ConnectivityListener                 mListener;

    public synchronized void startListening(Context context, ConnectivityListener connectivityListener) {
        if (mListener != null) return;

        mListener = connectivityListener;
        mConnectivityManager = context.getApplicationContext().getSystemService(ConnectivityManager.class);
        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                dispatch(State.CONNECTED);
            }

            @Override
            public void onLost(@NonNull Network network) {
                dispatch(State.NOT_CONNECTED);
            }
        };

        if (!isConnected(mConnectivityManager)) {
            dispatch(State.NOT_CONNECTED);
        }
        mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
    }

    public synchronized void stopListening() {
        if (mListener == null) return;

        if (mConnectivityManager != null && mNetworkCallback != null) {
            mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        }
        mMainHandler.removeCallbacksAndMessages(null);
        mConnectivityManager = null;
        mNetworkCallback = null;
        mListener = null;
    }

    private void dispatch(State state) {
        mMainHandler.post(() -> {
            ConnectivityListener listener = mListener;
            if (listener != null) {
                listener.onConnectivityChange(state);
            }
        });
    }

    private static boolean isConnected(ConnectivityManager connectivityManager) {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public interface ConnectivityListener {
        void onConnectivityChange(State state);
    }
}
