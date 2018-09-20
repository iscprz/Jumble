package com.sometimestwo.moxie.Model;

public class ExpandableMenuModel {

    public String menuName;
    public boolean hasChildren, isGroup;

    public ExpandableMenuModel(String menuName, boolean isGroup, boolean hasChildren) {

        this.menuName = menuName;
        this.isGroup = isGroup;
        this.hasChildren = hasChildren;
    }
}

