package com.sometimestwo.moxie;

public interface HomeEventListener {
    public void openSettings();

    public void refreshFeed(boolean invalidateData);

    // back arrow at top of screen (as opposed to hardware back button)
    public void menuGoBack();

    public void set404(boolean is404);

    // Clear any active fragment/activity and start over from ActivityHome.
    // This can be used when requesting a new log in
    public void startOver();
}
