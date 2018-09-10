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
import android.widget.TextView;
import android.widget.Toast;

import com.sometimestwo.moxie.Utils.Constants;

public class ActivitySettings extends Activity{
    private final String TAG = this.getClass().getCanonicalName();

    private Button mBackButton;
    private TextView mTextViewAllowNSFW;
    private TextView mTextViewAllowGifPreview;
    private TextView mTextViewAllowImagePreview;

    private CheckBox mCheckboxAllowNSFW;
    private CheckBox mCheckboxPreviewGIF;
    private CheckBox mCheckboxPreviewImage;

    // tracks whether anything was clicked on
    private boolean mModified = false;

    SharedPreferences prefs_settings;
    SharedPreferences.Editor prefs_settings_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs_settings = this.getSharedPreferences(Constants.KEY_GETPREFS_SETTINGS, Context.MODE_PRIVATE);
        prefs_settings_editor = prefs_settings.edit();

        /* Set up check box values */

        // NSFW
        mCheckboxAllowNSFW = (CheckBox) findViewById(R.id.settings_checkbox_nsfw);
        String allowNSFW = prefs_settings.getString(Constants.KEY_ALLOW_NSFW,null);
        // default NSFW to no if no setting found
        allowNSFW = allowNSFW == null ? Constants.SETTINGS_NO : allowNSFW;
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
        mCheckboxPreviewGIF = (CheckBox) findViewById(R.id.settings_checkbox_gif_preview);
        String previewGif = prefs_settings.getString(Constants.KEY_GIF_PREVIEW,null);
        previewGif = previewGif == null ? Constants.SETTINGS_NO : previewGif;
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

        //Image previewing
        mCheckboxPreviewImage = (CheckBox) findViewById(R.id.settings_checkbox_image_preview);
        String previewImage = prefs_settings.getString(Constants.KEY_IMAGE_PREVIEW,null);
        previewImage = previewImage == null ? Constants.SETTINGS_NO : previewImage;
        mCheckboxPreviewImage.setChecked(Constants.SETTINGS_YES.equalsIgnoreCase(previewImage));
        mCheckboxPreviewImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mModified = true;
                prefs_settings_editor.putString(Constants.KEY_IMAGE_PREVIEW, b ? Constants.SETTINGS_YES : Constants.SETTINGS_NO);
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
        if(mModified){
            Toast.makeText(this, getResources()
                    .getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show();
        }

        prefs_settings_editor.commit();
        Intent intent = getIntent();
        //TODO: Need to inform MainActivity of any changes that may require reload i.e. allowing NSFW
        //intent.putExtra(Constants.NUM_GALLERIE_DIRS_CHOSEN, chosenDirectories.size());
        setResult(RESULT_OK, intent);
        finish();
    }


}

