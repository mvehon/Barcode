package com.poyo.barcode.Fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.poyo.barcode.Adapter.ProductRecyclerAdapter;
import com.poyo.barcode.MainActivity;
import com.poyo.barcode.Model.Product;
import com.poyo.barcode.R;
import com.poyo.barcode.Model.Retailer;

import java.util.ArrayList;

//The main fragment that loads when MainActivity loads
@SuppressLint("InflateParams")
@SuppressWarnings("unchecked")
public class MainFragment extends Fragment implements View.OnClickListener {
    private EditText editText;
    private LinearLayout mainTop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        View v = inflater.inflate(R.layout.main, container, false);

        mainTop = (LinearLayout) v.findViewById(R.id.mainTop);
        mainTop.addView(getActivity().getLayoutInflater().inflate(R.layout.main_top_buttons, null));


        LinearLayout scan = (LinearLayout) v.findViewById(R.id.scan);
        LinearLayout editLayout = (LinearLayout) v.findViewById(R.id.editLayout);

        scan.setOnClickListener(this);
        editLayout.setOnClickListener(this);


        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        // use a linear layout manager

        RecyclerView.LayoutManager mLayoutManager;

        ArrayList<Product> abridgedHistory = (ArrayList<Product>) getArguments().get(getString(R.string.historyItemList));
        ArrayList<Retailer> retailers = (ArrayList<Retailer>) getArguments().get(getString(R.string.retailers));

        RecyclerView.Adapter mAdapter;
        assert abridgedHistory != null;
        if (abridgedHistory.size() > 0) {
            if (abridgedHistory.size() > 5) {
                abridgedHistory.subList(5, abridgedHistory.size()).clear();
            }
            mLayoutManager = new LinearLayoutManager(getActivity());
            mAdapter = new ProductRecyclerAdapter(getActivity().getApplicationContext(), abridgedHistory);
        } else {
            TextView text = (TextView) v.findViewById(R.id.recently_viewed);
            text.setText(R.string.supported_retailers);

            // use a grid layout manager
            mLayoutManager = new GridLayoutManager(getActivity(), 2);   //2 columns
            mAdapter = new GridRecyclerAdapter(retailers);
        }


        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                MainActivity.doBarcodeScan();
                break;
            case R.id.editLayout:
                mainTop.removeAllViews();
                View view = getActivity().getLayoutInflater().inflate(R.layout.main_top_edit, null);
                mainTop.addView(view);


                ImageView help = (ImageView) mainTop.findViewById(R.id.imageView3);
                help.setOnClickListener(this);

                editText = (EditText) mainTop.findViewById(R.id.editText2);
                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            String product_code = editText.getText().toString();
                            if (validProductCode(product_code)) {
                                MainActivity.resetToBasic(product_code);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                break;
            case R.id.imageView3:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Look for a number under or near the barcode of the product you want to search.\nFor books this will be a 10 or 13 digit number (ignore any dashes), and for other products this will be 6 or 12 digits.\nThis application currently only supports UPC-A, UPC-E, ISBN-10 and ISBN-13 for searches.").setTitle("Product Code");
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
    }

    private boolean validProductCode(String product_code) {
        int product_code_length = product_code.length();
        if (product_code_length == 6 //UPC-E
                || product_code_length == 10 //ISBN
                || product_code_length == 12 //UPC-A
                || product_code_length == 13 //ISBN-13
                ) {
            return true;
        } else {
            Toast.makeText(getActivity(), "The code you entered is not valid", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    class GridRecyclerAdapter extends RecyclerView.Adapter<GridRecyclerAdapter.ViewHolder> {
        private final ArrayList<Retailer> retailers;

        GridRecyclerAdapter(ArrayList<Retailer> ilist) {
            this.retailers = ilist;
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ViewHolder(View v) {
                super(v);
                imageView = (ImageView) v.findViewById(R.id.retail_logo);
            }
        }

        @Override
        public GridRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.retail_icon_recycler, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.imageView.setImageResource(retailers.get(position).getImg_id());

        }

        @Override
        public int getItemCount() {
            return retailers.size();
        }

    }
}
