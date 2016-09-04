package com.poyo.barcode.Adapter;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.poyo.barcode.MainActivity;
import com.poyo.barcode.Product;
import com.poyo.barcode.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ViewHolder> {
    private final ArrayList itemlist;
    private final Context context;

    public ProductRecyclerAdapter(Context context, ArrayList ilist) {
        this.context = context;
        this.itemlist = ilist;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_icon;
        TextView item_name;
        TextView item_price;
        TextView item_date;
        CardView product_parent;

        ViewHolder(View v) {
            super(v);
            this.item_icon = (ImageView) v.findViewById(R.id.item_icon);
            this.item_name = (TextView) v.findViewById(R.id.item_name);
            this.item_price = (TextView) v.findViewById(R.id.item_price);
            this.item_date = (TextView) v.findViewById(R.id.item_date);
            this.product_parent = (CardView) v.findViewById(R.id.card_view);
        }
    }

    @Override
    public ProductRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Product curItem = (Product) itemlist.get(position);
        Picasso.with(context).load(curItem.getThumbnailImage()).into(viewHolder.item_icon);
        viewHolder.item_name.setText(curItem.getName());
        viewHolder.item_date.setText(curItem.getTime());
        viewHolder.item_price.setText(String.format(context.getString(R.string.best_price), curItem.getSalePrice()));
        viewHolder.product_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.resetToBasic(curItem.getUpc());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemlist.size();
    }

}