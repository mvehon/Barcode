package com.poyo.barcode.Fragment;
/*
 * Created by Matthew on 8/8/2016.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.poyo.barcode.Adapter.ProductRecyclerAdapter;
import com.poyo.barcode.MainActivity;
import com.poyo.barcode.Model.Product;
import com.poyo.barcode.R;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class SavedFragment extends Fragment {

    private ArrayList<Product> savedItemList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        savedItemList = (ArrayList<Product>) getArguments().get("savedItemList");
        showSaved();

        return v;

    }


    private void showSaved() {
        if (savedItemList.isEmpty()) {
            MainActivity.noResults();
        } else {
            RecyclerView.Adapter mAdapter = new ProductRecyclerAdapter(getActivity().getApplicationContext(), savedItemList);
            recyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).onSavedUpdated(savedItemList);
    }

}