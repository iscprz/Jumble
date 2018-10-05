package com.sometimestwo.moxie.Model;

//import com.gfycat.core.gfycatapi.pojo.Gfycat;

import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.EmbeddedMedia;
import net.dean.jraw.models.VoteDirection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import retrofit2.Call;

/*
 *  This class is a customized version of the Model class provided by JRAW.
 *  See net.dean.jraw.models.Submission for field details
 */
public class SubmissionObj implements Serializable {
    public SubmissionObj() {
    }

    // For quick instantiation of submission list when we encounter empty subreddit
    public SubmissionObj(Boolean isSubredditEmpty) {
        this.isSubredditEmpty = isSubredditEmpty;
    }

    //Notifies us when we've tried requesting data from a non-existant subreddit (0 submissions)
    private boolean isSubredditEmpty = false;

    private String author;
    private Date dateCreated;
    private Constants.SubmissionDomain domain;
    private String fullName;
    private short gilded;
    private boolean isHidden;
    private boolean isScoreHidden;
    private String id;
    private boolean isSelfPost;
    private String linkFlairText;
    private boolean isLocked;
    private boolean isNSFW;
    private String permalink;
    private String postHint;
    private String selfText;
    private boolean isSpam;
    private boolean isSpoiler;
    private String subreddit;
    private String subredditFullName;
    /**
     * The suggested way to sort comments, if any
     */
    private CommentSort suggestedSort;
    private String thumbnail;
    private boolean hasThumbnail;
    private String previewUrl;
    private String title;
    private String url;
    private String cleanedUrl;
    private boolean isVisited;
    private boolean isRemoved;
    private VoteDirection vote;
    private Integer commentCount;
    private Constants.SubmissionType submissionType;
    // object is in the process of getting filled through async API call
    private boolean loadingData;
    private int score;
    private EmbeddedMedia embeddedMedia;
    private String compactTitle;
    private boolean isSaved;
    private boolean isDownloadableMedia = false;
    // added specifically for Gfycat mp4 urls. lazy solution
    private String mp4Url;
    // null if file is not downloadable
    private String fileExtension;
    // What we will name the file if user downloads it (if media is downloadable)
    private String filenameIfDownloaded;
    // Stores a reference to a gfycat async call in case we need to cancel it


    public boolean isSubredditEmpty() {
        return isSubredditEmpty;
    }

    public void setSubredditEmpty(boolean subredditEmpty) {
        isSubredditEmpty = subredditEmpty;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Constants.SubmissionDomain getDomain() {
        return domain;
    }

    public void setDomain(Constants.SubmissionDomain domain) {
        this.domain = domain;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public short getGilded() {
        return gilded;
    }

    public void setGilded(short gilded) {
        this.gilded = gilded;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public boolean isScoreHidden() {
        return isScoreHidden;
    }

    public void setScoreHidden(boolean scoreHidden) {
        isScoreHidden = scoreHidden;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelfPost() {
        return isSelfPost;
    }

    public void setSelfPost(boolean selfPost) {
        isSelfPost = selfPost;
    }

    public String getLinkFlairText() {
        return linkFlairText;
    }

    public void setLinkFlairText(String linkFlairText) {
        this.linkFlairText = linkFlairText;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isNSFW() {
        return isNSFW;
    }

    public void setNSFW(boolean NSFW) {
        isNSFW = NSFW;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getPostHint() {
        return postHint;
    }

    public void setPostHint(String postHint) {
        this.postHint = postHint;
    }

    public String getSelfText() {
        return selfText;
    }

    public void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setSpam(boolean spam) {
        isSpam = spam;
    }

    public boolean isSpoiler() {
        return isSpoiler;
    }

    public void setSpoiler(boolean spoiler) {
        isSpoiler = spoiler;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getSubredditFullName() {
        return subredditFullName;
    }

    public void setSubredditFullName(String subredditFullName) {
        this.subredditFullName = subredditFullName;
    }

    public CommentSort getSuggestedSort() {
        return suggestedSort;
    }

    public void setSuggestedSort(CommentSort suggestedSort) {
        this.suggestedSort = suggestedSort;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public VoteDirection getVote() {
        return vote;
    }

    public void setVote(VoteDirection vote) {
        this.vote = vote;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Constants.SubmissionType getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(Constants.SubmissionType submissionType) {
        this.submissionType = submissionType;
    }

    public boolean isLoadingData() {
        return loadingData;
    }

    public void setLoadingData(boolean loadingData) {
        this.loadingData = loadingData;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCleanedUrl() {
        return cleanedUrl;
    }

    public void setCleanedUrl(String cleanedUrl) {
        this.cleanedUrl = cleanedUrl;
    }

    public EmbeddedMedia getEmbeddedMedia() {
        return embeddedMedia;
    }

    public void setEmbeddedMedia(EmbeddedMedia embeddedMedia) {
        this.embeddedMedia = embeddedMedia;
    }

    public String getCompactTitle() {
        return compactTitle;
    }

    public void setCompactTitle(String compactTitle) {
        this.compactTitle = compactTitle;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public boolean isDownloadableMedia() {
        return this.getFileExtension() != null
                || this.getDomain() == Constants.SubmissionDomain.VREDDIT;
    }

    public void setIsDownloadableMedia(boolean downloadable) {
        isDownloadableMedia = downloadable;
    }

    public String getMp4Url() {
        return mp4Url;
    }

    public void setMp4Url(String mp4Url) {
        this.mp4Url = mp4Url;
    }

    public String getFileExtension() {
        String extension = null;
        if (this.getMp4Url() != null || this.getDomain() == Constants.SubmissionDomain.VREDDIT)
            extension = "mp4";
        else if (this.getCleanedUrl() != null)
            extension = Utils.getFileExtensionFromUrl(this.getCleanedUrl());
        else
            extension = Utils.getFileExtensionFromUrl(this.getUrl());
        if (Arrays.asList(Constants.VALID_MEDIA_EXTENSION).contains(extension.toLowerCase())) {
            return extension;
        }
        return null;
    }

    public void setExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getPrettyFilename() {
        if (this.isDownloadableMedia()
                || this.getDomain() == Constants.SubmissionDomain.VREDDIT) {
            return this.getId() + "." + this.getFileExtension();
        }
        return "unsupported_media_type";
    }

    public void setFilenameIfDownloaded(String filenameIfDownloaded) {
        this.filenameIfDownloaded = filenameIfDownloaded;
    }

}
