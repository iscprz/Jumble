package com.sometimestwo.jumble.EventListeners;

// used when attempting a generic reddit task such as subscribing/unsubscribing
public interface OnRedditTaskListener {
    public void onSuccess();
    public void onFailure(String exceptionMessage);
}
