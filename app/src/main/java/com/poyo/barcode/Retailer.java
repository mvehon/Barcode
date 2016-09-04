package com.poyo.barcode;

import java.io.Serializable;

public class Retailer implements Serializable {

    private String name;
    private String endpoint;
    private String return_type;
    private String api_key;
    private Boolean showRetailer;
    private int img_id;
    //private String secret_key;
    //private String affiliate;

/*
    ex - Walmart
    name="Walmart"
    endpoint="http://api.walmartlabs.com/v1/items?apiKey={apiKey}&upc={upc_code}"

    ex - Amazon
    name = "Amazon"
    endpoint= http://webservices.amazon.com/onca/xml
    Service=AWSECommerceService&
    AWSAccessKeyId=[AWS Access Key ID]&
    AssociateTag=[Associate ID]&
    Operation=ItemLookup&
    ItemId=[UPC]&                                             REQ
    SearchIndex=Books&                                        REQ (use All)
    IdType=UPC&
    Timestamp=[YYYY-MM-DDThh:mm:ssZ]&
    Signature=[Request Signature]


    ex - Best Buy
    endpoint = https://api.bestbuy.com/v1/products
*/


    Retailer(String name, String endpoint, String api_key, String return_type, int img_id) {
        this.name = name;
        this.endpoint = endpoint;
        this.return_type = return_type;
        this.api_key = api_key;
        this.showRetailer = true;
        this.img_id = img_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApi_key() {
        return api_key;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getReturn_type() {
        return return_type;
    }

    void setShowRetailer(Boolean bool) {
        this.showRetailer = bool;
    }

    public Boolean getShowRetailer() {
        return showRetailer;
    }

    public int getImg_id() {
        return img_id;
    }
}
