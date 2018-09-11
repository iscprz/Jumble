package com.sometimestwo.moxie.Imgur.response.galleries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sometimestwo.moxie.Imgur.response.common.Tag;

public class GalleriesTagRoot {

    @SerializedName("data")
    @Expose
    private Tag data;
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("status")
    @Expose
    private Integer status;

    public Tag getData() {
        return data;
    }

    public void setData(Tag data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
