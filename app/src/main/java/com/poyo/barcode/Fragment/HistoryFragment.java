package com.poyo.barcode.Fragment;
/*
 * Created by Matthew on 8/8/2016.
 */

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.poyo.barcode.Adapter.ProductRecyclerAdapter;
import com.poyo.barcode.MainActivity;
import com.poyo.barcode.Model.Product;
import com.poyo.barcode.R;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class HistoryFragment extends Fragment {

    private ArrayList<Product> historyItemList = new ArrayList<>();
    private Snackbar snackBar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);


        //Make snackbar to clear history
        snackBar = Snackbar.make(getActivity().findViewById(R.id.drawer_layout), "", Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction(R.string.clear_history, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyItemList.clear();
                Toast.makeText(getActivity(), R.string.history_cleared, Toast.LENGTH_SHORT).show();
                ((MainActivity) getActivity()).popBackStack();
            }
        });


        historyItemList = (ArrayList<Product>) getArguments().get(getString(R.string.historyItemList));

        mAdapter = new ProductRecyclerAdapter(getActivity().getApplicationContext(), historyItemList);
        recyclerView.setAdapter(mAdapter);


        showHistory();
        return v;
    }


    private void showHistory() {
        //If history itemlist is empty, close and change to emptyfragment
        if (historyItemList.isEmpty()) {
            mAdapter.notifyDataSetChanged();
            snackBar.dismiss();
            MainActivity.noResults();
        } else {
            //Show the snackbar to clear history list
            snackBar.show();

            //Get layout params so that the bottom of the RecyclerView will not be covered by the snackbar
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, Math.round(48 * Resources.getSystem().getDisplayMetrics().density));
            recyclerView.setLayoutParams(params);

//            mAdapter = new ProductRecyclerAdapter(getActivity().getApplicationContext(), historyItemList);
//            recyclerView.setAdapter(mAdapter);
//            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Snackbar must be closed before changing fragments, or else it will continue to show
        snackBar.dismiss();
        ((MainActivity) getActivity()).onHistoryUpdated(historyItemList);
    }


}
