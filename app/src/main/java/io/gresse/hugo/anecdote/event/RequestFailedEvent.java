package io.gresse.hugo.anecdote.event;

import android.content.Context;
import android.support.annotation.Nullable;

import io.gresse.hugo.anecdote.R;

/**
 * When a request failed
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class RequestFailedEvent implements Event {

    public static final int ERROR_GENERAL      = 0;
    public static final int ERROR_LOADFAIL     = 1;
    public static final int ERROR_PROCESSING   = 2;
    public static final int ERROR_RESPONSEFAIL = 3;
    public static final int ERROR_PARSING      = 4;
    public static final int ERROR_NOINTERNET   = 5;
    public static final int ERROR_SERVER       = 6;
    public static final int ERROR_WRONGCONFIG  = 7;

    public  Event     originalEvent;
    public  int       error;
    private String    websiteName;
    @Nullable
    public  Exception exception;
    public  String    additionalError;

    public RequestFailedEvent(Event originalEvent,
                              int error,
                              String websiteName,
                              @Nullable Exception exception) {
        this(originalEvent, error, websiteName, exception, null);

    }

    public RequestFailedEvent(Event originalEvent,
                              int error,
                              String websiteName,
                              @Nullable Exception exception,
                              @Nullable String additionalError) {
        this.originalEvent = originalEvent;
        this.error = error;
        this.websiteName = websiteName;
        this.additionalError = additionalError;
        this.exception = exception;
    }

    /**
     * Format the current fail event to return an human readable message
     * @param context app context to get resources from
     * @return the human readable error message
     */
    public String formatErrorMessage(Context context){
        switch (error){
            default:
            case ERROR_GENERAL:
                return context.getResources().getString(R.string.error_general);
            case ERROR_NOINTERNET:
                return context.getResources().getString(R.string.error_nointernet);
            case ERROR_SERVER:
                return context.getResources().getString(R.string.error_server);
            case ERROR_WRONGCONFIG:
                return context.getResources().getString(R.string.error_wrongconfig);
            case ERROR_LOADFAIL:
                return context.getResources().getString(R.string.error_loadfail, websiteName);
            case ERROR_PROCESSING:
                return context.getResources().getString(R.string.error_processing);
            case ERROR_RESPONSEFAIL:
                return context.getResources().getString(R.string.error_responsefail, additionalError);
            case ERROR_PARSING:
                return context.getResources().getString(R.string.error_parsing, websiteName);
        }
    }

    @Override
    public String toString() {
        return "RequestFailedEvent{" +
                "error='" + error + '\'' +
                ", websiteName=" + websiteName + " " +
                ", exception=" + exception +
                '}';
    }
}
