package com.poyo.barcode.Model;

import com.poyo.barcode.Model.Product;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.io.Serializable;

@Root(strict = false)
public class EbayProduct implements Serializable {
    //TODO - this can inherit from Product once I figure out any more information needed from result

    //Name (mName) is located in item/title
    //ImageURL (mThumbnailImage) is located in galleryURL
    //Price (mSalePrice) is located in sellingStatus/convertedCurrentPrice
    //Shipping is in shippingInfo/shippingServiceCost
    //UPC (mUpc) is not included
    //URL (mSalesLink) is located in viewItemURL

    //This is not returned in the XML
    private String mUpc;

    @Element(name = "title", required = false)
    @Path("searchResult/item")
    private String mName;

    @Element(name = "galleryURL", required = false)
    @Path("searchResult/item")
    private String mThumbnailImage;

    @Element(name = "viewItemURL", required = false)
    @Path("searchResult/item")
    private String mSalesLink;

    @Element(name = "convertedCurrentPrice", required = false)
    @Path("searchResult/item/sellingStatus")
    private double mSalesPrice;

    @Element(name = "shippingServiceCost", required = false)
    @Path("searchResult/item/shippingInfo")
    private double mStandardShipRate;


    //Convert to product
    public Product convertToProduct() {
        String retailer = "Ebay";
        return new Product(this.mName, this.mThumbnailImage, mSalesPrice, mStandardShipRate, this.mUpc, this.mSalesLink, retailer);
    }


    public void setmUpc(String upc){
        this.mUpc=upc;
    }

}
