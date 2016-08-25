package ru.pes.observer.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.pes.observer.activities.MainActivity;
import ru.pes.observer.R;


public class MainFragment extends Fragment{

    private Button btnStart, btnStop;

    public static MainFragment newInstance(AppCompatActivity act) {
        return new MainFragment();
    }

    public MainFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_main, container, false);
        btnStart = (Button) result.findViewById(R.id.btnStart);
        btnStop = (Button) result.findViewById(R.id.btnStop);
        if (getActivity() != null) {
            MainActivity act = (MainActivity) getActivity();
            btnStart.setOnClickListener(act);
            btnStop.setOnClickListener(act);
        }
        return result;
    }

}
