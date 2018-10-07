package com.sometimestwo.moxie.Model;

import net.dean.jraw.tree.CommentNode;

public class CommentObj {
    public CommentObj(){}
    public CommentObj(CommentNode comment) {
        this.comment = comment;
    }

    public CommentNode comment;

    public String getName(){
        return "";
    }
}
