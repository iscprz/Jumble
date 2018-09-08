package com.sometimestwo.moxie.Utils;


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

}
