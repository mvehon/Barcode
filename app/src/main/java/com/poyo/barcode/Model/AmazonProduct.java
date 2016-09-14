package com.poyo.barcode.Model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class AmazonProduct {
    //Name (mName) is located in ItemAttributes{Title}
    //ImageURL (mThumbnailImage) is located in SmallImage{URL}
    //Price (mSalePrice) is located in Offers{Offer{OfferListing{Price{FormattedPrice(as a string with a $ leading)}}}
    //Shipping is a no-go
    //UPC (mUpc) is in ItemAttributes{UPC}
    //URL (mSalesLink) is located in DetailPageURL

    @Element(name = "UPC", required = false)
    @Path("ItemAttributes")
    private String mUpc;

    @Element(name = "ISBN", required = false)
    @Path("ItemAttributes")
    private String mISBN;

    @Element(name = "Title", required = false)
    @Path("ItemAttributes")
    String mName;

    @Element(name = "URL", required = false)
    @Path("SmallImage")
    String mThumbnailImage;

    @Element(name = "DetailPageURL", required = false)
    String mSalesLink;

    @Element(name = "FormattedPrice", required = false)
    @Path("Offers/Offer/OfferListing/Price")
    String formattedprice;

    //Convert this object to regular Product
    public Product convertToProduct() {
        if (mUpc == null) {
            mUpc = mISBN;
        }
        String retailer = "Amazon";
        return new Product(this.mName, this.mThumbnailImage, convertFormattedPrice(), 0.0, this.mUpc, this.mSalesLink, retailer);
    }


    //Convert the price String to double
    double convertFormattedPrice() {
        String x = this.formattedprice;
        try {
            //Remove dollar sign
            x = x.substring(1);
        } catch (NullPointerException e) {
            x = "0.0";
        }
        return Double.valueOf(x);
    }

}
