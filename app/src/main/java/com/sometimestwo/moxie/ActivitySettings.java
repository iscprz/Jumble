package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sometimestwo.moxie.Utils.Constants;

public class ActivitySettings extends Activity {
    private final String TAG = this.getClass().getCanonicalName();

    // Back arrow
    private ImageView mBackButton;

    // NSFW settings
    private LinearLayout mBlockNSFW;
    private CheckBox mCheckboxAllowNSFW;

    // Image preview
    private LinearLayout mBlockAllowPreviewer;
    private CheckBox mCheckboxPreviewImage;

    // Radio buttons to select previewer size
    private RadioGroup mRadioGroupPreviewSize;

    // Big display
    private LinearLayout mBlockBigDisplayCloseClick;
    private CheckBox mCheckboxAllowBigDisplayCloseClick;

    // Media icons
    private LinearLayout mBlockMediaIcon;
    private CheckBox mCheckboxMediaIcon;

    // tracks whether anything was clicked on
    private boolean mModified = false;
    // True when a "restart required" setting has been changed
    private boolean mNeedsRefresh = false;
    private boolean mLogoutEvent = false;

    SharedPreferences prefs_settings;
    SharedPreferences.Editor prefs_settings_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs_settings = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        prefs_settings_editor = prefs_settings.edit();


        // NSFW
        mCheckboxAllowNSFW = (CheckBox) findViewById(R.id.settings_allow_nsfw_checkbox);
        boolean allowNSFW = prefs_settings.getBoolean(Constants.KEY_ALLOW_NSFW, false);
        mCheckboxAllowNSFW.setChecked(allowNSFW);
        mCheckboxAllowNSFW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxAllowNSFW.isChecked();
                //mCheckboxAllowNSFW.setChecked(!oldValue);
                prefs_settings_editor.putBoolean(Constants.KEY_ALLOW_NSFW, b);
                mModified = true;
                mNeedsRefresh = true;
            }
        });

        mBlockNSFW = (LinearLayout) findViewById(R.id.settings_block_allow_nsfw);
        mBlockNSFW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxAllowNSFW.setChecked(!mCheckboxAllowNSFW.isChecked());
            }
        });


        /* Close big display on click*/
        mCheckboxAllowBigDisplayCloseClick = (CheckBox) findViewById(R.id.settings_bigdisplay_closeclick_checkbox);
        boolean closeOnClick = prefs_settings.getBoolean(Constants.KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
        mCheckboxAllowBigDisplayCloseClick.setChecked(closeOnClick);
        mCheckboxAllowBigDisplayCloseClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxAllowBigDisplayCloseClick.isChecked();
                prefs_settings_editor.putBoolean(Constants.KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK, b);
                mModified = true;
            }
        });

        mBlockBigDisplayCloseClick = (LinearLayout) findViewById(R.id.settings_block_bigdisplay_closeclick);
        mBlockBigDisplayCloseClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mCheckboxAllowBigDisplayCloseClick.setChecked(!mCheckboxAllowBigDisplayCloseClick.isChecked());
            }
        });


        /* Previewing */
        mCheckboxPreviewImage = (CheckBox) findViewById(R.id.settings_allow_previewer_checkbox);
        boolean allowPreviewImage = prefs_settings.getBoolean(Constants.KEY_ALLOW_HOVER_PREVIEW, false);
        mCheckboxPreviewImage.setChecked(allowPreviewImage);
        mCheckboxPreviewImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxPreviewImage.isChecked();
                prefs_settings_editor.putBoolean(Constants.KEY_ALLOW_HOVER_PREVIEW, b);
                mModified = true;
            }
        });

        mBlockAllowPreviewer = (LinearLayout) findViewById(R.id.settings_block_allow_previewer);
        mBlockAllowPreviewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxPreviewImage.setChecked(!mCheckboxPreviewImage.isChecked());
            }
        });


        /* Preview size radio buttons */
        mRadioGroupPreviewSize = (RadioGroup) findViewById(R.id.radio_group_preview_size);
        String previewSizeSelection = prefs_settings.getString(Constants.KEY_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_SMALL);
        // check prefs for what user has selected for preview size and initialize radio buttons accordingly
        mRadioGroupPreviewSize.check(previewSizeSelection.equalsIgnoreCase(Constants.SETTINGS_PREVIEW_SIZE_SMALL)
                ? R.id.radio_preview_size_option_small : R.id.radio_preview_size_option_large);



        /* Display media icons */
        mCheckboxMediaIcon = (CheckBox) findViewById(R.id.settings_media_icon_checkbox);
        boolean allowMediaIcons = prefs_settings.getBoolean(Constants.KEY_SETTINGS_ALLOW_MEDIA_ICON, true);
        mCheckboxMediaIcon.setChecked(allowMediaIcons);
        mCheckboxMediaIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //boolean newValue = mCheckboxPreviewImage.isChecked();
                prefs_settings_editor.putBoolean(Constants.KEY_SETTINGS_ALLOW_MEDIA_ICON, b);
                mModified = true;
            }
        });

        mBlockMediaIcon = (LinearLayout) findViewById(R.id.settings_block_display_media_icon);
        mBlockMediaIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxMediaIcon.setChecked(!mCheckboxMediaIcon.isChecked());
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
    public void onBackPressed() {
        super.onBackPressed();

        // Technically we can modify changes AND log out but logout will take precedence here
        if (mLogoutEvent) {
            Toast.makeText(this, getResources()
                    .getString(R.string.toast_settings_logout_success), Toast.LENGTH_SHORT).show();
        } else if (mModified) {
            Toast.makeText(this, getResources()
                    .getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show();
        }

        prefs_settings_editor.commit();
        Intent intent = getIntent();
        //TODO: Need to inform ActivityMain of any changes that may require reload i.e. allowing NSFW
        //intent.putExtra(Constants.NUM_GALLERIE_DIRS_CHOSEN, chosenDirectories.size());
        if (mModified && mNeedsRefresh) {
            setResult(Constants.RESULT_OK_INVALIDATE_DATA, intent);
        } else if (mModified) {
            setResult(RESULT_OK, intent);

        }
        finish();
    }

    // preview size radio buttons
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radio_preview_size_option_large:
                if (checked) {
                    prefs_settings_editor.putString(Constants.KEY_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_LARGE);
                    mModified = true;
                }
                break;
            case R.id.radio_preview_size_option_small:
                if (checked) {
                    prefs_settings_editor.putString(Constants.KEY_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_SMALL);
                    mModified = true;
                }
                break;
        }
    }

    public String getRadioOption() {
        return "";
    }

}

