package com.sometimestwo.moxie;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.sometimestwo.moxie.Utils.Constants;

import java.lang.ref.WeakReference;


public class ActivityMain extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private SharedPreferences prefs_settings;
    private SharedPreferences.Editor prefs_settings_editor;
    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.drawer_layout);
        setContentView(R.layout.activity_main);

        /* Set default settings/preferences if never initialized.
         *
         * This is more of a precautionary mesaure to ensure we don't find null preferences later.
         */
        prefs_settings = this.getSharedPreferences(Constants.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        prefs_settings_editor =
                this.getSharedPreferences(Constants.KEY_SHARED_PREFS, Context.MODE_PRIVATE)
                        .edit();

        // Hover previewer size - defaults to small
        if (prefs_settings.getString(Constants.PREFS_PREVIEW_SIZE, null) == null) {
            prefs_settings_editor.putString(Constants.PREFS_PREVIEW_SIZE, Constants.PREFS_PREVIEW_SIZE_LARGE);
        }

        // Optimize filter
        if (prefs_settings.getBoolean(Constants.PREFS_FILTER_OPTIMIZE, true)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_FILTER_OPTIMIZE, true);
        }

        // Allow NSFW
        if (prefs_settings.getBoolean(Constants.PREFS_HIDE_NSFW, true)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_HIDE_NSFW, true);
        }

        // Hide NSFW thumbs
        if (!prefs_settings.getBoolean(Constants.PREFS_HIDE_NSFW_THUMBS, false)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_HIDE_NSFW_THUMBS, false);
        }

        // Allow hover previewer - default to yes
        if (prefs_settings.getBoolean(Constants.PREFS_ALLOW_HOVER_PREVIEW, true)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_HOVER_PREVIEW, true);
        }

        // Allow tap-to-close big display
        if (prefs_settings.getBoolean(Constants.PREFS_ALLOW_CLOSE_CLICK, true)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_CLOSE_CLICK, true);
        }

        // Allow domain icons - defaults to false
        if (!prefs_settings.getBoolean(Constants.PREFS_ALLOW_DOMAIN_ICON, false)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_DOMAIN_ICON, false);
        }

        // Allow filetype icons - defaults to false
        if (!prefs_settings.getBoolean(Constants.PREFS_ALLOW_FILETYPE_ICON, false)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_FILETYPE_ICON, false);
        }

        // Show NSFW icon on NSFW submissions
        if (!prefs_settings.getBoolean(Constants.PREFS_SHOW_NSFW_ICON, false)) {
            prefs_settings_editor.putBoolean(Constants.PREFS_SHOW_NSFW_ICON, false);
        }

        prefs_settings_editor.commit();

        new FetchRedditUser(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class FetchRedditUser extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<ActivityMain> activity;

        public FetchRedditUser(ActivityMain activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Load most recently logged in user
                String mostRecentUser =
                        prefs_settings.getString(Constants.MOST_RECENT_USER, Constants.USERNAME_USERLESS);

                if (!Constants.USERNAME_USERLESS.equalsIgnoreCase(mostRecentUser)) {
                    App.getAccountHelper().switchToUser(mostRecentUser);
                } else {
                    App.getAccountHelper().switchToUserless();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to switch to most recent user. Defaulting to Guest");
                App.getAccountHelper().switchToUserless();
                return true;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            Activity activity = this.activity.get();
            if (activity != null) {
                activity.startActivity(new Intent(activity, ActivityHome.class));
            }
        }
    }
}
