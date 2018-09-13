package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;

public class FragmentSubmissionViewer extends Fragment {
    private static final String TAG = Constants.TAG_FRAG_MEDIA_DISPLAY;
    private SubmissionObj mCurrSubmission;
    private BigImageView mBigImageView;
    private TextView mTitle;
    private ImageView mImageView;
    private RelativeLayout mBackground;
    private LinearLayout mCommentsContainer;
    private View mClicker;
    private GestureDetector mDetector;
    Toolbar mToolbar;

    private SubmissionDisplayerEventListener mMediaDisplayerEventListener;

    public interface SubmissionDisplayerEventListener {}

    public static FragmentSubmissionViewer newInstance() {
        return new FragmentSubmissionViewer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.EXTRA_POST);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getContext()));

        View v = inflater.inflate(R.layout.submission_viewer, container, false);

        /* Toolbar setup*/
        mToolbar = (Toolbar) v.findViewById(R.id.media_viewer_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        //mBigImageView = (BigImageView) v.findViewById(R.id.big_image_viewer);

        mTitle = (TextView) v.findViewById(R.id.media_viewer_title);

        /* The actual view in which the image is displayed*/
        mImageView = (ImageView) v.findViewById(R.id.media_viewer_image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMediaPlayer();
            }
        });
        /*mBigImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "Clicked media viewer(not the background)! ");
                mMediaDisplayerEventListener.closeMediaDisplayer();
            }
        });*/
        mBackground = (RelativeLayout) v.findViewById(R.id.post_container);
        // mBackground = (FrameLayout) v.findViewById(R.id.bg_media_viewer);
       /* mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "Clicked media viewer background(black tinted area)! ");
                //mMediaDisplayerEventListener.closeMediaDisplayer();
            }
        });*/


        mCommentsContainer = (LinearLayout) v.findViewById(R.id.media_viewer_comments_container);
        setupMedia();
      /*  mToolbar = (Toolbar) v.findViewById(R.id.toolbar_top);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);*/
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMediaDisplayerEventListener = (SubmissionDisplayerEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener inferfaces!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupToolbar();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseExoPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseExoPlayer();
    }

    @Override
    public void onDestroy() {
        // Notify calling fragment that we're closing the submission viewer
        // Don't need to pass anything back. Pass an empty intent for now
        Intent resultIntent = new Intent();
        getTargetFragment().onActivityResult(FragmentHome.KEY_INTENT_GOTO_SUBMISSIONVIEWER, Activity.RESULT_OK, resultIntent);
        releaseExoPlayer();
        super.onDestroy();
    }


    private void setupToolbar() {
        //toolbar setup
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            //toolbar.setTitle(getResources().getString(R.string.toolbar_title_albums));
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setHomeAsUpIndicator(R.drawable.ic_back_arrow);
            toolbar.setTitle("");
        }
        mToolbar.setAlpha(1);
    }

    // Pop this fragment off the stack, effectively closing the submission viewer.
    private void closeMediaPlayer() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment mediaDisplayerFragment = (FragmentSubmissionViewer) fm.findFragmentByTag(Constants.TAG_FRAG_MEDIA_DISPLAY);
        if (mediaDisplayerFragment != null) {
            fm.beginTransaction().remove(mediaDisplayerFragment).commit();
            // Note: onDestroy gets called when we pop this off the stack.
            fm.popBackStack();
        }
    }

    private void releaseExoPlayer() {}

    private void setupMedia() {

        /*
            RequestOptions options = new RequestOptions()
                    .skipMemoryCache(true)
                    .override(1280, 720)
                    .centerInside();
*/
        mTitle.setText(mCurrSubmission.getTitle());
        // mBigImageView.showImage(Uri.parse(mCurrSubmission.getPostURL()));

        //image
        //String cleanPostUrl = Helpers.ensureImageUrl(mCurrSubmission.getUrl());
        //TODO: handle invalid URL
        Glide.with(this)
                .load(mCurrSubmission.getUrl())
                //* .apply(options)*//*
                .into(mImageView);

    }
}
