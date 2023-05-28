package com.cxzcodes.statussaver.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cxzcodes.statussaver.Fragments.ImageFragment;
import com.cxzcodes.statussaver.Fragments.SavedFilesFragment;
import com.cxzcodes.statussaver.Fragments.VideoFragment;

public class PageAdapter extends FragmentPagerAdapter {

    private final int totalTabs;

    public PageAdapter(@NonNull FragmentManager fm, int totalTabs) {
        super(fm);
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if (position == 0)
            return new ImageFragment();

        if (position == 1)
            return new VideoFragment();

        return new SavedFilesFragment();

    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
