package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sometimestwo.moxie.Utils.Constants;

public class ActivitySettings extends Activity{
    private final String TAG = this.getClass().getCanonicalName();

    private Button mBackButton;
    private TextView mTextViewAllowNSFW;
    private TextView mTextViewAllowGifPreview;
    private CheckBox mCheckboxAllowNSFW;
    private CheckBox mCheckboxPreviewGIFHover;
    SharedPreferences prefs_settings;
    SharedPreferences.Editor prefs_settings_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs_settings = this.getSharedPreferences(Constants.KEY_GETPREFS_SETTINGS, Context.MODE_PRIVATE);
        prefs_settings_editor = prefs_settings.edit();

        /* Set up check box values */
        mCheckboxAllowNSFW = (CheckBox) findViewById(R.id.settings_checkbox_nsfw);
        String allowNSFW = prefs_settings.getString(Constants.KEY_ALLOW_NSFW,null);
        // default NSFW to no if no setting found
        allowNSFW = allowNSFW == null ? Constants.SETTINGS_NO : allowNSFW;
        mCheckboxAllowNSFW.setChecked(Constants.SETTINGS_YES.equalsIgnoreCase(allowNSFW));
        mCheckboxAllowNSFW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
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

        mCheckboxPreviewGIFHover = (CheckBox) findViewById(R.id.settings_checkbox_gif_hover_preview);
        String previewGifHover = prefs_settings.getString(Constants.KEY_GIF_PLAY_HOVER,null);
        previewGifHover = previewGifHover == null ? Constants.SETTINGS_NO : previewGifHover;
        mCheckboxPreviewGIFHover.setChecked(Constants.SETTINGS_YES.equalsIgnoreCase(previewGifHover));
        mCheckboxPreviewGIFHover.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs_settings_editor.putString(Constants.KEY_GIF_PLAY_HOVER, b ? Constants.SETTINGS_YES : Constants.SETTINGS_NO);
            }
        });
        // textview clicks will trigger checkbox checking
        mTextViewAllowGifPreview = (TextView) findViewById(R.id.settings_textview_gif_preview);
        mTextViewAllowGifPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckboxPreviewGIFHover.setChecked(!mCheckboxPreviewGIFHover.isChecked());
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
        prefs_settings_editor.commit();
    }


}

