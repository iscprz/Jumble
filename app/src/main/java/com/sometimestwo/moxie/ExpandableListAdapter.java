package com.sometimestwo.moxie;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sometimestwo.moxie.Model.ExpandableMenuModel;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<ExpandableMenuModel> listDataHeader;
    private HashMap<ExpandableMenuModel, List<ExpandableMenuModel>> listDataChild;

    public ExpandableListAdapter(Context context, List<ExpandableMenuModel> listDataHeader,
                                 HashMap<ExpandableMenuModel, List<ExpandableMenuModel>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
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

        final String childText = getChild(groupPosition, childPosition).menuName;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_child, null);
        }

        TextView txtListChild = convertView.findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
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
        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageView lblListHeaderIcon = convertView.findViewById(R.id.list_group_icon);
        lblListHeaderIcon.setBackground(getMenuHeaderIcon(convertView,headerTitle));
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

    private Drawable getMenuHeaderIcon(View convertView, String headerTitle){
        if(convertView.getResources().getString(R.string.menu_accounts).equalsIgnoreCase(headerTitle)){
            return convertView.getResources().getDrawable(R.drawable.ic_white_person_no_bg);
        }
        else if(convertView.getResources().getString(R.string.menu_goto_subreddit).equalsIgnoreCase(headerTitle)) {
            return convertView.getResources().getDrawable(R.drawable.ic_send_it);
        }
        else if(convertView.getResources().getString(R.string.menu_settings).equalsIgnoreCase(headerTitle)) {
            return convertView.getResources().getDrawable(R.drawable.ic_white_settings);
        }
        else{
            return null;
        }
    }
}
