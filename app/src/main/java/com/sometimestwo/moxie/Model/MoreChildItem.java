package com.sometimestwo.moxie.Model;

import net.dean.jraw.models.MoreChildren;
import net.dean.jraw.tree.CommentNode;

public class MoreChildItem extends CommentObj {
    public MoreChildren children;

    public MoreChildItem(CommentNode node, MoreChildren children) {
        comment = node;
        this.children = children;
    }
    @Override
    public String getName() {
        return comment.getSubject().getFullName() + "more";
//        return comment.getComment().getFullName() + "more";
    }
}
