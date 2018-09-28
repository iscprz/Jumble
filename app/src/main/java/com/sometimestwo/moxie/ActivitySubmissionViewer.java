package com.sometimestwo.moxie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;

public class ActivitySubmissionViewer extends AppCompatActivity implements OnCloseClickEventListener {
    private SubmissionObj mCurrSubmission;
    private Toolbar mToolbar;
    private TextView mSubmissionTitle;
    private FrameLayout mImageViewContainer;
    private ImageView mImageView;
    private ImageView mPlayButton;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mRefreshLayout;

    // Permissions from shared prefs that we will use
    Boolean mAllowCloseOnClick;

    // Submission info
    LinearLayout mSubmissionInfo;
    TextView mTextViewAuthor;
    TextView mTextViewVoteCount;
    TextView mCommentCount;

    // Vote bar
    LinearLayout mVotebar;
    ImageView mButtonUpvote;
    ImageView mButtonDownvote;
    ImageView mButtonSave;
    ImageView mButtonOverflow;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submission_viewer);

        // Read relevant permission settings
        SharedPreferences prefs = getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        mAllowCloseOnClick = prefs.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);

        /* Refresh layout */
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.submission_viewer_refresh_layout);
        mRefreshLayout.setDistanceToTriggerSync(Constants.REFRESH_PULL_TOLERANCE);
        /* Title(s)*/
        mSubmissionTitle = (TextView) findViewById(R.id.submission_viewer_title);

        /* The actual view in which the image is displayed*/
        mImageViewContainer = (FrameLayout) findViewById(R.id.submission_viewer_media_container);
        mImageView = (ImageView) findViewById(R.id.submission_media_view);
        mPlayButton = (ImageView) findViewById(R.id.submission_viewer_play_button);

        /* Loading progress bar */
        mProgressBar = (ProgressBar) findViewById(R.id.submission_viewer_media_progress);

        /* Post info : Vote count, comment count, author */
        mSubmissionInfo = (LinearLayout) findViewById(R.id.submission_viewer_submission_info);
        mTextViewAuthor = (TextView) findViewById(R.id.submission_viewer_author);
        mTextViewVoteCount = (TextView) findViewById(R.id.submission_viewer_vote_count);
        mCommentCount = (TextView) findViewById(R.id.submission_viewer_comment_count);

        /* Upvote/downvote/save/overflow*/
        mVotebar = (LinearLayout) findViewById(R.id.submission_viewer_vote_bar);
        mButtonUpvote = (ImageView) findViewById(R.id.submission_viewer_commit_upvote);
        mButtonDownvote = (ImageView) findViewById(R.id.submission_viewer_commit_downvote);
        mButtonSave = (ImageView) findViewById(R.id.submission_viewer_commit_save);
        mButtonOverflow = (ImageView) findViewById(R.id.submission_viewer_votebar_overflow);

        unpackExtras();
        setupToolbar();
        setupMedia();
        setupSubmissionInfoBar();
        setupVotebar();

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSimpleMediaDisplay();
            }
            // closeMediaPlayer();
        });
    }


    private void unpackExtras() {
        mCurrSubmission = (SubmissionObj) getIntent().getExtras().get(Constants.EXTRA_SUBMISSION_OBJ);
    }

    private void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        ActionBar toolbar = getSupportActionBar();
        toolbar.show();
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setHomeAsUpIndicator(R.drawable.ic_white_back_arrow);
        toolbar.setTitle("/r/" + mCurrSubmission.getSubreddit());
    }

    private void setupMedia() {
        mSubmissionTitle.setText(mCurrSubmission.getTitle());

        // Prioritize using the cleaned URL. Some post URLS point to indirect images:
        // Indirect url example: imgur.com/AktjAWe
        // Cleaned url example: imgur.com/AktjAWe.jpg
        String imageUrl = (mCurrSubmission.getCleanedUrl() != null)
                ? mCurrSubmission.getCleanedUrl() : mCurrSubmission.getUrl();


        if(mCurrSubmission.getSubmissionType() == Constants.SubmissionType.IMAGE){
            mPlayButton.setVisibility(View.GONE);
           // mExoplayer.setVisibility(View.GONE);
            //TODO: handle invalid URL
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new ProgressBarRequestListener(mProgressBar))
                    .into(mImageView);
        }else if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.GIF
                || mCurrSubmission.getSubmissionType() == Constants.SubmissionType.VIDEO){
            mPlayButton.setVisibility(View.VISIBLE);
            String videoSnapshotUrl = mCurrSubmission.getPreviewUrl();

            Glide.with(this)
                    .load(videoSnapshotUrl)
                    .listener(new ProgressBarRequestListener(mProgressBar))
                    .into(mImageView);
           /* mImageView.setVisibility(View.GONE);
            mExoplayer.setVisibility(View.VISIBLE);
            initializePlayer(imageUrl);*/
        }
    }

    private void setupSubmissionInfoBar(){
        mTextViewAuthor.setText(mCurrSubmission.getAuthor());
        mTextViewVoteCount.setText(String.valueOf(mCurrSubmission.getScore()));
        mCommentCount.setText(String.valueOf(mCurrSubmission.getCommentCount()));
    }

    private void setupVotebar(){
        // change icon color depending on of user has voted on this already (only for logged in users)
    }

    private void openSimpleMediaDisplay(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment simpleViewerFragment = FragmentSimpleImageDisplay.newInstance();

        Bundle args = new Bundle();
        args.putSerializable(Constants.ARGS_SUBMISSION_OBJ, mCurrSubmission);
        simpleViewerFragment.setArguments(args);


       // int parentContainerId = ((ViewGroup) getView().getParent()).getId();
        ft.add(R.id.submission_viewer_simple_display_container,
                simpleViewerFragment,
                Constants.TAG_FRAG_SIMPLE_DISPLAYER);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_submission_view_header,menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
              /*  FragmentSimpleImageDisplay simpleImageDisplayFragment
                        = (FragmentSimpleImageDisplay) getSupportFragmentManager()
                        .findFragmentByTag(Constants.TAG_FRAG_SIMPLE_DISPLAYER);
                if(simpleImageDisplayFragment != null && simpleImageDisplayFragment.isVisible()){
                    getSupportFragmentManager().popBackStack();
                }*/
             //   else{
                    finish();
                //}
                return true;
            case R.id.menu_submission_view_comments_sortby:
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCloseClickDetected() {
        if(mAllowCloseOnClick) {
            getSupportFragmentManager().popBackStack();
        }
    }
}