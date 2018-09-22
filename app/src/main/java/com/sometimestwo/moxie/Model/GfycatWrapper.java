package com.sometimestwo.moxie.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GfycatWrapper {
    @SerializedName("gfyItem")
    @Expose
    private GfyItem gfyItem;

    public GfyItem getGfyItem() {
        return gfyItem;
    }

    public void setGfyItem(GfyItem gfyItem) {
        this.gfyItem = gfyItem;
    }
}
