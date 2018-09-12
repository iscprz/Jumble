package com.sometimestwo.moxie;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.sometimestwo.moxie.Utils.Helpers;

import net.dean.jraw.models.Submission;

public class FragmentMediaDisplayer extends Fragment {
    private static final String TAG = Constants.TAG_FRAG_MEDIA_DISPLAY;
    private SubmissionObj mCurrSubmission;
    private BigImageView mBigImageView;
    private TextView mTitle;
    private ImageView mImageView;
    private RelativeLayout mBackground;
    private LinearLayout mCommentsContainer;
    private View mClicker;
    private GestureDetector mDetector;


    private MediaDisplayerEventListener mMediaDisplayerEventListener;

    public interface MediaDisplayerEventListener {
        public void closeMediaDisplayer();
    }

    public static FragmentMediaDisplayer newInstance() {
        return new FragmentMediaDisplayer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.EXTRA_POST);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getContext()));

        View v = inflater.inflate(R.layout.media_viewer, container, false);

        //mBigImageView = (BigImageView) v.findViewById(R.id.big_image_viewer);

        mTitle = (TextView) v.findViewById(R.id.media_viewer_title);

        /* View that will capture click events on image. This view will be transparent*/
 /*       mClicker = (View) v.findViewById(R.id.media_viewer_clicker);
        mDetector = new GestureDetector(getContext(), new MediaViewGestureListener());
        mClicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mDetector.onTouchEvent(motionEvent);
            }
        });*/

        /* The actual view in which the image is displayed*/
        mImageView = (ImageView) v.findViewById(R.id.media_viewer_image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaDisplayerEventListener.closeMediaDisplayer();
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
            mMediaDisplayerEventListener = (MediaDisplayerEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener inferfaces!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //setupToolbar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


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
