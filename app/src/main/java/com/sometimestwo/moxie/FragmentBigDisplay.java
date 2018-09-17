package com.sometimestwo.moxie;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;

public class FragmentBigDisplay extends Fragment {

    private RequestManager GlideApp;
    private SubmissionObj mCurrSubmission;
    private boolean mPrefsAllowNSFW;
    private boolean mAllowCloseOnClick;

    /* Toolbar */
    private ImageView mButtonBack;
    private ImageView mButtonDownload;
    private ImageView mButtonCopyURL;
    private ImageView mButtonShare;

    /* Big zoomie view*/
    private BigImageView mBigImageView;

    public static FragmentBigDisplay newInstance() {
        return new FragmentBigDisplay();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlideApp = Glide.with(this);

        unpackArgs();

        // Read relevant permission settings
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.KEY_GETPREFS_SETTINGS, Context.MODE_PRIVATE);

        mPrefsAllowNSFW = prefs.getString(Constants.KEY_ALLOW_NSFW, Constants.SETTINGS_NO).equalsIgnoreCase(Constants.SETTINGS_YES);
        mAllowCloseOnClick = prefs.getString(Constants.KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK, Constants.SETTINGS_NO).equalsIgnoreCase(Constants.SETTINGS_YES);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getContext()));
        View v = inflater.inflate(R.layout.fragment_big_display, container, false);

        /* Toolbar back button*/
        mButtonBack = (ImageView) v.findViewById(R.id.big_display_button_back);
        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        /* Toolbar download button*/
        mButtonDownload = (ImageView) v.findViewById(R.id.big_display_button_download);
        /* Toolbar copy url button*/
        mButtonCopyURL = (ImageView) v.findViewById(R.id.big_display_button_copy_url);
        /* Toolbar share button*/
        mButtonShare = (ImageView) v.findViewById(R.id.big_display_button_share);


        /* Main zoomie image view*/
        mBigImageView = (BigImageView) v.findViewById(R.id.big_image_viewer);
        mBigImageView.showImage(Uri.parse(mCurrSubmission.getUrl()));

        /* Exit on image tap if settings option is enabled*/
        mBigImageView.setClickable(true);
        mBigImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAllowCloseOnClick) {
                    closeMediaPlayer();
                }
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void unpackArgs(){
        try{
            // Submission to be viewed
            mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.ARGS_SUBMISSION_OBJ);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Pop this fragment off the stack, effectively closing the submission viewer.
    private void closeMediaPlayer() {
        goBack();
    }

    private void goBack() {
        //releaseExoPlayer();
        try {
            getActivity().getSupportFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /* private void releaseExoPlayer() {
        if (player != null) {
            player.release();
        }
    }*/
}
