package com.poyo.barcode;

import com.google.gson.annotations.SerializedName;


import java.io.Serializable;

//sample upc#1 028400199148 - lays potato chips
//sample upc#2 600603185212 - insignia tv

public class Product implements Serializable {
    private String time;

    @SerializedName(value = "name", alternate = {"Title", "title"})
    private String mName;

    @SerializedName(value = "thumbnailImage", alternate = {"image", "galleryURL"})
    private String mThumbnailImage;

    @SerializedName(value = "salePrice", alternate = {"__value__", "salePrice3"})
    private double mSalePrice;

    //@SerializedName(value="standardShipRate", alternate={"shippingCost", "standardShipRate3"})
    private double mStandardShipRate = 0.0;

    @SerializedName(value = "upc", alternate = {"upc2", "upc3"})
    private String mUpc;

    @SerializedName(value = "addToCartUrl", alternate = {"linkShareAffiliateUrl", "viewItemURL"})
    //TODO change the first to affiliateAddToCartUrl once I get an affiliate for Walmart
    private String mSalesLink;

    @SerializedName(value = "source", alternate = {"source2", "source3"})
    private String retailer;


    public Product(String mName, String mThumbnailImage, double mSalePrice, double mStandardShipRate, String mUpc, String mSalesLink, String retailer) {
        this.mName = mName;
        this.mThumbnailImage = mThumbnailImage;
        this.mSalePrice = mSalePrice;
        this.mStandardShipRate = mStandardShipRate;
        this.mUpc = mUpc;
        this.mSalesLink = mSalesLink;
        this.retailer = retailer;
    }


    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getThumbnailImage() {
        return mThumbnailImage;
    }

    public double getSalePrice() {
        return mSalePrice;
    }

    public double getStandardShipRate() {
        return mStandardShipRate;
    }

    public String getUpc() {
        return mUpc;
    }

    public String getSalesLink() {
        return this.mSalesLink;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    //Change retailer to proper name
    public void setRetailer() {
        if (this.getThumbnailImage().contains("walmart")) {
            this.retailer = "Walmart";
        } else if (this.retailer.equals("bestbuy")) {
            this.retailer = "BestBuy";
        }
    }

    //Return the image for the retailer
    public int setRetailerImage() {
        switch (this.retailer) {
            case ("Walmart"):
                return R.drawable.logo_walmart;
            case ("BestBuy"):
                return R.drawable.logo_bestbuy;
            case ("Amazon"):
                return R.drawable.logo_amazon;
            case ("Ebay"):
                return R.drawable.logo_ebay;
        }

        return R.drawable.transparent;
    }

    //Trim very long titles
    public void normalizeTitleLength() {
        if (this.mName.length() > 100) {
            this.mName = this.mName.substring(0, 100);
        }
    }
}
