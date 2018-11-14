package com.sometimestwo.jumble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sometimestwo.jumble.Utils.Constants;

public class ActivitySettings extends Activity {
    private final String TAG = this.getClass().getCanonicalName();
    boolean mHideNSFW;

    // Colors for quick reference
    int mColorWhite;
    int mColorGrayout;

    // Back arrow
    private ImageView mBackButton;

    // Optimize (exclude non-media)
    private LinearLayout mLLOptimize;
    private CheckBox mCheckboxOptimize;

    // NSFW settings
    private LinearLayout mLLNSFW;
    private CheckBox mCheckboxHideNSFW;

    // NSFW thumbnail hidden overlay
    private LinearLayout mLLNSFWOverlay;
    private CheckBox mCheckboxNSFWOverlay;
    private TextView mTitleNSFWOverlay;
    private TextView mSubtitleNSFWOverlay;

    // Image preview
    private LinearLayout mLLAllowPreviewer;
    private CheckBox mCheckboxPreviewImage;

    // Big display
    private LinearLayout mLLBigDisplayCloseClick;
    private CheckBox mCheckboxAllowBigDisplayCloseClick;

    // Domain icons
    private LinearLayout mLLDomainIcon;
    private CheckBox mCheckboxDomainIcon;

    // Filetype icons
    private LinearLayout mLLFiletypeIcon;
    private CheckBox mCheckboxFiletypeIcon;

    // NSFW icon
    private LinearLayout mLLNSFWIcon;
    private CheckBox mCheckboxNSFWIcon;
    private TextView mTitleNSFWIcon;
    private TextView mSubtitleNSFWIcon;


    // tracks whether anything was clicked on
    private boolean mModified = false;

