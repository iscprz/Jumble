package com.sometimestwo.moxie.Utils;


import net.dean.jraw.models.Submission;

import java.util.Arrays;

public class Helpers {

    /*
       imgur links will be given in the following format :
       https://i.imgur.com/CtyvHl6.gifv
     */
    public static String getFileExtensionFromPostUrl(String postURL){
        String split[] = postURL.split("\\.");
        return split.length > 0 ? split[split.length-1] : "";
    }

    public static String formatGifUrl(String badGifUrl){
        String split[] = badGifUrl.split("\\.");
        if("gifv".equalsIgnoreCase(split[split.length-1])){
            split[split.length-1] = "gif";
        }
        return Arrays.toString(split);
    }

    public enum MediaType{
        IMAGE,
        GIF,
        YOUTUBE
    }

    public static MediaType getMediaType(Submission item){
        String extension = getFileExtensionFromPostUrl(item.getUrl());
        if("gif".equalsIgnoreCase(extension)
                || "gifv".equalsIgnoreCase(extension)){
            return MediaType.GIF;
        }
        else if("jpg".equalsIgnoreCase(extension)
                || "jpeg".equalsIgnoreCase(extension)
                || "png".equalsIgnoreCase(extension)){
            return MediaType.IMAGE;
        }
        //youtube
        /* else if()*/

            return null;
    }

}
