package com.sometimestwo.jumble.Utils;

import android.app.Notification;
import android.os.Binder;
import android.text.TextUtils;


public class DownloadBinder extends Binder {

    private DownloadManager downloadManager = null;
    private DownloadListener downloadListener = null;
    private String currDownloadUrl = "";
    private String newFilename = Constants.DEFAULT_DOWNLOAD_FILENAME;

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public DownloadBinder() {
        if(downloadListener == null)
        {
            downloadListener = new DownloadListener();
        }
    }

    // If vreddit video
    public void startDownloadVreddit(String postUrl, String newFilename){
        postUrl = Utils.getCleanVRedditDownloadUrl(postUrl);
        downloadListener.downloadVreddit(postUrl, newFilename);
    }

    // Anything that's non-vreddit video
    public void startDownload(String downloadUrl,String newFilename,int progress )
    {
        /* Because downloadManager is a subclass of AsyncTask, and AsyncTask can only be executed once,
         * So each download need a new downloadManager. */
        downloadManager = new DownloadManager(downloadListener,newFilename);

        /* Because DownloadUtil has a static variable of downloadManger, so each download need to use new downloadManager. */
        DownloadUtil.setDownloadManager(downloadManager);

        // Will need to know the name of the newly created file later
        downloadListener.setNewFilePath(newFilename);

        // Execute download manager, this will invoke downloadManager's doInBackground() method.
        downloadManager.execute(downloadUrl);

        // Save current download file url.
        currDownloadUrl = downloadUrl;

        // Create and start foreground service with notification.
        Notification notification = downloadListener.getDownloadNotification("Downloading...", progress);
        downloadListener.getDownloadService().startForeground(1, notification);

    }

    public void continueDownload()
    {
        if(currDownloadUrl != null && !TextUtils.isEmpty(currDownloadUrl))
        {
            int lastDownloadProgress = downloadManager.getLastDownloadProgress();
            startDownload(currDownloadUrl, newFilename, lastDownloadProgress);
        }
    }

    public void cancelDownload()
    {
        downloadManager.cancelDownload();
    }

    public void pauseDownload()
    {
        downloadManager.pauseDownload();
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }
}