package com.example.plannus.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.plannus.Adaptors.ViewPagerAdapter;
import com.example.plannus.Fragments.ChildFragments.MondayClassFragment;
import com.example.plannus.Fragments.ChildFragments.MondayTaskFragment;
import com.example.plannus.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MondayFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public MondayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monday, container, false);
        addFragment(view);
        return view;
    }

    private void addFragment(View view) {
        tabLayout = view.findViewById(R.id.tabMonday);
        viewPager = view.findViewById(R.id.pagerMonday);
        ViewPagerAdapter adaptor = new ViewPagerAdapter(getChildFragmentManager(), getLifecycle());
        adaptor.addFragment(new MondayClassFragment(), "Monday Class");
        adaptor.addFragment(new MondayTaskFragment(), "Monday Task");
        viewPager.setAdapter(adaptor);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Class" : "Tasks");
        }).attach();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}