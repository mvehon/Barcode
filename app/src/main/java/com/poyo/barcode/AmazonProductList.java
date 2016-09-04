package com.poyo.barcode;

import android.util.Log;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Root(strict = false)
public class AmazonProductList implements Serializable {

    @ElementList(entry = "Item", inline = true)
    @Path("Items")
    private List<AmazonProduct> itemList;

    //Get the cheapest available listing
    public AmazonProduct getBest() {
        Collections.sort(itemList, new AmazonPriceComparator());
        removeInvalidListings();
        return itemList.get(0);
    }

    //Remove the unavailable items
    private void removeInvalidListings() {
        for (int i = 0; i < itemList.size(); i++) {
            Log.d("i:", String.valueOf(i) + "");
            Log.d("mName:", itemList.get(i).mName + "");
            Log.d("formattedprice:", itemList.get(i).formattedprice + "");
            Log.d("thumbnail:", itemList.get(i).mThumbnailImage + "");
            Log.d("msaleslink:", itemList.get(i).mSalesLink + "");
            Log.d("convertedformat:", String.valueOf(itemList.get(i).convertFormattedPrice()) + "");

            //if the price is 0 or there's no image, it's probably not a valid listing
            if (itemList.get(i).convertFormattedPrice() <= 0 || itemList.get(i).mThumbnailImage == null) {
                itemList.remove(i);
                i--;
            }
        }
    }

    //Sort list by price
    private static class AmazonPriceComparator implements Comparator<AmazonProduct> {
        @Override
        public int compare(AmazonProduct o1, AmazonProduct o2) {
            return Double.compare(o1.convertFormattedPrice(), o2.convertFormattedPrice());
        }
    }
}
