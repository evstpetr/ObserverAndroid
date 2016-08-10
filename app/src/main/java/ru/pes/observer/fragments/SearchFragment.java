package ru.pes.observer.fragments;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import ru.pes.observer.MainActivity;
import ru.pes.observer.R;

public class SearchFragment extends Fragment {

    private ListView list;
    private Button btnSave, btnSearch;
    private ProgressBar bar;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_search, container, false);
        list = (ListView) result.findViewById(R.id.lvSensors);
        btnSave = (Button) result.findViewById(R.id.btnSave);
        btnSearch = (Button) result.findViewById(R.id.btnSearch);
        bar = (ProgressBar) result.findViewById(R.id.progressBar);
        bar.setVisibility(View.INVISIBLE);
        if (getActivity() != null) {
            MainActivity act = (MainActivity) getActivity();
            btnSearch.setOnClickListener(act);
            btnSave.setOnClickListener(act);
        }
        return result;
    }

}
