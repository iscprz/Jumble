package com.sometimestwo.moxie.Model;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sometimestwo.moxie.App;
import com.sometimestwo.moxie.R;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;
import com.xwray.groupie.ExpandableGroup;
import com.xwray.groupie.ExpandableItem;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import net.dean.jraw.tree.CommentNode;


public class CommentObj extends Item implements ExpandableItem {
    SubmissionObj currSubmission; // reference to the submission which this comment is for
    public CommentObj() {
    }

    public CommentObj(CommentNode comment) {
        this.comment = comment;
    }
    public CommentObj(CommentNode comment, SubmissionObj currSubmission) {
        this.comment = comment;
        this.currSubmission = currSubmission;
    }

    public CommentNode comment;

    private ExpandableGroup expandableGroup;

    @Override
    public int getLayout() {
        return R.layout.recycler_item_comment;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        RelativeLayout commentMasterContainer = (RelativeLayout) viewHolder.getRoot().findViewById(R.id.comment_item_master_container);
        RelativeLayout commentInfoContainer = (RelativeLayout) viewHolder.getRoot().findViewById(R.id.comment_info_container);
        ImageView commentCollapseButton = (ImageView) viewHolder.getRoot().findViewById(R.id.comment_button_collapse);
        TextView commentAuthorTextView = (TextView) viewHolder.getRoot().findViewById(R.id.comment_item_author);
        TextView commentScoreTextView = (TextView) viewHolder.getRoot().findViewById(R.id.comment_item_score);
        TextView commentTimeSubmittedTextView = (TextView)viewHolder.getRoot().findViewById(R.id.comment_item_time_submitted);
        TextView commentNumRepliesTextView = (TextView) viewHolder.getRoot().findViewById(R.id.comment_item_num_replies);
        ImageView commentGoldStarImageView = (ImageView) viewHolder.getRoot().findViewById(R.id.comment_item_gilded);
        TextView commentGoldCount = (TextView) viewHolder.getRoot().findViewById(R.id.comment_item_gold_count);
        TextView commentBodyTextView = (TextView) viewHolder.getRoot().findViewById(R.id.comment_item_body);

        /* Init comment*/
        // Collapse button color
        commentCollapseButton.setBackgroundColor(App.getAppResources().getColor(getCommentColor(comment.getDepth())));
        // Author
        commentAuthorTextView.setText(comment.getSubject().getAuthor());
        // Check if Author of this comment is OP
        commentAuthorTextView.setTextColor(getAuthorTextColor(comment.getSubject().getAuthor()));
        // Score
        commentScoreTextView.setText(getScoreText(comment.getSubject().getScore(),comment.getSubject().isScoreHidden()));
        // Time submitted
        commentTimeSubmittedTextView.setText(Utils.getStringTimestamp(comment.getSubject().getCreated().getTime()));
        // Num replies (displayed when item is collapsed)
        commentNumRepliesTextView.setText(getNumRepliesText(comment.getReplies().size()));
        // Gold star if gilded
        commentGoldStarImageView.setBackground(comment.getSubject().getGilded() > 0
                ? App.getAppResources().getDrawable(R.drawable.ic_yellow_star_filled_2_big) : null);
        // Gilded counter
        commentGoldCount.setText(getGoldCountText(comment.getSubject().getGilded()));
        // Comment text body
        commentBodyTextView.setText(comment.getSubject().getBody());


        // indentation
        if(comment.getDepth() == 1){
            // root comment only gets 5 left padding
            viewHolder.itemView.setPadding(
                    Constants.COMMENTS_INDENTATION_PADDING_ROOT,
                    0,
                    20,
                    0);
        }
        else{
            viewHolder.itemView.setPadding(
                    (comment.getDepth()-1) * Constants.COMMENTS_INDENTATION_PADDING,
                    0,
                    20,
                    0);
        }



        // Hide body portion of comment if comment is deemed collapsed at this point.
        if (!expandableGroup.isExpanded()){
            commentBodyTextView.setVisibility(View.GONE);
        }

        if (expandableGroup.isExpanded()) {
            commentNumRepliesTextView.setVisibility(View.GONE);
        }

        // collapse/uncollapse
        commentInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandableGroup.onToggleExpanded();
                if (expandableGroup.isExpanded()) {
                    commentBodyTextView.setVisibility(View.VISIBLE);
                    commentNumRepliesTextView.setVisibility(View.GONE);
                }
                // collapsed
                else {
                    commentBodyTextView.setVisibility(View.GONE);
                    commentNumRepliesTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void setExpandableGroup(@NonNull ExpandableGroup onToggleListener) {
        expandableGroup = onToggleListener;
    }

    private String getNumRepliesText(int numReplies) {
        StringBuilder sb = new StringBuilder();
        if (numReplies == 0) return sb.append("").toString();
        else if (numReplies == 1) return sb.append("1 reply").toString();
        else return sb.append(numReplies).append(" replies").toString();
    }

    private String getScoreText(int score, boolean isScoreHidden) {
        StringBuilder sb = new StringBuilder();
        if(isScoreHidden) return sb.append("score hidden").toString();
        else if (score == 1) return sb.append("1point").toString();
        else return sb.append(score).append("points").toString();
    }

    private int getCommentColor(int depth) {
        depth %= 5;
        switch (depth) {
            case 0:
                return R.color.comments_yellow;
            case 1:
                return R.color.comments_blue;
            case 2:
                return R.color.comments_red;
            case 3:
                return R.color.comments_green;
            case 4:
                return R.color.comments_purple;
            default:
                // should not happen
                return R.color.comments_orange;
        }
    }


    private String getGoldCountText(short goldCount){
        StringBuilder sb = new StringBuilder();
        if(goldCount <= 1) return "";
        else return sb.append("x").append(String.valueOf(goldCount)).toString();
    }

    private int getAuthorTextColor(String commentAuthor){
        if(commentAuthor.equalsIgnoreCase(currSubmission.getAuthor()))
            return App.getAppResources().getColor(R.color.comments_OP);
        else
            return App.getAppResources().getColor(R.color.colorWhite);
    }
}
