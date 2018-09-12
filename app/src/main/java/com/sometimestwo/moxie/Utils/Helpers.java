package com.sometimestwo.moxie.Utils;


import net.dean.jraw.models.Submission;

import java.util.Arrays;
import java.util.List;

public class Helpers {

    /*
       imgur links will be given in the following format :
       https://i.imgur.com/CtyvHl6.gifv
     */
    public static String getFileExtensionFromPostUrl(String postURL) {
        String split[] = postURL.split("\\.");
        return split.length > 0 ? split[split.length - 1] : "";
    }

    public static String formatGifUrl(String badGifUrl) {
        String split[] = badGifUrl.split("\\.");
        if ("gifv".equalsIgnoreCase(split[split.length - 1])) {
            split[split.length - 1] = "gif";
        }
        return Arrays.toString(split);
    }

    public enum MediaType {
        IMAGE,
        GIF,
        YOUTUBE
    }

    public static Constants.SubmissionType getSubmissionType(String url) {
        String extension = getFileExtensionFromPostUrl(url);
        if ("gif".equalsIgnoreCase(extension)
                || "gifv".equalsIgnoreCase(extension)) {
            return Constants.SubmissionType.GIF;
        } else if ("jpg".equalsIgnoreCase(extension)
                || "jpeg".equalsIgnoreCase(extension)
                || "png".equalsIgnoreCase(extension)) {
            return Constants.SubmissionType.IMAGE;
        }
        //youtube
        /* else if()*/

        return null;
    }

    // takes a URL and ensures it takes us directly to an image URL (.jpg, .jpeg, .png)
    public static String ensureImageUrl(String url) {
        //  url = url.toLowerCase();
        if (Arrays.asList(Constants.VALID_IMAGE_EXTENSION).contains(getFileExtensionFromPostUrl(url))) {
            // already directly points to image, no change needed
            return url;
        }

        StringBuilder sb = new StringBuilder(url);

        // imgur
        if ("imgur".contains(url)) {
            // url is formatted like: https://imgur.com/V51pWpk
            // url needs to be formatted to https://imgur.com/V51pWpk.jpg
            sb.append(".jpg");
            return sb.toString();
        }

        return "";
    }


    // Assuming we get an Imgur link in one of the following formats:
    // 1. https://i.imgur.com/4RxPsWI.gifv
    // 2. https://i.imgur.com/4RxPsWI
    //
    // Return: 4RxPsWI
    public static String getImgurHash(String imgurLink){
        String split[] = imgurLink.split("/");
        String res = split[split.length-1];
        if(res.contains(".")){
            res = res.split("\\.")[0];
        }
        return res;
    }
}
