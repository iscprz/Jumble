package com.sometimestwo.moxie;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.sometimestwo.moxie.Model.MoxieInfoObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.VideoCacher.HttpProxyCacheServer;

import net.dean.jraw.android.AndroidHelper;
import net.dean.jraw.android.AppInfoProvider;
import net.dean.jraw.android.ManifestAppInfoProvider;
import net.dean.jraw.android.SharedPreferencesTokenStore;
import net.dean.jraw.android.SimpleAndroidLogAdapter;
import net.dean.jraw.http.LogAdapter;
import net.dean.jraw.http.SimpleHttpLogger;
import net.dean.jraw.oauth.AccountHelper;

import java.util.UUID;

public final class App extends Application {
    private static AccountHelper accountHelper;
    private static SharedPreferencesTokenStore tokenStore;
    private static MoxieInfoObj currSubredditObj;
    private static SharedPreferences shared_prefs;
    public static HttpProxyCacheServer proxy;
    public static RequestManager GlideApp;
    @Override
    public void onCreate() {
        super.onCreate();

        // Get UserAgent and OAuth2 data from AndroidManifest.xml
        AppInfoProvider provider = new ManifestAppInfoProvider(getApplicationContext());

        // Ideally, this should be unique to every device
        UUID deviceUuid = UUID.randomUUID();

        // represents info about a subreddit we're currently exploring
        currSubredditObj = new MoxieInfoObj();

        // Store our access tokens and refresh tokens in shared preferences
        tokenStore = new SharedPreferencesTokenStore(getApplicationContext());
        // Load stored tokens into memory
        tokenStore.load();
        // Automatically save new tokens as they arrive
        tokenStore.setAutoPersist(true);

        // An AccountHelper manages switching between accounts and into/out of userless mode.
        accountHelper = AndroidHelper.accountHelper(provider, deviceUuid, tokenStore);

        // Every time we use the AccountHelper to switch between accounts (from one account to
        // another, or into/out of userless mode), call this function
        accountHelper.onSwitch(redditClient -> {
            // By default, JRAW logs HTTP activity to System.out. We're going to use Log.i()
            // instead.
            LogAdapter logAdapter = new SimpleAndroidLogAdapter(Log.INFO);

            // We're going to use the LogAdapter to write down the summaries produced by
            // SimpleHttpLogger
            redditClient.setLogger(
                    new SimpleHttpLogger(SimpleHttpLogger.DEFAULT_LINE_LENGTH, logAdapter));

            // If you want to disable logging, use a NoopHttpLogger instead:
            // redditClient.setLogger(new NoopHttpLogger());

            shared_prefs = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
            return null;
        });

        proxy = new HttpProxyCacheServer.Builder(this).maxCacheSize(5 * 1024)
                .maxCacheFilesCount(20)
                .build();

        // Glide
        GlideApp = Glide.with(this);

    }

    public static AccountHelper getAccountHelper() { return accountHelper; }
    public static SharedPreferencesTokenStore getTokenStore() { return tokenStore; }
    public static MoxieInfoObj getMoxieInfoObj() {return currSubredditObj;}
    public static SharedPreferences getSharedPrefs(){return shared_prefs;};
    public static RequestManager getGlideApp() { return GlideApp; }

    // Video cache
    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }
}
