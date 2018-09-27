package com.sometimestwo.moxie;

public interface HomeEventListener {
    public void openSettings();

    public void refreshFeed(boolean invalidateData);

    public void isHome(boolean isHome);

    public void goBack();

    public void set404(boolean is404);
}
