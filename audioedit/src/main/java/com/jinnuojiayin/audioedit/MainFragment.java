package com.jinnuojiayin.audioedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * @author hesong
 * @time 2018/1/9
 * @desc
 */

public class MainFragment extends Fragment {

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public void navigateTo(FragmentActivity activity, int parentViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .add(parentViewId, this, getClass().getSimpleName())
                .addToBackStack(getClass().getSimpleName())
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, null);

        view.findViewById(R.id.btn_cut_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CutFragment.newInstance().navigateTo(getActivity(), R.id.fl_content);
            }
        });
        view.findViewById(R.id.btn_insert_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InsertFragment.newInstance().navigateTo(getActivity(), R.id.fl_content);
            }
        });
        view.findViewById(R.id.btn_mix_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MixFragment.newInstance().navigateTo(getActivity(), R.id.fl_content);
            }
        });

        return view;
    }


}
