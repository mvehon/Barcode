package com.poyo.barcode.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//TODO - use this to create a selector for multiple different product results (ex: UPC 078742012322 returns results for both Great Value diced peaches and Fiber One granola bars)
//Create list of items returned from product API call
public class ProductList {

    @SerializedName(value="items", alternate={"products", "product"})
        private List<Product> items;


    public Product getItem(int i) {
        return items.get(i);
    }
}
