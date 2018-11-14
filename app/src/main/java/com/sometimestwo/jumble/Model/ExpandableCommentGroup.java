package com.sometimestwo.jumble.Model;

import com.sometimestwo.jumble.Utils.Constants;
import com.xwray.groupie.ExpandableGroup;
import com.xwray.groupie.Group;

import net.dean.jraw.models.Comment;
import net.dean.jraw.tree.CommentNode;

import java.util.List;

@SuppressWarnings("unchecked")
public class ExpandableCommentGroup extends ExpandableGroup {
    public ExpandableCommentGroup(Group expandableItem, boolean isExpanded, SubmissionObj currSubmission) {
        super(expandableItem, isExpanded);

        List<CommentNode<Comment>> replies = ((CommentObj) expandableItem).comment.getReplies();
        for(CommentNode<Comment> c : replies){
            CommentObj child = new CommentObj(c,currSubmission);
            // expand if root comment or if comment score threshold is met
            boolean expandChild = (child.comment.getSubject().getScore() >= Constants.COMMENTS_MIN_SCORE);
            add(new ExpandableCommentGroup(child,expandChild,currSubmission));
        }
    }
}
