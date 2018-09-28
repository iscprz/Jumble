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

/*09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: FATAL EXCEPTION: main
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: Process: com.sometimestwo.moxie, PID: 27891
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: java.lang.RuntimeException: Unable to start activity ComponentInfo{com.sometimestwo.moxie/com.sometimestwo.moxie.ActivityHome}: java.lang.IllegalStateException: No current authenticated client
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2858)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2933)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.ActivityThread.-wrap11(Unknown Source:0)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1612)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.os.Handler.dispatchMessage(Handler.java:105)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.os.Looper.loop(Looper.java:164)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.ActivityThread.main(ActivityThread.java:6710)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at java.lang.reflect.Method.invoke(Native Method)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:770)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: Caused by: java.lang.IllegalStateException: No current authenticated client
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at net.dean.jraw.oauth.AccountHelper.getReddit(AccountHelper.kt:54)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at com.sometimestwo.moxie.ExpandableListAdapter.<init>(ExpandableListAdapter.java:25)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at com.sometimestwo.moxie.FragmentHome.populateExpandableList(FragmentHome.java:837)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at com.sometimestwo.moxie.FragmentHome.setupLeftNavView(FragmentHome.java:593)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at com.sometimestwo.moxie.FragmentHome.onCreateView(FragmentHome.java:239)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.Fragment.performCreateView(Fragment.java:2442)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1460)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.FragmentManagerImpl.moveFragmentToExpectedState(FragmentManager.java:1784)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1852)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.FragmentManagerImpl.dispatchStateChange(FragmentManager.java:3269)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.FragmentManagerImpl.dispatchActivityCreated(FragmentManager.java:3229)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.FragmentController.dispatchActivityCreated(FragmentController.java:201)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v4.app.FragmentActivity.onStart(FragmentActivity.java:620)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.support.v7.app.AppCompatActivity.onStart(AppCompatActivity.java:178)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1334)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.Activity.performStart(Activity.java:6999)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2821)
09-28 07:12:05.878 10349 27891 27891 E AndroidRuntime: 	... 9 more*/
    private String mCurrLoggedInUser = App.getAccountHelper().getReddit().getAuthManager().currentUsername();

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
        String currLoggedInUser = App.getAccountHelper().getReddit().getAuthManager().currentUsername();

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
