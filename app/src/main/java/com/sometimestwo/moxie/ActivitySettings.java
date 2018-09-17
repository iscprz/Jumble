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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sometimestwo.moxie.Utils.Constants;

public class ActivitySettings extends Activity {
    private final String TAG = this.getClass().getCanonicalName();

    private Button mBackButton;
    private TextView mTextViewAllowNSFW;
    private TextView mTextViewAllowGifPreview;
    private TextView mTextViewAllowImagePreview;
    private TextView mTextViewAllowBigDisplayCloseClick;

    private CheckBox mCheckboxAllowNSFW;
    //private CheckBox mCheckboxPreviewGIF;
    private CheckBox mCheckboxPreviewImage;
    private CheckBox mCheckboxAllowBigDisplayCloseClick;

    private RadioGroup mRadioGroupPreviewSize;

    // tracks whether anything was clicked on
    private boolean mModified = false;

    SharedPreferences prefs_settings;
    SharedPreferences.Editor prefs_settings_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs_settings = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        prefs_settings_editor = prefs_settings.edit();

        /* Set up check box values */

        // NSFW
        mCheckboxAllowNSFW = (CheckBox) findViewById(R.id.settings_checkbox_nsfw);
        String allowNSFW = prefs_settings.getString(Constants.KEY_ALLOW_NSFW, Constants.SETTINGS_NO);
        mCheckboxAllowNSFW.setChecked(Constants.SETTINGS_YES.equalsIgnoreCase(allowNSFW));
        mCheckboxAllowNSFW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mModified = true;
                prefs_settings_editor.putString(Constants.KEY_ALLOW_NSFW, b ? Constants.SETTINGS_YES : Constants.SETTINGS_NO);
            }
        });
        // textview clicks will trigger checkbox checking
        mTextViewAllowNSFW = (TextView) findViewById(R.id.settings_textview_nsfw);
        mTextViewAllowNSFW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxAllowNSFW.setChecked(!mCheckboxAllowNSFW.isChecked());
            }
        });

        // GIF previewing
       /* mCheckboxPreviewGIF = (CheckBox) findViewById(R.id.settings_checkbox_gif_preview);
        String previewGif = prefs_settings.getString(Constants.KEY_GIF_PREVIEW, Constants.SETTINGS_NO);
        mCheckboxPreviewGIF.setChecked(Constants.SETTINGS_YES.equalsIgnoreCase(previewGif));
        mCheckboxPreviewGIF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mModified = true;
                prefs_settings_editor.putString(Constants.KEY_GIF_PREVIEW, b ? Constants.SETTINGS_YES : Constants.SETTINGS_NO);
            }
        });
        // textview clicks will trigger checkbox checking
        mTextViewAllowGifPreview = (TextView) findViewById(R.id.settings_textview_gif_preview);
        mTextViewAllowGifPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxPreviewGIF.setChecked(!mCheckboxPreviewGIF.isChecked());
            }
        });
*/


        /* Close big display on click*/
        mCheckboxAllowBigDisplayCloseClick = (CheckBox) findViewById(R.id.settings_checkbox_bigdisplay_close);
        String closeOnClick = prefs_settings.getString(Constants.KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK, Constants.SETTINGS_NO);
        mCheckboxAllowBigDisplayCloseClick.setChecked(Constants.SETTINGS_YES.equalsIgnoreCase(closeOnClick));
        mCheckboxAllowBigDisplayCloseClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mModified = true;
                prefs_settings_editor.putString(Constants.KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK, b ? Constants.SETTINGS_YES : Constants.SETTINGS_NO);
            }
        });
        // textview clicks will trigger checkbox checking
        mTextViewAllowBigDisplayCloseClick = (TextView) findViewById(R.id.settings_textview_bigdisplay_close);
        mTextViewAllowBigDisplayCloseClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxAllowBigDisplayCloseClick.setChecked(!mCheckboxAllowBigDisplayCloseClick.isChecked());
            }
        });

        /* Previewing */
        mCheckboxPreviewImage = (CheckBox) findViewById(R.id.settings_checkbox_image_preview);
        String previewImage = prefs_settings.getString(Constants.KEY_ALLOW_HOVER_PREVIEW, Constants.SETTINGS_NO);
        mCheckboxPreviewImage.setChecked(Constants.SETTINGS_YES.equalsIgnoreCase(previewImage));
        mCheckboxPreviewImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mModified = true;
                prefs_settings_editor.putString(Constants.KEY_ALLOW_HOVER_PREVIEW, b ? Constants.SETTINGS_YES : Constants.SETTINGS_NO);
            }
        });
        // textview clicks will trigger checkbox checking
        mTextViewAllowImagePreview = (TextView) findViewById(R.id.settings_textview_image_preview);
        mTextViewAllowImagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxPreviewImage.setChecked(!mCheckboxPreviewImage.isChecked());
            }
        });

        // Preview size radio buttons
        mRadioGroupPreviewSize = (RadioGroup) findViewById(R.id.radio_group_preview_size);
        String previewSizeSelection = prefs_settings.getString(Constants.KEY_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_SMALL);
        // check prefs for what user has selected for preview size and initialize radio buttons accordingly
        mRadioGroupPreviewSize.check(previewSizeSelection.equalsIgnoreCase(Constants.SETTINGS_PREVIEW_SIZE_SMALL)
                ? R.id.radio_preview_size_option_small : R.id.radio_preview_size_option_large);



        /* Exit settings */
        mBackButton = (Button) findViewById(R.id.settings_back);
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
        if (mModified) {
            Toast.makeText(this, getResources()
                    .getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show();
        }

        prefs_settings_editor.commit();
        Intent intent = getIntent();
        //TODO: Need to inform ActivityMain of any changes that may require reload i.e. allowing NSFW
        //intent.putExtra(Constants.NUM_GALLERIE_DIRS_CHOSEN, chosenDirectories.size());
        setResult(RESULT_OK, intent);
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

    public String getRadioOption(){
        return "";
    }

}

