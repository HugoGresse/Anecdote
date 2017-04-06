package io.gresse.hugo.anecdote.anecdote;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.SaveAndShareAnecdoteEvent;
import io.gresse.hugo.anecdote.view.CustomImageView;

/**
 * Manage context/option on media custom action like long touch
 * <p>
 * Created by Hugo Gresse on 05/04/2017.
 */

public class MediaContextDialog {

    /**
     * Open a dialog to have custom action on a given creative
     *
     * @param context     the app context
     * @param websiteName the website name
     * @param anecdote    the anecdote
     * @param contentUrl  the media url
     * @param viewTouched the view touched (optional)
     */
    public static void openDialog(final Context context,
                                  final String websiteName,
                                  final Anecdote anecdote,
                                  final String contentUrl,
                                  @Nullable final View viewTouched) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(R.array.anecdote_content_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        EventBus.getDefault().post(new CopyAnecdoteEvent(websiteName, anecdote, CopyAnecdoteEvent.TYPE_MEDIA, contentUrl));
                        break;
                    case 1:
                        if (viewTouched instanceof CustomImageView) {
                            EventBus.getDefault().post(new SaveAndShareAnecdoteEvent(websiteName, anecdote, (CustomImageView) viewTouched));
                        }
                        break;
                    default:
                        Toast.makeText(context, R.string.not_implemented, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        builder.show();
    }

}
