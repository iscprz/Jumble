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
import android.widget.ProgressBar;
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
    private TextView mSubmissionTitle;
    private ImageView mImageView;
    private LinearLayout mCommentsContainer;
    private View mClicker;
    private GestureDetector mDetector;
    Toolbar mToolbar;
    ProgressBar mProgressBar;

    private SubmissionDisplayerEventListener mMediaDisplayerEventListener;

    public interface SubmissionDisplayerEventListener { }

    public static FragmentSubmissionViewer newInstance() {
        return new FragmentSubmissionViewer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.EXTRA_SUBMISSION_OBJ);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getContext()));

        View v = inflater.inflate(R.layout.submission_viewer, container, false);

        /* Toolbar setup*/
        mToolbar = (Toolbar) v.findViewById(R.id.submission_viewer_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        //mBigImageView = (BigImageView) v.findViewById(R.id.big_image_viewer);

        mSubmissionTitle = (TextView) v.findViewById(R.id.submission_viewer_title);

        /* The actual view in which the image is displayed*/
        mImageView = (ImageView) v.findViewById(R.id.submission_media_view);
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

        mProgressBar = (ProgressBar) v.findViewById(R.id.submission_viewer_media_progress);

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
            toolbar.setTitle(mCurrSubmission.getSubreddit());
        }
        mToolbar.setAlpha(1);
    }

    // Pop this fragment off the stack, effectively closing the submission viewer.
    private void closeMediaPlayer() {
        goBack();
    }

    private void goBack() {
        releaseExoPlayer();
        try {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void releaseExoPlayer() { }

    private void setupMedia() {

        /*
            RequestOptions options = new RequestOptions()
                    .skipMemoryCache(true)
                    .override(1280, 720)
                    .centerInside();
*/
        mSubmissionTitle.setText(mCurrSubmission.getTitle());
        // mBigImageView.showImage(Uri.parse(mCurrSubmission.getPostURL()));

        //image
        //String cleanPostUrl = Helpers.ensureImageUrl(mCurrSubmission.getUrl());
        //TODO: handle invalid URL
        Glide.with(this)
                .load(mCurrSubmission.getUrl())
                .listener(new ProgressBarRequestListener(mProgressBar))
                //* .apply(options)*//*
                .into(mImageView);

    }
}
