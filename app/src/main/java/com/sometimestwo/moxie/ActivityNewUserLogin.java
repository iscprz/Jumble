package com.sometimestwo.moxie;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.oauth.OAuthException;
import net.dean.jraw.oauth.StatefulAuthHelper;
import net.dean.jraw.pagination.BarebonesPaginator;
import net.dean.jraw.pagination.Paginator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ActivityNewUserLogin extends AppCompatActivity {
    private SharedPreferences prefs_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
       // prefs_settings = this.getSharedPreferences(Constants.KEY_SHARED_PREFS, Context.MODE_PRIVATE);

        // Don't save any cookies, cache, or history from previous sessions. If we don't do this,
        // once the first user logs in and authenticates, the next time we go to add a new user,
        // the first user will be automatically logged in, which is not what we want.
        final WebView webView = findViewById(R.id.webview_login);
        webView.clearCache(true);
        webView.clearHistory();

        // Stolen from https://github.com/ccrama/Slide/blob/a2184269/app/src/main/java/me/ccrama/redditslide/Activities/Login.java#L92
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }

        // Get a StatefulAuthHelper instance to manage interactive authentication
        final StatefulAuthHelper helper = App.getAccountHelper().switchToNewUser();

        // Watch for pages loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (helper.isFinalRedirectUrl(url)) {
                    // No need to continue loading, we've already got all the required information
                    webView.stopLoading();
                    webView.setVisibility(View.GONE);

                    // Try to authenticate the user
                    new AuthenticateTask(ActivityNewUserLogin.this, helper).execute(url);
                }
            }
        });

        // Generate an authentication URL
        boolean requestRefreshToken = true;
        boolean useMobileSite = true;
        String[] scopes = new String[]{
                "read",
                "identity",
                "vote",
                "history",
                "save",
                "subscribe",
                "mysubreddits"};
        String authUrl = helper.getAuthorizationUrl(requestRefreshToken, useMobileSite, scopes);

        // Finally, show the authorization URL to the user
        webView.loadUrl(authUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * An async task that takes a final redirect URL as a parameter and reports the success of
     * authorizing the user. We also build a list of the new user's subreddits and save them
     * in shared preferences for later
     */
    private static final class AuthenticateTask extends AsyncTask<String, Void, Boolean> {
        // Use a WeakReference so that we don't leak a Context
        private final WeakReference<Activity> context;
        private final StatefulAuthHelper helper;

        AuthenticateTask(Activity context, StatefulAuthHelper helper) {
            this.context = new WeakReference<>(context);
            this.helper = helper;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                helper.onUserChallenge(urls[0]);

                BarebonesPaginator<Subreddit> paginator = App.getAccountHelper().getReddit().me()
                        .subreddits("subscriber")
                        .limit(Paginator.RECOMMENDED_MAX_LIMIT)
                        .build();

                ArrayList<String> listOfSubredditNames = new ArrayList<String>();

                for (Listing<Subreddit> page : paginator) {
                    for(Subreddit s : page){
                        listOfSubredditNames.add(s.getName());
                    }
                }

                storeUserSubscriptions(listOfSubredditNames);
                return true;
            } catch (OAuthException e) {
                // Report failure if an OAuthException occurs
                return false;
            }
        }

        // stores the current user's subscriptions in shared preferences
        private void storeUserSubscriptions(ArrayList<String> subs){
            String subsStr = App.getGsonApp().toJson(subs);
            App.getSharedPrefs().edit().putString(Constants.PREFS_CURR_USER_SUBS,subsStr).commit();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // Finish the activity if it's still running
            Activity host = this.context.get();
            if (host != null) {
                host.setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED, new Intent());
                host.finish();
            }
        }
    }
}
