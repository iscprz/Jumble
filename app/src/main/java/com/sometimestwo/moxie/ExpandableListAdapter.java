package com.sometimestwo.moxie;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sometimestwo.moxie.Model.ExpandableMenuModel;
import com.sometimestwo.moxie.Utils.Constants;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<ExpandableMenuModel> listDataHeader;
    private HashMap<ExpandableMenuModel, List<ExpandableMenuModel>> listDataChild;
    private String mCurrLoggedInUser;

    public ExpandableListAdapter(Context context, List<ExpandableMenuModel> listDataHeader,
                                 HashMap<ExpandableMenuModel, List<ExpandableMenuModel>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        mCurrLoggedInUser = App.getSharedPrefs().getString(
                Constants.MOST_RECENT_USER,
                Constants.USERNAME_USERLESS);
    }

    @Override
    public ExpandableMenuModel getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String menuItem = getChild(groupPosition, childPosition).menuName;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_child, null);
        }

        TextView txtListChild = convertView.findViewById(R.id.expand_list_item);
        txtListChild.setText(menuItem);
        txtListChild.setTextColor(getMenuItemTextColor(convertView, menuItem));
        if ((convertView.getResources().getString(R.string.menu_add_account).equalsIgnoreCase(menuItem))) {
            SpannableString content = new SpannableString(menuItem);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            txtListChild.setText(content);
        }
        ImageView childLeftIconImageView = convertView.findViewById(R.id.list_child_icon_left);
        childLeftIconImageView.setBackground(getMenuIcon(convertView, menuItem));
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        if (this.listDataChild.get(this.listDataHeader.get(groupPosition)) == null)
            return 0;
        else
            return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                    .size();
    }

    @Override
    public ExpandableMenuModel getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();

    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition).menuName;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_header, null);
        }
        TextView lblListHeader = convertView.findViewById(R.id.expand_list_header);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageView lblListHeaderIcon = convertView.findViewById(R.id.list_group_icon);
        lblListHeaderIcon.setBackground(getMenuIcon(convertView, headerTitle));
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /* Gets menu icons for menu items in the left nav bar */
    private Drawable getMenuIcon(View convertView, String headerTitle) {


        if (convertView.getResources().getString(R.string.menu_accounts).equalsIgnoreCase(headerTitle)) {
            return convertView.getResources().getDrawable(R.drawable.ic_white_accounts);
        } else if (convertView.getResources().getString(R.string.menu_goto_subreddit).equalsIgnoreCase(headerTitle)) {
            return convertView.getResources().getDrawable(R.drawable.ic_send_it);
        } else if (convertView.getResources().getString(R.string.menu_settings).equalsIgnoreCase(headerTitle)) {
            return convertView.getResources().getDrawable(R.drawable.ic_white_settings);
        } else if(convertView.getResources().getString(R.string.menu_saved_submissions).equalsIgnoreCase(headerTitle)){
            return convertView.getResources().getDrawable(R.drawable.ic_yellow_star_filled_2);
        } else if (convertView.getResources().getString(R.string.menu_add_account).equalsIgnoreCase(headerTitle)) {
            return convertView.getResources().getDrawable(R.drawable.ic_white_add_account);
        } else if ((Constants.USERNAME_USERLESS_PRETTY).equalsIgnoreCase(headerTitle)) {
            if ((Constants.USERNAME_USERLESS).equalsIgnoreCase(mCurrLoggedInUser)) {
                return convertView.getResources().getDrawable(R.drawable.ic_blue_person_filled);
            }
            return convertView.getResources().getDrawable(R.drawable.ic_white_person_unfilled);
        } else if (App.getTokenStore().getUsernames().contains(headerTitle)) {
            if (headerTitle.equalsIgnoreCase(mCurrLoggedInUser)) {
                return convertView.getResources().getDrawable(R.drawable.ic_blue_person_filled);
            }
            return convertView.getResources().getDrawable(R.drawable.ic_white_person_filled);
        } else {
            return null;
        }
    }


    private int getMenuItemTextColor(View convertView, String headerTitle) {
        String currLoggedInUser =  App.getSharedPrefs().getString(
                Constants.MOST_RECENT_USER,
                Constants.USERNAME_USERLESS);//App.getAccountHelper().getReddit().getAuthManager().currentUsername();

        // swap username ugly to pretty for sake of comparing sharedpref details and menu item
        if (Constants.USERNAME_USERLESS.equalsIgnoreCase(currLoggedInUser)) {
            currLoggedInUser = Constants.USERNAME_USERLESS_PRETTY;
        }
        if (currLoggedInUser.equalsIgnoreCase(headerTitle)) {
            return convertView.getResources().getColor(R.color.colorAccentBlue);
        } else {
            return convertView.getResources().getColor(R.color.colorWhite);
        }
    }

}
