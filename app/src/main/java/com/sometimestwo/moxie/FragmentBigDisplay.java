package com.sometimestwo.moxie;

import android.content.Context;
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

    private BigImageView mBigImageView;
    private ImageView mButtonDownload;
    private ImageView mButtonCopyMediaURL;
    private ImageView mButtonShare;

    public static FragmentBigDisplay newInstance() {
        return new FragmentBigDisplay();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlideApp = Glide.with(this);

        unpackArgs();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getContext()));
        View v = inflater.inflate(R.layout.fragment_big_display, container, false);

        /* Main zoomie image view*/
        mBigImageView = (BigImageView) v.findViewById(R.id.big_image_viewer);
        mBigImageView.showImage(Uri.parse(mCurrSubmission.getUrl()));
        /* Snackbar */


       // mButtonDownload = (ImageView) v.findViewById(R.id.button1);
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
        }    }

}