    SharedPreferences prefs_settings;
    SharedPreferences.Editor prefs_settings_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs_settings = this.getSharedPreferences(Constants.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        prefs_settings_editor = prefs_settings.edit();
        mHideNSFW = prefs_settings.getBoolean(Constants.PREFS_HIDE_NSFW, true);
        mColorWhite = getResources().getColor(R.color.colorWhite);
        mColorGrayout = getResources().getColor(R.color.colorGray);

        // Optimize
        mCheckboxOptimize = (CheckBox) findViewById(R.id.settings_optimize_checkbox);
        boolean optimize = prefs_settings.getBoolean(Constants.PREFS_FILTER_OPTIMIZE, true);
        mCheckboxOptimize.setChecked(optimize);
        mCheckboxOptimize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxAllowBigDisplayCloseClick.isChecked();
                prefs_settings_editor.putBoolean(Constants.PREFS_FILTER_OPTIMIZE, b);
                mModified = true;
            }
        });

        mLLOptimize = (LinearLayout) findViewById(R.id.settings_LL_optimize);
        mLLOptimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxOptimize.setChecked(!mCheckboxOptimize.isChecked());
            }
        });

        // NSFW
        mCheckboxHideNSFW = (CheckBox) findViewById(R.id.settings_hide_nsfw_checkbox);
        mCheckboxHideNSFW.setChecked(mHideNSFW);
        mCheckboxHideNSFW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs_settings_editor.putBoolean(Constants.PREFS_HIDE_NSFW, b);
                mModified = true;
                mHideNSFW = b;

                // depend on the state of NSFW submissions being allowed
                setupNSFWOverlayLL(b);
                setupNSFWIconLL(b);
            }
        });

        mLLNSFW = (LinearLayout) findViewById(R.id.settings_LL_hide_nsfw);
        mLLNSFW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxHideNSFW.setChecked(!mCheckboxHideNSFW.isChecked());
            }
        });


        /****** NSFW thumbnail overlay ******/
        mCheckboxNSFWOverlay = (CheckBox) findViewById(R.id.settings_nsfw_overlay_checkbox);
        mLLNSFWOverlay = (LinearLayout) findViewById(R.id.settings_LL_nsfw_overlay);
        // Title and subtitle text views to control their text color
        mTitleNSFWOverlay = (TextView) findViewById(R.id.settings_title_nsfw_overlay);
        mSubtitleNSFWOverlay = (TextView) findViewById(R.id.settings_nsfw_overlay_subtitle);

        boolean hideNSFWThumbnails = prefs_settings.getBoolean(Constants.PREFS_HIDE_NSFW_THUMBS, false);
        mCheckboxNSFWOverlay.setChecked(hideNSFWThumbnails);

        // Will need to "disable" this LL if NSFW posts are not allowed
        setupNSFWOverlayLL(mCheckboxHideNSFW.isChecked());

        mCheckboxNSFWOverlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxPreviewImage.isChecked();
                prefs_settings_editor.putBoolean(Constants.PREFS_HIDE_NSFW_THUMBS, b);
                mModified = true;
            }
        });

        mLLNSFWOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only care about clicks if NSFW submissions are allowed
                if(!mHideNSFW) {
                    mCheckboxNSFWOverlay.setChecked(!mCheckboxNSFWOverlay.isChecked());
                }
            }
        });


        /********** Close big display on click*********/
        mCheckboxAllowBigDisplayCloseClick = (CheckBox) findViewById(R.id.settings_bigdisplay_closeclick_checkbox);
        boolean closeOnClick = prefs_settings.getBoolean(Constants.PREFS_ALLOW_CLOSE_CLICK, true);
        mCheckboxAllowBigDisplayCloseClick.setChecked(closeOnClick);
        mCheckboxAllowBigDisplayCloseClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxAllowBigDisplayCloseClick.isChecked();
                prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_CLOSE_CLICK, b);
                mModified = true;
            }
        });

        mLLBigDisplayCloseClick = (LinearLayout) findViewById(R.id.settings_LL_bigdisplay_closeclick);
        mLLBigDisplayCloseClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxAllowBigDisplayCloseClick.setChecked(!mCheckboxAllowBigDisplayCloseClick.isChecked());
            }
        });


        /* Previewing */
        mCheckboxPreviewImage = (CheckBox) findViewById(R.id.settings_allow_previewer_checkbox);
        boolean allowPreviewImage = prefs_settings.getBoolean(Constants.PREFS_ALLOW_HOVER_PREVIEW, true);
        mCheckboxPreviewImage.setChecked(allowPreviewImage);
        mCheckboxPreviewImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxPreviewImage.isChecked();
                prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_HOVER_PREVIEW, b);
                mModified = true;
            }
        });

        mLLAllowPreviewer = (LinearLayout) findViewById(R.id.settings_LL_allow_previewer);
        mLLAllowPreviewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxPreviewImage.setChecked(!mCheckboxPreviewImage.isChecked());
            }
        });


        /* Preview size radio buttons */
      /*  mRadioGroupPreviewSize = (RadioGroup) findViewById(R.id.radio_group_preview_size);
        String previewSizeSelection = prefs_settings.getString(Constants.PREFS_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_SMALL);
        // check prefs_settings for what user has selected for preview size and initialize radio buttons accordingly
        mRadioGroupPreviewSize.check(previewSizeSelection.equalsIgnoreCase(Constants.SETTINGS_PREVIEW_SIZE_SMALL)
                ? R.id.radio_preview_size_option_small : R.id.radio_preview_size_option_large);
*/


        /* Display domain icons */
        mCheckboxDomainIcon = (CheckBox) findViewById(R.id.settings_domain_icon_checkbox);
        boolean displayDomainIcons = prefs_settings.getBoolean(Constants.PREFS_ALLOW_DOMAIN_ICON, false);
        mCheckboxDomainIcon.setChecked(displayDomainIcons);
        mCheckboxDomainIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxPreviewImage.isChecked();
                prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_DOMAIN_ICON, b);
                mModified = true;
            }
        });

        mLLDomainIcon = (LinearLayout) findViewById(R.id.settings_LL_display_domain_icon);
        mLLDomainIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxDomainIcon.setChecked(!mCheckboxDomainIcon.isChecked());
            }
        });



        /****** Display filetype icons *******/
        mCheckboxFiletypeIcon = (CheckBox) findViewById(R.id.settings_filetype_icon_checkbox);
        boolean displayFiletypeIcon = prefs_settings.getBoolean(Constants.PREFS_ALLOW_FILETYPE_ICON, true);
        mCheckboxFiletypeIcon.setChecked(displayFiletypeIcon);

        mCheckboxFiletypeIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxPreviewImage.isChecked();
                prefs_settings_editor.putBoolean(Constants.PREFS_ALLOW_FILETYPE_ICON, b);
                mModified = true;
            }
        });

        mLLFiletypeIcon = (LinearLayout) findViewById(R.id.settings_LL_filetype_icon);
        mLLFiletypeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxFiletypeIcon.setChecked(!mCheckboxFiletypeIcon.isChecked());
            }
        });


        /** NSFW icon  **/
        mCheckboxNSFWIcon = (CheckBox) findViewById(R.id.settings_nsfw_icon_checkbox);
        mLLNSFWIcon = (LinearLayout) findViewById(R.id.settings_LL_nsfw_icon);
        // Title and subtitle text views to control their text color
        mTitleNSFWIcon = (TextView) findViewById(R.id.settings_title_nsfw_icon);
        mSubtitleNSFWIcon = (TextView) findViewById(R.id.settings_nsfw_icon_subtitle);

        boolean showNSFWIcon = prefs_settings.getBoolean(Constants.PREFS_SHOW_NSFW_ICON, false);
        mCheckboxNSFWIcon.setChecked(showNSFWIcon);
        /*mTitleNSFWIcon.setTextColor(mHideNSFW ? mColorWhite
                : mColorGrayout);*/

        // Will need to "disable" this LL if NSFW posts are not allowed
        setupNSFWIconLL(mCheckboxHideNSFW.isChecked());

        mCheckboxNSFWIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxPreviewImage.isChecked();
                prefs_settings_editor.putBoolean(Constants.PREFS_SHOW_NSFW_ICON, b);
                mModified = true;
            }
        });

        mLLNSFWIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only care about clicks if NSFW submissions are allowed
                if(!mHideNSFW) {
                    mCheckboxNSFWIcon.setChecked(!mCheckboxNSFWIcon.isChecked());
                }
            }
        });

        /* Exit settings */
        mBackButton = (ImageView) findViewById(R.id.settings_button_back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        // User can become unauthenticated when inactive(tabbed out of app) for a long period (1hour).
        String mostRecentUser = prefs_settings.getString(Constants.MOST_RECENT_USER, Constants.USERNAME_USERLESS);
        if(!App.getAccountHelper().isAuthenticated()){
            if (!Constants.USERNAME_USERLESS.equalsIgnoreCase(mostRecentUser)) {
                App.getAccountHelper().switchToUser(mostRecentUser);
            }
            else {
                App.getAccountHelper().switchToUserless();
            }
        }
        super.onResume();
    }

    // set up "cover up NSFW submission thumbnails"
    private void setupNSFWOverlayLL(boolean checked) {
        if (checked) {
            mTitleNSFWOverlay.setTextColor(mColorGrayout);
            mSubtitleNSFWOverlay.setTextColor(mColorGrayout);
            mCheckboxNSFWOverlay.setEnabled(false);
        } else {
            mTitleNSFWOverlay.setTextColor(mColorWhite);
            mSubtitleNSFWOverlay.setTextColor(mColorWhite);
            mCheckboxNSFWOverlay.setEnabled(true);
            // Color issues with disabled check boxes:
            //https://stackoverflow.com/questions/5854047/how-to-change-the-color-of-a-checkbox/31354418
        }
    }


    // set up "show NSFW icons"
    private void setupNSFWIconLL(boolean checked) {
        if (checked) {
            mTitleNSFWIcon.setTextColor(mColorGrayout);
            mSubtitleNSFWIcon.setTextColor(mColorGrayout);
            mCheckboxNSFWIcon.setEnabled(false);
        } else {
            mTitleNSFWIcon.setTextColor(mColorWhite);
            mSubtitleNSFWIcon.setTextColor(mColorWhite);
            mCheckboxNSFWIcon.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mModified) {
            Toast.makeText(this, getResources()
                    .getString(R.string.toast_settings_saved_refresh), Toast.LENGTH_SHORT).show();
        }

        prefs_settings_editor.commit();
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }
}

