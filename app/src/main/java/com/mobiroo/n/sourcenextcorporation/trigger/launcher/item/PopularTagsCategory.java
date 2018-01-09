package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class PopularTagsCategory {
    private final String mName;
    private final List<ExampleTask> mTags;

    public PopularTagsCategory(String name, ImmutableList<ExampleTask> tags) {
        mName = name;
        mTags = tags;
    }

    public String getName() {
        return mName;
    }

    public List<ExampleTask> getTags() {
        return mTags;
    }
}
