package com.sometimestwo.moxie.Utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.sometimestwo.moxie.App;
import com.sometimestwo.moxie.OnTaskCompletedListener;
import com.sometimestwo.moxie.R;

import java.io.File;
import java.net.MalformedURLException;

public class DownloadService extends Service implements OnTaskCompletedListener {
    public static final String ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD";
    public static final String ACTION_CONTINUE_DOWNLOAD = "ACTION_CONTINUE_DOWNLOAD";
    public static final String ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD";
    private DownloadBinder downloadBinder = new DownloadBinder();
    // stores a filename to be given to a newly downloaded file
    private String mNewDownloadedFilename;
    // stores a "cleaned" version of a Vreddit URL
    private String mVredditUrlCleaned;

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        downloadBinder.getDownloadListener().setDownloadService(this);
        return downloadBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_PAUSE_DOWNLOAD.equals(action)) {
            downloadBinder.pauseDownload();
            Toast.makeText(getApplicationContext(), "Download is paused", Toast.LENGTH_LONG).show();
        } else if (ACTION_CANCEL_DOWNLOAD.equals(action)) {
            downloadBinder.cancelDownload();
            Toast.makeText(getApplicationContext(), "Download is canceled", Toast.LENGTH_LONG).show();
        } else if (ACTION_CONTINUE_DOWNLOAD.equals(action)) {
            downloadBinder.continueDownload();
            Toast.makeText(getApplicationContext(), "Download continue", Toast.LENGTH_LONG).show();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    // Scans a file with MediaScannerConnection so that the new file shows up in gallery.
    // Also Toast a success message.
    public void scanFile(String newlyCreatedFileName) {
        MediaScannerConnection.scanFile(this,
                new String[]{Constants.APP_DOWNLOAD_PATH + File.separator + newlyCreatedFileName}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast successToast = Toast.makeText(
                                        DownloadService.this,
                                        getResources().getString(R.string.toast_download_success),
                                        Toast.LENGTH_LONG);
                                successToast.show();
                            }
                        });
                        stopForeground(true);
                    }
                });
    }

    public void downloadVreddit(String vredditURL, String newFilename) {
        mNewDownloadedFilename = newFilename;
        mVredditUrlCleaned = Utils.getCleanVRedditDownloadUrl(vredditURL);
        new Utils.FetchVRedditGifTask(DownloadService.this, vredditURL, this).execute();
    }


    // This gets called from the Async task Utils.FetchVRedditGifTask upon completion.
    // The Vreddit video in question will have either been freshly downloaded (and cached)
    // or simply retrieved from cache. Either way we are provided the Uri of said video.
    @Override
    public void onVRedditMuxTaskCompleted(Uri uriToLoad) {
        try {
            File src = new File(
                    App.getProxy(this).getCacheFile(mVredditUrlCleaned).getAbsolutePath());
            // The location that we'll store the video at
            File dest = new File(Constants.APP_DOWNLOAD_PATH, mNewDownloadedFilename);

            // make sure we have the directory made to store downloads
            if (!dest.exists()) {
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                dest.createNewFile();
            }

            Utils.copy(src, dest);
            // finally, scan video so that our gallery app recognizes it
            scanFile(mNewDownloadedFilename);
        } catch (MalformedURLException badurle) {
            Log.e("DOWNLOAD_VREDDIT",
                    "MalformedURLException: Could not convert Uri to URI. Uri was: "
                            + uriToLoad.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
