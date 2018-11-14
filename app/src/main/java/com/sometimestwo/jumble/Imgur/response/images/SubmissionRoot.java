package com.sometimestwo.jumble.Imgur.response.images;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionRoot {

    @SerializedName("data")
    @Expose
    private ImgurSubmission imgurSubmissionData;
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("status")
    @Expose
    private Integer status;

    public ImgurSubmission getImgurSubmissionData() {
        return imgurSubmissionData;
    }

    public void setImgurSubmissionData(ImgurSubmission imgurSubmissionData) {
        this.imgurSubmissionData = imgurSubmissionData;
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
