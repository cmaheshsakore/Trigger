package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BetterFragmentPagerAdapter extends FragmentPagerAdapter {
    protected final List<FragmentInfo> mFragments = new ArrayList<FragmentInfo>();

    private static final class FragmentInfo {
        private final Fragment mFragment;
        private final String   mTitle;

        private FragmentInfo(Fragment fragment, String title) {
            mFragment = fragment;
            mTitle = title;
        }
    }

    public BetterFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragments.add(new FragmentInfo(fragment, title));
    }
    
    public void removeFragment(int position) {
        mFragments.remove(position);
    }
    
    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position).mFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).mTitle.toUpperCase();
    }

}
