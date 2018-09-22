package com.sometimestwo.moxie.Model;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GfyItem {

    @SerializedName("gfyId")
    @Expose
    private String gfyId;
    @SerializedName("gfyName")
    @Expose
    private String gfyName;
    @SerializedName("gfyNumber")
    @Expose
    private String gfyNumber;
    @SerializedName("avgColor")
    @Expose
    private String avgColor;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("width")
    @Expose
    private String width;
    @SerializedName("height")
    @Expose
    private String height;
    @SerializedName("frameRate")
    @Expose
    private String frameRate;
    @SerializedName("numFrames")
    @Expose
    private String numFrames;
    @SerializedName("mp4Url")
    @Expose
    private String mp4Url;
    @SerializedName("webmUrl")
    @Expose
    private String webmUrl;
    @SerializedName("webpUrl")
    @Expose
    private String webpUrl;
    @SerializedName("mobileUrl")
    @Expose
    private String mobileUrl;
    @SerializedName("mobilePosterUrl")
    @Expose
    private String mobilePosterUrl;
    @SerializedName("posterUrl")
    @Expose
    private String posterUrl;
    @SerializedName("thumb360Url")
    @Expose
    private String thumb360Url;
    @SerializedName("thumb360PosterUrl")
    @Expose
    private String thumb360PosterUrl;
    @SerializedName("thumb100PosterUrl")
    @Expose
    private String thumb100PosterUrl;
    @SerializedName("max5mbGif")
    @Expose
    private String max5mbGif;
    @SerializedName("max2mbGif")
    @Expose
    private String max2mbGif;
    @SerializedName("mjpgUrl")
    @Expose
    private String mjpgUrl;
    @SerializedName("miniUrl")
    @Expose
    private String miniUrl;
    @SerializedName("miniPosterUrl")
    @Expose
    private String miniPosterUrl;
    @SerializedName("gifUrl")
    @Expose
    private Object gifUrl;
    @SerializedName("gifSize")
    @Expose
    private Object gifSize;
    @SerializedName("mp4Size")
    @Expose
    private String mp4Size;
    @SerializedName("webmSize")
    @Expose
    private String webmSize;
    @SerializedName("createDate")
    @Expose
    private String createDate;
    @SerializedName("views")
    @Expose
    private Integer views;
    @SerializedName("viewsNewEpoch")
    @Expose
    private Object viewsNewEpoch;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("extraLemmas")
    @Expose
    private Object extraLemmas;
    @SerializedName("md5")
    @Expose
    private Object md5;
    @SerializedName("tags")
    @Expose
    private Object tags;
    @SerializedName("userTags")
    @Expose
    private Object userTags;
    @SerializedName("nsfw")
    @Expose
    private String nsfw;
    @SerializedName("sar")
    @Expose
    private Object sar;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("dynamo")
    @Expose
    private Object dynamo;
    @SerializedName("subreddit")
    @Expose
    private Object subreddit;
    @SerializedName("redditId")
    @Expose
    private Object redditId;
    @SerializedName("redditIdText")
    @Expose
    private Object redditIdText;
    @SerializedName("likes")
    @Expose
    private String likes;
    @SerializedName("dislikes")
    @Expose
    private Object dislikes;
    @SerializedName("published")
    @Expose
    private String published;
    @SerializedName("description")
    @Expose
    private Object description;
    @SerializedName("copyrightClaimaint")
    @Expose
    private Object copyrightClaimaint;
    @SerializedName("languageText")
    @Expose
    private Object languageText;
    @SerializedName("fullDomainWhitelist")
    @Expose
    private List<Object> fullDomainWhitelist = null;
    @SerializedName("fullGeoWhitelist")
    @Expose
    private List<Object> fullGeoWhitelist = null;
    @SerializedName("iframeProfileImageVisible")
    @Expose
    private Boolean iframeProfileImageVisible;

    public String getGfyId() {
        return gfyId;
    }

    public void setGfyId(String gfyId) {
        this.gfyId = gfyId;
    }

    public String getGfyName() {
        return gfyName;
    }

    public void setGfyName(String gfyName) {
        this.gfyName = gfyName;
    }

    public String getGfyNumber() {
        return gfyNumber;
    }

    public void setGfyNumber(String gfyNumber) {
        this.gfyNumber = gfyNumber;
    }

    public String getAvgColor() {
        return avgColor;
    }

    public void setAvgColor(String avgColor) {
        this.avgColor = avgColor;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(String frameRate) {
        this.frameRate = frameRate;
    }

    public String getNumFrames() {
        return numFrames;
    }

    public void setNumFrames(String numFrames) {
        this.numFrames = numFrames;
    }

    public String getMp4Url() {
        return mp4Url;
    }

    public void setMp4Url(String mp4Url) {
        this.mp4Url = mp4Url;
    }

    public String getWebmUrl() {
        return webmUrl;
    }

    public void setWebmUrl(String webmUrl) {
        this.webmUrl = webmUrl;
    }

    public String getWebpUrl() {
        return webpUrl;
    }

    public void setWebpUrl(String webpUrl) {
        this.webpUrl = webpUrl;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public String getMobilePosterUrl() {
        return mobilePosterUrl;
    }

    public void setMobilePosterUrl(String mobilePosterUrl) {
        this.mobilePosterUrl = mobilePosterUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getThumb360Url() {
        return thumb360Url;
    }

    public void setThumb360Url(String thumb360Url) {
        this.thumb360Url = thumb360Url;
    }

    public String getThumb360PosterUrl() {
        return thumb360PosterUrl;
    }

    public void setThumb360PosterUrl(String thumb360PosterUrl) {
        this.thumb360PosterUrl = thumb360PosterUrl;
    }

    public String getThumb100PosterUrl() {
        return thumb100PosterUrl;
    }

    public void setThumb100PosterUrl(String thumb100PosterUrl) {
        this.thumb100PosterUrl = thumb100PosterUrl;
    }

    public String getMax5mbGif() {
        return max5mbGif;
    }

    public void setMax5mbGif(String max5mbGif) {
        this.max5mbGif = max5mbGif;
    }

    public String getMax2mbGif() {
        return max2mbGif;
    }

    public void setMax2mbGif(String max2mbGif) {
        this.max2mbGif = max2mbGif;
    }

    public String getMjpgUrl() {
        return mjpgUrl;
    }

    public void setMjpgUrl(String mjpgUrl) {
        this.mjpgUrl = mjpgUrl;
    }

    public String getMiniUrl() {
        return miniUrl;
    }

    public void setMiniUrl(String miniUrl) {
        this.miniUrl = miniUrl;
    }

    public String getMiniPosterUrl() {
        return miniPosterUrl;
    }

    public void setMiniPosterUrl(String miniPosterUrl) {
        this.miniPosterUrl = miniPosterUrl;
    }

    public Object getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(Object gifUrl) {
        this.gifUrl = gifUrl;
    }

    public Object getGifSize() {
        return gifSize;
    }

    public void setGifSize(Object gifSize) {
        this.gifSize = gifSize;
    }

    public String getMp4Size() {
        return mp4Size;
    }

    public void setMp4Size(String mp4Size) {
        this.mp4Size = mp4Size;
    }

    public String getWebmSize() {
        return webmSize;
    }

    public void setWebmSize(String webmSize) {
        this.webmSize = webmSize;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Object getViewsNewEpoch() {
        return viewsNewEpoch;
    }

    public void setViewsNewEpoch(Object viewsNewEpoch) {
        this.viewsNewEpoch = viewsNewEpoch;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getExtraLemmas() {
        return extraLemmas;
    }

    public void setExtraLemmas(Object extraLemmas) {
        this.extraLemmas = extraLemmas;
    }

    public Object getMd5() {
        return md5;
    }

    public void setMd5(Object md5) {
        this.md5 = md5;
    }

    public Object getTags() {
        return tags;
    }

    public void setTags(Object tags) {
        this.tags = tags;
    }

    public Object getUserTags() {
        return userTags;
    }

    public void setUserTags(Object userTags) {
        this.userTags = userTags;
    }

    public String getNsfw() {
        return nsfw;
    }

    public void setNsfw(String nsfw) {
        this.nsfw = nsfw;
    }

    public Object getSar() {
        return sar;
    }

    public void setSar(Object sar) {
        this.sar = sar;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Object getDynamo() {
        return dynamo;
    }

    public void setDynamo(Object dynamo) {
        this.dynamo = dynamo;
    }

    public Object getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(Object subreddit) {
        this.subreddit = subreddit;
    }

    public Object getRedditId() {
        return redditId;
    }

    public void setRedditId(Object redditId) {
        this.redditId = redditId;
    }

    public Object getRedditIdText() {
        return redditIdText;
    }

    public void setRedditIdText(Object redditIdText) {
        this.redditIdText = redditIdText;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public Object getDislikes() {
        return dislikes;
    }

    public void setDislikes(Object dislikes) {
        this.dislikes = dislikes;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getCopyrightClaimaint() {
        return copyrightClaimaint;
    }

    public void setCopyrightClaimaint(Object copyrightClaimaint) {
        this.copyrightClaimaint = copyrightClaimaint;
    }

    public Object getLanguageText() {
        return languageText;
    }

    public void setLanguageText(Object languageText) {
        this.languageText = languageText;
    }

    public List<Object> getFullDomainWhitelist() {
        return fullDomainWhitelist;
    }

    public void setFullDomainWhitelist(List<Object> fullDomainWhitelist) {
        this.fullDomainWhitelist = fullDomainWhitelist;
    }

    public List<Object> getFullGeoWhitelist() {
        return fullGeoWhitelist;
    }

    public void setFullGeoWhitelist(List<Object> fullGeoWhitelist) {
        this.fullGeoWhitelist = fullGeoWhitelist;
    }

    public Boolean getIframeProfileImageVisible() {
        return iframeProfileImageVisible;
    }

    public void setIframeProfileImageVisible(Boolean iframeProfileImageVisible) {
        this.iframeProfileImageVisible = iframeProfileImageVisible;
    }

}