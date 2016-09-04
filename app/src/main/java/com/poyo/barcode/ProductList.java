package com.poyo.barcode;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//Create list of items returned from product API call
public class ProductList {

    @SerializedName(value="items", alternate={"products", "product"})
        private List<Product> items;


    public Product getItem(int i) {
        return items.get(i);
    }
}
