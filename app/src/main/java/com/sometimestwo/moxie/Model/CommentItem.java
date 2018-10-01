package com.sometimestwo.moxie.Model;

import net.dean.jraw.tree.CommentNode;

public class CommentItem extends CommentObj {


    public CommentItem(CommentNode node) {
        comment = node;
    }

    @Override
    public String getName() {
        return comment.getSubject().getFullName();
        //return comment.getComment().getFullName();
    }

    @Override
    public boolean isComment() {
        return true;
    }

}