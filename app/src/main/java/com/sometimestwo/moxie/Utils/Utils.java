package com.sometimestwo.moxie.Utils;


import android.app.Activity;
import android.os.Looper;
import android.widget.ProgressBar;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

public class Utils {

    /* Takes a string and makes it cute: CONTROVERSIAL -> Controversial */
    public static String makeTextCute(String ugly){
        if(ugly != null && ugly.length() > 0) {
            return ugly.substring(0, 1).toUpperCase() + ugly.substring(1).toLowerCase();
        }
        return ugly;
    }
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
    public enum SubmissionDomain{
        IMGUR,
        GFYCAT,
        VREDDIT,
        IREDDIT,
        YOUTUBE,
        OTHER
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

    public static String getGfycatHash(String gfycatUrl){
        String hash = gfycatUrl.substring(gfycatUrl.lastIndexOf("/", gfycatUrl.length()));
        if (hash.contains("-size_restricted")){
            hash = hash.replace("-size_restricted", "");
        }
        if(hash.contains("?autoplay=enabled")){
            hash = hash.replace("?autoplay=enabled", "");
        }
        // remove trailing slash
        return hash.substring(1);
        //return gfycatUrl.substring(gfycatUrl.lastIndexOf("/", gfycatUrl.length()));
    }


    /**
     * Shows a ProgressBar in the UI. If this method is called from a non-main thread, it will run
     * the UI code on the main thread
     *
     * @param activity        The activity context to use to display the ProgressBar
     * @param progressBar     The ProgressBar to display
     * @param isIndeterminate True to show an indeterminate ProgressBar, false otherwise
     */
    public static void showProgressBar(final Activity activity, final ProgressBar progressBar,
                                        final boolean isIndeterminate) {
        if (activity == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Current Thread is Main Thread.
            if (progressBar != null) progressBar.setIndeterminate(isIndeterminate);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressBar != null) progressBar.setIndeterminate(isIndeterminate);
                }
            });
        }
    }


    public static boolean mux(String videoFile, String audioFile, String outputFile) {
        com.googlecode.mp4parser.authoring.Movie video;
        try {
            new MovieCreator();
            video = MovieCreator.build(videoFile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        com.googlecode.mp4parser.authoring.Movie audio;
        try {
            new MovieCreator();
            audio = MovieCreator.build(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        com.googlecode.mp4parser.authoring.Track audioTrack = audio.getTracks().get(0);

        CroppedTrack croppedTrack = new CroppedTrack(audioTrack, 0, audioTrack.getSamples().size());
        video.addTrack(croppedTrack);
        Container out = new DefaultMp4Builder().build(video);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        BufferedWritableFileByteChannel byteBufferByteChannel =
                new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Code from https://stackoverflow.com/a/9293885/3697225
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private static class BufferedWritableFileByteChannel implements WritableByteChannel {
        private static final int BUFFER_CAPACITY = 1000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() {
            dumpToFile();
            isOpen = false;
        }

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
