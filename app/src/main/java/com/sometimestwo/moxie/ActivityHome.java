package com.sometimestwo.moxie;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;

import com.sometimestwo.moxie.EventListeners.HomeEventListener;
import com.sometimestwo.moxie.EventListeners.OnCloseClickEventListener;
import com.sometimestwo.moxie.Utils.Constants;


public class ActivityHome extends AppCompatActivity implements HomeEventListener,
        OnCloseClickEventListener,
        Fragment404.Fragment404EventListener,
        FragmentFullDisplay.OnCommentsEventListener {

    private final String TAG = ActivityHome.class.getSimpleName();

    //screen size metrics for flexibility in displaying dialogs
    private DisplayMetrics mDisplayMetrics;
    private int mScreenWidth;
    private int mScreenHeight;
    //For handling back button press. Need to close comments view (not fragment)
    // which resides inside the FullDisplayer fragment.
    private boolean mCommentsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        loadReddit(false);
    }

    private void loadReddit(boolean invalidateData) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment homeFragment = (FragmentHome) fm.findFragmentByTag(Constants.TAG_FRAG_HOME);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (homeFragment == null || invalidateData) {
            homeFragment = FragmentHome.newInstance();
            Bundle args = new Bundle();
            args.putBoolean(Constants.ARGS_INVALIDATE_DATASOURCE, invalidateData);
            homeFragment.setArguments(args);
            fm.beginTransaction().add(R.id.fragment_container_home, homeFragment, Constants.TAG_FRAG_HOME).commit();
        }
    }

    // Actually removes current fragment and creates new one
    protected void refreshFragment(String fragmentTag, boolean invalidateData) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        try {
            ft.remove(fragment).commit();
            loadReddit(invalidateData);
        } catch (NullPointerException e) {
            throw new NullPointerException(this.toString()
                    + ". Could not refresh fragment! Probably provided incorrect fragment tag. " +
                    " Fragment tag provided: " + fragmentTag);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default_header, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        // Back button should close nav view drawers if they're open (on either side)
        DrawerLayout navViewDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (isNavViewOpen(navViewDrawer)) {
            navViewDrawer.closeDrawers();
        } else if (mCommentsOpen) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag(Constants.TAG_FRAG_FULL_DISPLAYER);
            // should never be null since comments cannot be open
            if (fragment != null) {
                ((FragmentFullDisplay) fragment).closeComments();
            }
        }
        // Top left back arrow should open left nav menu drawer if we're not currently in any fragments
        else if ((getSupportFragmentManager().getBackStackEntryCount() == 0)
                && !navViewDrawer.isDrawerOpen(GravityCompat.START)) {
            navViewDrawer.openDrawer(GravityCompat.START);
        }
        // Not at the home screen, pop back stack instead of closing activity
        else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /* Hacky workaround for differentiating between hardware back button and menu back button*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back button should close nav view drawers if they're open (on either side)
        DrawerLayout navViewDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0 || isNavViewOpen(navViewDrawer)) {
                onBackPressed();
            }
            // Confirm exit app
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this, R.style.TransparentDialog);
                builder.setTitle("Confirm exit");
                builder.setMessage("Really exit app?");
                builder.setIcon(R.drawable.ic_white_exclamation);

                builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ActivityHome.this.exitApp();
                    }
                });
                builder.setNegativeButton(android.R.string.no, null);
                AlertDialog alertDialog = builder.create();

                // button color setup
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(getResources().getColor(R.color.colorWhite));
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                .setTextColor(getResources().getColor(R.color.colorWhite));
                    }
                });

                // resize the alert dialog
                alertDialog.show();
                alertDialog.getWindow().setLayout((6 * mScreenWidth) / 7, (4 * mScreenHeight) / 18);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INTENT_SETTINGS) {
            if (resultCode == RESULT_OK) {
                //refreshFragment(Constants.TAG_FRAG_HOME, false);
                //Log.e(TAG, "Returned from settings activity");
            }
        }
    }


    private void exitApp() {
        super.onBackPressed();
    }

    // A Navigation View is a menu that slides from the left or right
    private boolean isNavViewOpen(DrawerLayout drawer) {
        return drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END);
    }

    /*
        Interface implementations
     */
    @Override
    public void openSettings() {
        Intent settingsIntent = new Intent(this, ActivitySettings.class);
        //settingsIntent.putExtra()
        startActivityForResult(settingsIntent, Constants.INTENT_SETTINGS);
    }

    @Override
    public void refreshFeed(boolean invalidateData) {
        refreshFragment(Constants.TAG_FRAG_HOME, invalidateData);
    }


    @Override
    public void menuGoBack() {
        onBackPressed();
    }

    // hosting a 404 page as the top of the backstack
    @Override
    public void set404(boolean is404) {
        //this.mIs404 = is404;
    }

    @Override
    public void onCloseClickDetected() {
        if (App.getSharedPrefs().getBoolean(Constants.PREFS_ALLOW_CLOSE_CLICK, true)) {
            if (!mCommentsOpen) {
                getSupportFragmentManager().popBackStack();
            } else {
                // Check mCommentsOpen prevents this scenario:
                // View submission if full displayer -> Open comments ->
                // Click zoomieview while comments open ->
                // Full displayer closes (should close comments before closing entire view)
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentByTag(Constants.TAG_FRAG_FULL_DISPLAYER);
                // should never be null since comments cannot be open
                if (fragment != null) {
                    ((FragmentFullDisplay) fragment).closeComments();
                }
            }
        }
    }

    // Called on Retry button click
    @Override
    public void refresh404(String tag) {
        refreshFragment(tag, true);
    }


    @Override
    public void startOver() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        refreshFeed(true);
    }

    @Override
    public void isCommentsOpen(boolean commentsOpen) {
        mCommentsOpen = commentsOpen;
    }
}
