package com.sometimestwo.moxie.Utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ProgressBar;

import com.coremedia.iso.boxes.Container;
import com.google.gson.reflect.TypeToken;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.sometimestwo.moxie.API.GfycatAPI;
import com.sometimestwo.moxie.App;
import com.sometimestwo.moxie.Imgur.client.ImgurClient;
import com.sometimestwo.moxie.Imgur.response.images.ImgurSubmission;
import com.sometimestwo.moxie.Imgur.response.images.SubmissionRoot;
import com.sometimestwo.moxie.Model.GfyItem;
import com.sometimestwo.moxie.Model.GfycatWrapper;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.EventListeners.OnRedditTaskListener;
import com.sometimestwo.moxie.EventListeners.RedditHeartbeatListener;
import com.sometimestwo.moxie.EventListeners.OnTaskCompletedListener;
import com.sometimestwo.moxie.EventListeners.OnVRedditTaskCompletedListener;
import com.sometimestwo.moxie.StringListIgnoreCase;


import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.pagination.BarebonesPaginator;
import net.dean.jraw.pagination.Paginator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utils {

    /* Takes a string and makes it cute: CONTROVERSIAL -> Controversial */
    public static String makeTextCute(String ugly) {
        if (ugly != null && ugly.length() > 0) {
            return ugly.substring(0, 1).toUpperCase() + ugly.substring(1).toLowerCase();
        }
        return ugly;
    }

    /* Truncates a large number such as 14,303 and
    converts it to a truncated string: "14.3k"*/
    // 14925 -> 14.9k
    // 101231 -> 100.2k
    public static String truncateCount(int count) {
        int left, right;
        if (count > 9999) {
            left = count / 1000;
            right = (count % 1000) / 100;
            return String.valueOf(left) + "." + String.valueOf(right) + "k";
        }
        return String.valueOf(count);
    }

    /*
       imgur links will be given in the following format :
       https://i.imgur.com/CtyvHl6.gifv
     */
    public static String getFileExtensionFromUrl(String postURL) {
        String split[] = postURL.split("\\.");
        return split.length > 0 ? split[split.length - 1] : "";
    }

    public static Constants.SubmissionType getSubmissionType(String url) {

        if (url != null) {
            url = url.toLowerCase().trim();
            if (url.contains(".gifv") || url.contains(".gif")) {
                return Constants.SubmissionType.GIF;
            } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
                return Constants.SubmissionType.IMAGE;
            } else if (url.contains("youtube") || url.contains("youtu.be")) {
                return Constants.SubmissionType.VIDEO;
            }
        }
        return null;
    }

    // Assuming we get an Imgur link in one of the following formats:
    // 1. https://i.imgur.com/4RxPsWI.gifv
    // 2. https://i.imgur.com/4RxPsWI
    //
    // Return: 4RxPsWI
    public static String getImgurHash(String imgurLink) {
        String split[] = imgurLink.split("/");
        String res = split[split.length - 1];
        if (res.contains(".")) {
            res = res.split("\\.")[0];
        }
        return res;
    }

    // TODO Verify this is the only qualification...
    public static boolean isImgurAlbum(String url) {
        return url.contains("/a/");
    }

    /*
       Takes indirect Imgur url such as https://imgur.com/7Ogk88I, fetches direct link from
       Imgur API, and sets item's URL to direct link.
       We also are setting the item's submission type here. Might need to do this elsewhere.
    */
    public static void fixIndirectImgurUrl(SubmissionObj item,
                                           String imgurHash,
                                           OnTaskCompletedListener listener) {
        ImgurClient imgurClient = new ImgurClient();

        imgurClient.getImageService()
                .getImageByHash(imgurHash)
                .subscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<SubmissionRoot>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(SubmissionRoot submissionRoot) {
                        ImgurSubmission imgurSubmissionData = submissionRoot.getImgurSubmissionData();
                        // ImgurSubmission can be one of two things:
                        // 1. Image. Will not contain any mp4 data
                        // 2. Gif. Will contain mp4 link

                        // image
                        if (imgurSubmissionData.getMp4() == null) {
                            item.setCleanedUrl(imgurSubmissionData.getLink());
                        }
                        // gif
                        //TODO: THIS SHOULD NEVER HAPPEN! We assumed that only indirect imgur URLs
                        //      are calling this function. We also assumed that indirect imgur URLs
                        //      imply IMAGE (not gif/video)
                        else {
                            item.setCleanedUrl(imgurSubmissionData.getMp4());
                            Log.e("IMGUR_GIF_WTF",
                                    "Found a gif/mp4 file when fetching an indirect url fix!");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        listener.downloadSuccess();
                        //item.setLoadingData(false);
                    }
                });
    }

    /*

        Given a imgur link ending with .gif/gifv such as https://i.imgur.com/4RxPsWI.gifv,
        retrieve corresponding .mp4 link: https://i.imgur.com/4RxPsWI.mp4 and set item's
        URL to new .mp4 link.
     */
    public static void getMp4LinkImgur(SubmissionObj item, String imgurHash, OnTaskCompletedListener listener) {
        ImgurClient imgurClient = new ImgurClient();
        //UjpwIRe is a 404 gifv hash
        imgurClient.getImageService()
                .getImageByHash(imgurHash)
                .subscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<SubmissionRoot>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(SubmissionRoot submissionRoot) {
                        ImgurSubmission imgurSubmissionData = submissionRoot.getImgurSubmissionData();
                        // item.setUrl(imgurSubmissionData.getMp4());
                        item.setCleanedUrl(imgurSubmissionData.getMp4());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ERROR_IMGUR_FETCH", "Failed to retrieve imgur hash: " + imgurHash);
                        e.printStackTrace();
                        listener.downloadFailure();
                    }

                    @Override
                    public void onComplete() {
                        listener.downloadSuccess();
                    }
                });
    }

    public static String getGfycatHash(String gfycatUrl) {
        String hash = gfycatUrl.substring(gfycatUrl.lastIndexOf("/", gfycatUrl.length()));
        if (hash.contains("-size_restricted")) {
            hash = hash.replace("-size_restricted", "");
        }
        if (hash.contains("?autoplay=enabled")) {
            hash = hash.replace("?autoplay=enabled", "");
        }
        // remove trailing slash
        return hash.substring(1);
        //return gfycatUrl.substring(gfycatUrl.lastIndexOf("/", gfycatUrl.length()));
    }

    public static void getGfycat(String gfycatHash, SubmissionObj item) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_GFYCAT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GfycatAPI gfycatAPI = retrofit.create(GfycatAPI.class);
        gfycatAPI.getGfycat(gfycatHash);
        Call<GfycatWrapper> call = gfycatAPI.getGfycat(gfycatHash);
        call.enqueue(new Callback<GfycatWrapper>() {
            @Override
            public void onResponse(Call<GfycatWrapper> call, Response<GfycatWrapper> response) {
                //Log.d(TAG, "onResponse: feed: " + response.body().toString());
                Log.d("GFYCAT_RESPONSE",
                        "getGfycat onResponse: Server Response: " + response.toString());

                GfyItem gfyItem = new GfyItem();
                try {
                    gfyItem = response.body().getGfyItem();
                } catch (Exception e) {
                    Log.e("GFYCAT_RESPONSE_ERROR",
                            "Failed in attempt to retrieve gfycat object for hash "
                                    + gfycatHash + ". "
                                    + e.getMessage());
                    call.cancel();
                }
                if (gfyItem != null) {
                    item.setCleanedUrl(gfyItem.getMobileUrl() != null ? gfyItem.getMobileUrl() : gfyItem.getMp4Url());
                    item.setMp4Url(gfyItem.getMp4Url());
                }
            }

            @Override
            public void onFailure(Call<GfycatWrapper> call, Throwable t) {
                call.cancel();
                Log.e("GETGFYCAT_ERROR",
                        "getGfycat onFailure: Unable to retrieve Gfycat: " + t.getMessage());
            }

        });
    }

    public static String getYouTubeID(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static boolean mux(String videoFile, String audioFile, String outputFile) {
        com.googlecode.mp4parser.authoring.Movie video;
        try {
            new MovieCreator();
            video = MovieCreator.build(videoFile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        com.googlecode.mp4parser.authoring.Movie audio;
        try {
            new MovieCreator();
            audio = MovieCreator.build(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        com.googlecode.mp4parser.authoring.Track audioTrack = audio.getTracks().get(0);

        CroppedTrack croppedTrack = new CroppedTrack(audioTrack, 0, audioTrack.getSamples().size());
        video.addTrack(croppedTrack);
        Container out = new DefaultMp4Builder().build(video);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        BufferedWritableFileByteChannel byteBufferByteChannel =
                new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            Log.e("Utils.mux()", "Error! Missed close()!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Code from https://stackoverflow.com/a/9293885/3697225
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static String getCleanVRedditDownloadUrl(String url) {
        if (!url.contains("DASH")) {
            if (url.endsWith("/")) {
                url = url.substring(url.length() - 2);
            }
            url = url + "/DASH_9_6_M";
        }
        return url;
    }

    private static class BufferedWritableFileByteChannel implements WritableByteChannel {
        private static final int BUFFER_CAPACITY = 1000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() {
            dumpToFile();
            isOpen = false;
        }

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // stores the current user's subscriptions in shared preferences
    public static void storeLocalUserSubscriptions(StringListIgnoreCase subs){
        String subsStr = App.getGsonApp().toJson(subs);
        App.getSharedPrefs().edit().putString(Constants.PREFS_CURR_USER_SUBS,subsStr).commit();
    }

    public static void addToLocalUserSubscriptions(String newSub){
        StringListIgnoreCase currSubs = getCurrUserLocalSubscriptions();
        if(!currSubs.contains(newSub)) {
            currSubs.add(newSub);
            storeLocalUserSubscriptions(currSubs);
        }
    }

    public static StringListIgnoreCase getCurrUserLocalSubscriptions(){
        StringListIgnoreCase userSubs = new StringListIgnoreCase();

        String subsStr = App.getSharedPrefs().getString(Constants.PREFS_CURR_USER_SUBS,null);
        userSubs.addAll(App.getGsonApp().fromJson(subsStr,new TypeToken<StringListIgnoreCase>(){}.getType()));

        return userSubs;
    }



    public static void removeFromLocalUserSubscriptions(String subToRemove){
        StringListIgnoreCase currSubs = getCurrUserLocalSubscriptions();
        if(currSubs.contains(subToRemove)) {
            currSubs.remove(subToRemove);
            storeLocalUserSubscriptions(currSubs);
        }
    }

    // removes all subscriptions info from shared prefs
    public static void removeLocalSubscriptionsList(){
        App.getSharedPrefs().edit().putString(Constants.PREFS_CURR_USER_SUBS,"").commit();
    }

    // Updates our shared preferences with a list of logged in user's subreddit subscriptions
    public static class FetchUserSubscriptionsAndStoreLocally extends AsyncTask<Void, Void, Void>{
        String username;

        public FetchUserSubscriptionsAndStoreLocally(String username) {
            this.username = username;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            RedditClient redditClient = App.getAccountHelper().switchToUser(username);
            // get the list of the new user's subscriptions
            BarebonesPaginator<Subreddit> paginator = redditClient.me()
                    .subreddits("subscriber")
                    .limit(Paginator.RECOMMENDED_MAX_LIMIT)
                    .build();

            StringListIgnoreCase listOfSubredditNames = new StringListIgnoreCase();
            // Paginator implements Iterable
            for (Listing<Subreddit> page : paginator) {
                for(Subreddit s : page){
                    listOfSubredditNames.add(s.getName());
                }
            }
            storeLocalUserSubscriptions(listOfSubredditNames);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public static class SubscribeSubredditTask extends AsyncTask<Void,Void,Void>{
        String subreddit;
        OnRedditTaskListener listener;

        public SubscribeSubredditTask(String subreddit, OnRedditTaskListener listener) {
            this.subreddit = subreddit;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            RedditClient redditClient = App.getAccountHelper().getReddit();
            redditClient.subreddit(subreddit).subscribe();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listener.onSuccess();
            super.onPostExecute(aVoid);
        }

    }

    public static class UnsubscribeSubredditTask extends AsyncTask<Void,Void,Boolean>{
        String subreddit;
        OnRedditTaskListener listener;
        public UnsubscribeSubredditTask(String subreddit, OnRedditTaskListener listener) {
            this.subreddit = subreddit;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                RedditClient redditClient = App.getAccountHelper().getReddit();
                redditClient.subreddit(subreddit).unsubscribe();
            }
            catch (Exception e){
                listener.onFailure(e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success)
                listener.onSuccess();

            super.onPostExecute(success);
        }

    }

    /***********[V.REDDIT SPECIFIC FUNCTION] ***************/
    public static class FetchVRedditGifTask extends AsyncTask<String, Void, String> {
        private Context context;
        String url;
        private OnVRedditTaskCompletedListener listener;

        public FetchVRedditGifTask(Context context, String dirtyUrl, OnVRedditTaskCompletedListener listener) {
            this.listener = listener;
            this.url = dirtyUrl;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            url = getCleanVRedditDownloadUrl(url);

            File videoFile = App.getProxy(context).getCacheFile(url);

            if (videoFile.length() <= 0) {
                try {
                    if (!videoFile.exists()) {
                        if (!videoFile.getParentFile().exists()) {
                            videoFile.getParentFile().mkdirs();
                        }
                        videoFile.createNewFile();
                    }

                    HttpURLConnection conv = (HttpURLConnection) (new URL(url)).openConnection();
                    conv.setRequestMethod("GET");
                    conv.connect();

                    String downloadsPath = context.getCacheDir().getAbsolutePath();
                    String fileName = "video.mp4"; //temporary location for video
                    File videoOutput = new File(downloadsPath, fileName);
                    HttpURLConnection cona = (HttpURLConnection) new URL(
                            url.toString().substring(0, url.lastIndexOf("/") + 1)
                                    + "audio").openConnection();
                    cona.setRequestMethod("GET");

                    if (!videoOutput.exists()) {
                        videoOutput.createNewFile();
                    }

                    FileOutputStream fos = new FileOutputStream(videoOutput);
                    InputStream is = conv.getInputStream();
                    int fileLength = conv.getContentLength() + cona.getContentLength();

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = is.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            is.close();
                        }
                        total += count;
                        fos.write(data, 0, count);
                    }
                    fos.close();
                    is.close();

                    cona.connect();

                    String fileNameAudio = "audio.mp4"; //temporary location for audio
                    File audioOutput = new File(downloadsPath, fileNameAudio);
                    File muxedPath = new File(downloadsPath, "muxedvideo.mp4");
                    muxedPath.createNewFile();

                    if (!audioOutput.exists()) {
                        audioOutput.createNewFile();
                    }

                    fos = new FileOutputStream(audioOutput);

                    int stat = cona.getResponseCode();
                    if (stat != 403) {
                        InputStream isa = cona.getInputStream();

                        byte dataa[] = new byte[4096];
                        int counta;
                        while ((counta = isa.read(dataa)) != -1) {
                            // allow canceling with back button
                            if (isCancelled()) {
                                isa.close();
                            }
                            total += counta;

                            fos.write(dataa, 0, counta);
                        }
                        fos.close();
                        isa.close();

                        //TODO:
                        //W/OkHttpClient: A connection to https://v.redd.it/ was leaked. Did you forget to close a response body?
                        cona.disconnect();

                        Utils.mux(videoOutput.getAbsolutePath(), audioOutput.getAbsolutePath(),
                                muxedPath.getAbsolutePath());

                        Utils.copy(muxedPath, videoFile);
                        new File(videoFile.getAbsolutePath() + ".a").createNewFile();
                        //setMuteVisibility(true);

                    } else {
                        Utils.copy(videoOutput, videoFile);
                        //no audio!
                        //setMuteVisibility(false);
                    }
                } catch (Exception e) {
                    Log.e("FetchVRedditGifTask", "Error! Missed close()!");
                    e.printStackTrace();
                }

            }
            // found in cache???
            else {
                File isAudio = new File(videoFile.getAbsolutePath() + ".a");
                if (isAudio.exists()) {
                    //setMuteVisibility(true);
                }
            }
            String toLoad = App.getProxy(context).getCacheFile(url).getAbsolutePath();

            return toLoad;
        }

        @Override
        protected void onPostExecute(String urlToLoad) {
            listener.onVRedditMuxTaskCompleted(Uri.parse(urlToLoad));
        }
    }

    public static class SaveSubmissionTask extends AsyncTask<Void, Void, Void> {
        SubmissionObj currSubmission;
        OnVRedditTaskCompletedListener listener;

        public SaveSubmissionTask(SubmissionObj currSubmission, OnVRedditTaskCompletedListener listener) {
            super();
            this.currSubmission = currSubmission;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String submissionId = currSubmission.getId();
                boolean isSaved = App.getAccountHelper().getReddit().submission(submissionId).inspect().isSaved();
                App.getAccountHelper().getReddit().submission(submissionId).setSaved(!isSaved);
            } catch (Exception e) {
                Log.e("SAVE_SUBMISSION_ERROR",
                        "Could not save submission " + currSubmission.getId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public static class VoteSubmissionTask extends AsyncTask<Void, Void, Void> {
        SubmissionObj currSubmission;
        OnVRedditTaskCompletedListener listener;
        VoteDirection voteDirection;

        public VoteSubmissionTask(SubmissionObj currSubmission,
                                  OnVRedditTaskCompletedListener listener,
                                  VoteDirection voteDirection) {
            this.currSubmission = currSubmission;
            this.listener = listener;
            this.voteDirection = voteDirection;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String submissionID = currSubmission.getId();
            try {
                App.getAccountHelper().getReddit().submission(submissionID).setVote(voteDirection);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("VOTE FAILURE", "Failed to set vote for submission: " + submissionID);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }

    public static class RedditHeartbeatTask extends AsyncTask<Void, Void, Void> {
        String mostRecentUser;
        RedditHeartbeatListener listener;
        boolean didSomething = false;

        public RedditHeartbeatTask(RedditHeartbeatListener listener) {
            this.mostRecentUser = App.getSharedPrefs()
                    .getString(Constants.MOST_RECENT_USER, Constants.USERNAME_USERLESS);
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!App.getAccountHelper().isAuthenticated()) {
                didSomething = true;
                Log.e("FETCH_AUTH_USER", "Attempting to reauthenticate: " + mostRecentUser);
                if (!Constants.USERNAME_USERLESS.equalsIgnoreCase(mostRecentUser)) {
                    App.getAccountHelper().switchToUser(mostRecentUser);
                } else {
                    App.getAccountHelper().switchToUserless();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (didSomething)
                Log.e("FETCH_AUTH_USER", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Authenticated success! ");
            listener.redditUserAuthenticated();
        }
    }

    /* Check for internet connection*/
    // stolen from : https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out/27312494#27312494
    public static class InternetCheck extends AsyncTask<Void, Void, Boolean> {

        private Consumer mConsumer;

        public interface Consumer {
            void accept(Boolean internet);
        }

        public InternetCheck(Consumer consumer) {
            mConsumer = consumer;
            execute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
                sock.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean internet) {
            mConsumer.accept(internet);
        }
    }

    public static boolean hasDownloadPermissions(Fragment fragment) {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // Here, thisActivity is the current activity
        fragment.requestPermissions(permissions, Constants.PERMISSIONS_DOWNLOAD_MEDIA);
        return true;
    }

    public static boolean hasDownloadPermissions(Activity activity) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
/*            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {}*/
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.PERMISSIONS_DOWNLOAD_MEDIA);

        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }
}
