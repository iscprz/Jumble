package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;

public class FragmentFullDisplay extends Fragment {

    private RequestManager GlideApp;
    private SubmissionObj mCurrSubmission;
    private boolean mPrefsAllowNSFW;
    private boolean mAllowCloseOnClick;

    /* Titles */
    TextView mTitleTextView;
    TextView mSubredditTextView;
    RelativeLayout mTitleContainer;

    /* Snackbar */
    private ImageView mButtonComments;
    private ImageView mButtonUpvote;
    private ImageView mButtonDownvote;
    private ImageView mButtonOverflow;
    private LinearLayout mSnackbarContainer;

    /* Big zoomie view*/
    private BigImageView mBigImageView;

    public static FragmentFullDisplay newInstance() {
        return new FragmentFullDisplay();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlideApp = Glide.with(this);

        unpackArgs();

        // Read relevant permission settings
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);

        mPrefsAllowNSFW = prefs.getBoolean(Constants.SETTINGS_ALLOW_NSFW, false);
        mAllowCloseOnClick = prefs.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getContext()));
        View v = inflater.inflate(R.layout.fragment_full_media_display, container, false);


        /* Titles */
        mTitleTextView = (TextView) v.findViewById(R.id.big_display_title);
        mSubredditTextView = (TextView) v.findViewById(R.id.big_display_subreddit);

        /* Snackbar */
        mButtonComments = (ImageView) v.findViewById(R.id.big_display_snack_bar_comments);
        mButtonUpvote = (ImageView) v.findViewById(R.id.big_display_snack_bar_upvote);
        mButtonDownvote = (ImageView) v.findViewById(R.id.big_display_snack_bar_downvote);
        mButtonOverflow = (ImageView) v.findViewById(R.id.big_display_snack_bar_overflow);
        mSnackbarContainer = (LinearLayout) v.findViewById(R.id.big_display_snack_bar_container);


        setupFullViewer();



        /* Toolbar Copy Url button*//*
        mButtonCopyURL = (ImageView) v.findViewById(R.id.big_display_button_copy_url);
        mButtonCopyURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(mCurrSubmission.getUrl());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(mCurrSubmission.getTitle(), mCurrSubmission.getUrl());
                    clipboard.setPrimaryClip(clip);
                }
               *//* Toast.makeText(getContext(), getContext().getResources()
                        .getString(R.string.toast_copied_to_clipboard), Toast.LENGTH_SHORT).show();*//*
            }
        });
        *//* Toolbar Share button*//*
        mButtonShare = (ImageView) v.findViewById(R.id.big_display_button_share);*/


        /* Main zoomie image view*/
        mBigImageView = (BigImageView) v.findViewById(R.id.big_image_viewer);
//        mBigImageView.bringToFront();
        // mBigImageView.setScaleX(.5f);
        // mBigImageView.set(.5f);

        mBigImageView.showImage(Uri.parse(mCurrSubmission.getUrl()));

        /* Exit on image tap if settings option is enabled*/
        mBigImageView.setClickable(true);
        mBigImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllowCloseOnClick) {
                    closeFullDisplay();
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

    @Override
    public void onDestroy() {
        // Notify calling fragment that we're closing the submission viewer
        // Don't need to pass anything back. Pass an empty intent for now
        Intent resultIntent = new Intent();
        getTargetFragment().onActivityResult(FragmentHome.KEY_INTENT_GOTO_BIG_DISPLAY, Activity.RESULT_OK, resultIntent);
        //releaseExoPlayer();
        super.onDestroy();
    }

    private void unpackArgs() {
        try {
            // Submission to be viewed
            mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.ARGS_SUBMISSION_OBJ);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pop this fragment off the stack, effectively closing the submission viewer.
    private void closeFullDisplay() {
        //releaseExoPlayer();
        try {
            getActivity().getSupportFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupFullViewer() {
        // set up title
        mTitleTextView.setText(mCurrSubmission.getCompactTitle() != null
                ? mCurrSubmission.getCompactTitle() : mCurrSubmission.getTitle());
        mSubredditTextView.setText(mCurrSubmission.getSubreddit());

        // visit comments
        mButtonComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSubmissionViewer();
            }
        });
    }


    private void openSubmissionViewer() {
        Intent submissionViewerIntent = new Intent(getContext(), ActivitySubmissionViewer.class);
        submissionViewerIntent.putExtra(Constants.EXTRA_SUBMISSION_OBJ, mCurrSubmission);
        startActivity(submissionViewerIntent);
        closeFullDisplay();
       // getActivity().finish();
    }

   /* private void releaseExoPlayer() {
        if (player != null) {
            player.release();
        }
    }*/
}
