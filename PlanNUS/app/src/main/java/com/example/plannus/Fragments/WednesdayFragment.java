package com.example.plannus.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.plannus.Adaptors.CalendarAdapter;
import com.example.plannus.Adaptors.ViewPagerAdapter;
import com.example.plannus.Fragments.ChildFragments.MondayClassFragment;
import com.example.plannus.Fragments.ChildFragments.MondayTaskFragment;
import com.example.plannus.Fragments.ChildFragments.WednesdayClassFragment;
import com.example.plannus.Fragments.ChildFragments.WednesdayTaskFragment;
import com.example.plannus.Objects.NUSClass;
import com.example.plannus.R;
import com.example.plannus.SessionManager;
import com.example.plannus.WrapContentLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.Query;

public class WednesdayFragment extends Fragment {
    private SessionManager sessionManager;
    private String userID;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private CalendarAdapter adapter;
    private RecyclerView recyclerView;
    public WednesdayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_monday, container, false);
        View view = inflater.inflate(R.layout.fragment_wednesday, container, false);
        sessionManager = SessionManager.get();
        userID = sessionManager.getUserID();
        addFragment(view);
        return view;
    }

    private void addFragment(View view) {
        tabLayout = view.findViewById(R.id.tabWednesday);
        viewPager = view.findViewById(R.id.pagerWednesday);
        ViewPagerAdapter adaptor = new ViewPagerAdapter(getChildFragmentManager(), getLifecycle());
        adaptor.addFragment(new WednesdayClassFragment(), "Wednesday Class");
        adaptor.addFragment(new WednesdayTaskFragment(), "Wednesday Task");
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