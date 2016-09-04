package com.poyo.barcode.Fragment;
/*
 * Created by Matthew on 8/8/2016.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.poyo.barcode.Adapter.RetailRecyclerAdapter;
import com.poyo.barcode.AmazonProductList;
import com.poyo.barcode.AmazonSignatureHelper;
import com.poyo.barcode.EbayProduct;
import com.poyo.barcode.MainActivity;
import com.poyo.barcode.Product;
import com.poyo.barcode.ProductList;
import com.poyo.barcode.R;
import com.poyo.barcode.Retailer;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressWarnings("unchecked")
public class ResultFragment extends Fragment {

    private ImageView product_image;
    private TextView product_name;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Product> savedItemList = new ArrayList<>();
    private ArrayList<Product> historyItemList = new ArrayList<>();
    private ArrayList<Product> itemList = new ArrayList<>();
    private ArrayList<Retailer> retailers = new ArrayList<>();
    private FloatingActionButton fab;
    private Boolean isSaved = false;
    private Resources res;
    private String productUpc;
    private ProgressDialog progress;
    private volatile int searchesDone;
    private int historyItemLimit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.result_layout, container, false);


        product_image = (ImageView) v.findViewById(R.id.product_image);
        product_name = (TextView) v.findViewById(R.id.product_name);

        //Set up Recyclerview - listview with product at retailers
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RetailRecyclerAdapter(getActivity(), itemList);
        recyclerView.setAdapter(mAdapter);

        res = getActivity().getResources();
        SharedPreferences prefs = getActivity().getSharedPreferences("com.poyo.barcode", Context.MODE_PRIVATE);

        //Max amount of searches to store in history list - default to 100
        historyItemLimit = prefs.getInt("historyItemLimit", 100);


        //Floating action button for users to save product
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSaved) {
                    addSavedProduct();
                    Toast.makeText(getActivity(), R.string.item_saved, Toast.LENGTH_SHORT).show();
                } else {
                    removeSavedProduct();
                    Toast.makeText(getActivity(), R.string.item_removed, Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Get product upc passed from Activity
        productUpc = getArguments().getString("productUpc", "");
        historyItemList = (ArrayList<Product>) getArguments().get(getString(R.string.historyItemList));
        savedItemList = (ArrayList<Product>) getArguments().get("savedItemList");
        retailers = (ArrayList<Retailer>) getArguments().get("retailers");

        Log.d("ProductUpc", productUpc);

        //If there is no upc, display message and change fragment
        if (productUpc.equals("")) {
            Toast.makeText(getActivity(), "No Barcode Detected", Toast.LENGTH_LONG).show();
            MainActivity.noResults();
        } else {
            //Otherwise, generate the product and check if it is in saved list
            isSaved = productIsInList(savedItemList, productUpc) >= 0;
            getProductResults(productUpc);
        }

        return v;
    }

    //Add the product to the saved list
    private void addSavedProduct() {
        savedItemList.add(0, itemList.get(0));
        //Set state to saved
        fab.setImageDrawable(ResourcesCompat.getDrawable(res, R.drawable.saved, null));
        isSaved = true;

    }

    private void removeSavedProduct() {
        //If product is in saved item list, remove it
        int x = productIsInList(savedItemList, itemList.get(0).getUpc());
        if (x >= 0) {
            savedItemList.remove(x);
        }

        //Set state to not saved
        fab.setImageDrawable(ResourcesCompat.getDrawable(res, R.drawable.unsaved, null));
        isSaved = false;
    }




    //Return index of item in memory by comparing UPC, returns -1 if not in list
    private int productIsInList(List<Product> itemList, String resultupc) {
        for (int i = 0; i < itemList.size(); i++) {
            Product product = itemList.get(i);
            if (product.getUpc().equals(resultupc)) {
                return i;
            }
        }
        return -1;
    }

    //This is called to generate a product
    private void getProductResults(String resultupc) {
        productUpc = resultupc;                                             //This is needed for Ebay items - since UPC code is not returned in the response
        searchesDone = 0;                                                   //This keeps track of progress for ProgressDialog
        progress = new ProgressDialog(getActivity());
        progress.setMessage("Searching retailers");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setProgress(searchesDone);
        progress.setMax(validRetailersSize());
        progress.show();

        //Show as saved if item is in saved item list
        if (productIsInList(savedItemList, resultupc) >= 0) {
            fab.setImageDrawable(ResourcesCompat.getDrawable(getActivity().getApplicationContext().getResources(), R.drawable.saved, null));
            isSaved = true;
        }

        //TODO if book, search the Google Books api to use for info, productCodeType(upc).equals(getString(R.string.isbn))

        //Call each of the retailer APIs
        for (int i = 0; i < retailers.size(); i++) {
            if (retailers.get(i).getShowRetailer()) {
                callAllRetailerAPI(i, resultupc);
            }
        }
    }


    //Contains the logic for all retailer APIs
    private void callAllRetailerAPI(int i, String upc) {
        productUpc = upc;
        Retailer retail = retailers.get(i);
        String tag = retail.getName();
        Log.d(tag, Integer.toString(i));

        SortedMap<String, String> sortedParamMap = new TreeMap<>();
        switch (tag) {
            case "Walmart":
                WalmartService wm = createRetrofitService(WalmartService.class, retail.getEndpoint(), retail.getReturn_type());
                wm.getProduct(retail.getApi_key(), upc).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(newSubscribe());
                break;
            case "BestBuy":
                BestBuyService bbs = createRetrofitService(BestBuyService.class, retail.getEndpoint(), retail.getReturn_type());
                bbs.getProduct(upc, retail.getApi_key(), retail.getReturn_type()).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(newSubscribe());
                break;
            case "Macy's":
                MacysService mcs = createRetrofitService(MacysService.class, retail.getEndpoint(), retail.getReturn_type());
                mcs.getProduct(retail.getApi_key(), upc).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(newSubscribe());
                break;
            case "Ebay":
                EbayService ebs = createRetrofitService(EbayService.class, retail.getEndpoint(), retail.getReturn_type());
                ebs.getProduct(getString(R.string.findItemsByProduct), retail.getApi_key(), retail.getReturn_type(), productCodeType(upc), upc).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(newEbaySubscribe());
                break;
            case "Amazon":
                //Add search paramaters for Amazon, must be sorted bitwise to generate signature
                AmazonService ams = createRetrofitService(AmazonService.class, retail.getEndpoint(), retail.getReturn_type());
                sortedParamMap.put(getString(R.string.AWSAccessKeyId), retail.getApi_key());
                sortedParamMap.put(getString(R.string.itemid), upc);
                sortedParamMap.put(getString(R.string.operation), getString(R.string.itemlookup));
                sortedParamMap.put(getString(R.string.associatetag), getString(R.string.amazon_associateid));
                sortedParamMap.put(getString(R.string.responsegroup), getString(R.string.large));
                sortedParamMap.put(getString(R.string.idtype), productCodeType(upc));
                if (productCodeType(upc).equals(getString(R.string.isbn))) {
                    sortedParamMap.put(getString(R.string.searchindex), getString(R.string.books));
                } else {
                    sortedParamMap.put(getString(R.string.searchindex), getString(R.string.all));
                }

                //Create a valid signature for Amazon api call
                AmazonSignatureHelper ash = new AmazonSignatureHelper(getString(R.string.awsSecretKey));
                ams.getProduct(sortedParamMap, ash.sign(sortedParamMap)).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(newAmazonSubscribe());
                break;
        }


    }

    //Return the type of product code
    private String productCodeType(String upc) {
        String type;
        Log.d("Product code type", Integer.toString(upc.length()));
        if (upc.contains("-") || upc.length() == 10 || upc.length() == 13) {
            type = getString(R.string.isbn);
        } else {
            type = getString(R.string.UPC_caps);
        }
        return type;
    }

    //Updates when ProductList created
    private Subscriber<ProductList> newSubscribe() {
        return new Subscriber<ProductList>() {
            @Override
            public final void onCompleted() {
                updateProgress();
            }

            @Override
            public final void onError(Throwable e) {
                Log.e("Error Retrieving", e.getMessage());
                updateProgress();
            }

            @Override
            public final void onNext(ProductList responses) {
                createProduct(responses.getItem(0));
            }
        };
    }

    //Updates when AmazonProductList created
    private Subscriber<AmazonProductList> newAmazonSubscribe() {
        return new Subscriber<AmazonProductList>() {
            @Override
            public final void onCompleted() {
                updateProgress();
            }

            @Override
            public final void onError(Throwable e) {
                Log.e("Amazon Error Retrieving", e.getMessage());
                updateProgress();
            }

            @Override
            public final void onNext(AmazonProductList responses) {
                //Convert Amazon product to product
                createProduct(responses.getBest().convertToProduct());
            }
        };
    }

    //Updates when EbayProduct created
    private Subscriber<EbayProduct> newEbaySubscribe() {
        return new Subscriber<EbayProduct>() {
            @Override
            public final void onCompleted() {
                updateProgress();
            }

            @Override
            public final void onError(Throwable e) {
                Log.e("Ebay Error Retrieving", e.getMessage());
                updateProgress();
            }

            @Override
            public final void onNext(EbayProduct responses) {
                responses.setmUpc(productUpc);
                createProduct(responses.convertToProduct());
            }
        };
    }

    //Update the progress dialog when API call finished
    private void updateProgress() {
        if (progress.isShowing()) {
            searchesDone++;
            if (searchesDone == validRetailersSize()) {
                progress.dismiss();
                if (itemList.isEmpty()) {
                    //If no products found, remove fragment
                    MainActivity.noResults();
                } else {

                    //If there was result(s) found, add the best listing to history itemlist
                    historyItemList.add(0, itemList.get(0));

                    //Remove oldest item from history list if limit reached
                    if (historyItemList.size() > historyItemLimit) {
                        historyItemList.remove(historyItemLimit);
                    }
                }
            } else {
                progress.setProgress(searchesDone);
            }
        }
    }

    private void createProduct(Product response) {
        Log.d("Product", "Product Name: " + response.getName());
        Log.d("Product", "Product Image: " + response.getThumbnailImage());
        Log.d("Product", "Product UPC: " + response.getUpc());
        Log.d("Product", "Product Price: " + Double.toString(response.getSalePrice()));
        Log.d("Product", "Product Ship Price: " + Double.toString(response.getStandardShipRate()));

        response.setRetailer();
        response.normalizeTitleLength();
        response.setTime(getCurrentTimeStamp());

        //If the product is available for purchase
        if (response.getSalePrice() > 0.0) {
            itemList.add(response);
            Collections.sort(itemList, new PriceComparator());
            mAdapter.notifyDataSetChanged();

            //Set the header views with details from first returned product
            if (product_name.getText().equals("")) {
                product_name.setText(response.getName());
                Picasso.with(getActivity().getApplicationContext()).load(response.getThumbnailImage()).into(product_image);

            }
        }
    }

    //Return a timestamp for the search time
    private String getCurrentTimeStamp() {
        String timestamp;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("MMMM d',' yyyy '-' h:mma");
        dfm.setTimeZone(cal.getTimeZone());
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }


    //Returns the total amount of retailers to search (users can disable specific retailers in Settings menu)
    private int validRetailersSize() {
        int i = 0;
        for (Retailer ret : retailers) {
            if (ret.getShowRetailer()) {
                i++;
            }
        }
        return i;
    }

    //Retrofit service for API call and response
    private <T> T createRetrofitService(final Class<T> clazz, final String endpoint, String input) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit restAdapter;
        if (input.equals("json")) {
            //Json adapter
            restAdapter = new Retrofit.Builder()
                    .baseUrl(endpoint).client(client)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } else {
            //XML adapter
            restAdapter = new Retrofit.Builder()
                    .baseUrl(endpoint).client(client)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build();
        }

        return restAdapter.create(clazz);
    }

    //Orders list by price - lowest first
    private class PriceComparator implements Comparator<Product> {
        @Override
        public int compare(Product o1, Product o2) {
            return Double.compare(o1.getSalePrice(), o2.getSalePrice());
        }
    }



    //Interface for making a Walmart api call
    interface WalmartService {
        @GET("items")
        Observable<ProductList> getProduct(
                @Query("apiKey") String apiKey,
                @Query("upc") String upc);
    }

    //Interface for making a BestBuy api call
    interface BestBuyService {
        @GET("products(upc={upc_code})")
        Observable<ProductList> getProduct(
                @Path("upc_code") String upcs,
                @Query("apiKey") String apiKeys,
                @Query("format") String json);
    }

    //Interface for making an Ebay api call
    interface EbayService {
        @GET("v1")
        Observable<EbayProduct> getProduct(
                @Query("OPERATION-NAME") String op,
                @Query("SECURITY-APPNAME") String api_key,
                @Query("RESPONSE-DATA-FORMAT") String responseformat,
                @Query("productId.@type") String producttype,
                @Query("productId") String upc);
    }

    //Interface for making an Amazon api call
    interface AmazonService {
        @GET("xml")
        Observable<AmazonProductList> getProduct(
                @QueryMap SortedMap<String, String> s,
                @Query("Signature") String signature);
    }


    //Interface for making a Macy's api call
    interface MacysService {
        @GET("catalog/product/upc/{upc_code}(productdetails(price))")
        Observable<ProductList> getProduct(
                @Header("x-macys-webservice-client-id") String api_key,
                @Path("upc_code") String upcs);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).onSavedUpdated(savedItemList);
        ((MainActivity) getActivity()).onHistoryUpdated(historyItemList);
    }
}
