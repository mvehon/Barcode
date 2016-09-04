package com.poyo.barcode.Adapter;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.poyo.barcode.Product;
import com.poyo.barcode.R;

import java.text.NumberFormat;
import java.util.ArrayList;

public class RetailRecyclerAdapter extends RecyclerView.Adapter<RetailRecyclerAdapter.ViewHolder> {
    private final ArrayList itemlist;
    private final Context context;
    private Resources res;

    public RetailRecyclerAdapter(Context context, ArrayList ilist) {
        this.context = context;
        this.itemlist = ilist;
        res = context.getResources();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_icon;
        TextView item_price;
        LinearLayout retail_parent;

        ViewHolder(View v) {
            super(v);
            Log.d("Recycler", "Making item viewholder");
            this.item_icon = (ImageView) v.findViewById(R.id.item_icon);
            this.item_price = (TextView) v.findViewById(R.id.item_price);
            this.retail_parent = (LinearLayout) v.findViewById(R.id.retail_parent);
        }
    }

    @Override
    public RetailRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.retail_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        final Product curItem = (Product) itemlist.get(position);
        viewHolder.item_icon.setImageDrawable(ResourcesCompat.getDrawable(res, curItem.setRetailerImage(), null));
        viewHolder.item_price.setText(formatter.format(curItem.getSalePrice()));
        viewHolder.retail_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(curItem.getSalesLink()));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemlist.size();
    }


}