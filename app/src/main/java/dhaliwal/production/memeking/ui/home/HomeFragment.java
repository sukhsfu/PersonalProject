package dhaliwal.production.memeking.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dhaliwal.production.memeking.R;

public class HomeFragment extends Fragment{

    private HomeViewModel homeViewModel;
    //Arraylist of StorageReference to pass to jadapter.
    // The number of native ads to load and display.
    public static final int spaceBetweenAds = 6;
    private AdLoader adLoader;
    private List<UnifiedNativeAd> mNativeAds = new ArrayList<>();

    private ArrayList<Object> downloadImage;
    private jadapter Jadapter;
    private RecyclerView list;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;

    public HomeFragment(){

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //viewmodel should be used later to load the data.
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        //root view should be used instead of getActivity() to find or set items.
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        context=getContext();

        //Firebase setup getInstance().
        final FirebaseStorage storage=FirebaseStorage.getInstance();
        //set up recylcerview;
        list=root.findViewById(R.id.homerecylerview);
        list.setHasFixedSize(true);
        list.setItemViewCacheSize(20);
        list.setDrawingCacheEnabled(true);
        list.setNestedScrollingEnabled(false);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference memes=database.getReference("memes");
        memes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                downloadImage=new ArrayList<>();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    String memereferid=ds.getValue(String.class);
                    StorageReference memesReference2=storage.getReference().child("Memes").child(memereferid);
                    downloadImage.add(memesReference2);
                }
                Collections.reverse(downloadImage);
                loadNativeAds();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return root;

    }
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){

        swipeRefreshLayout=view.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //
                final FirebaseStorage storage=FirebaseStorage.getInstance();
                FirebaseDatabase database=FirebaseDatabase.getInstance();
                DatabaseReference memes=database.getReference("memes");
                memes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            downloadImage.clear();
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            String memereferid=ds.getValue(String.class);
                            StorageReference memesReference2=storage.getReference().child("Memes").child(memereferid);
                            downloadImage.add(memesReference2);
                        }
                        Collections.reverse(downloadImage);
                        loadNativeAds2();

                        swipeRefreshLayout.setRefreshing(false);

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.darker_gray,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);




    }

    private void loadNativeAds2() {
              mNativeAds.clear();

            AdLoader.Builder builder = new AdLoader.Builder(context, getString(R.string.ad_unit_id));
            adLoader = builder.forUnifiedNativeAd(
                    new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            // A native ad loaded successfully, check if the ad loader has finished loading
                            // and if so, insert the ads into the list.
                            mNativeAds.add(unifiedNativeAd);
                            if (!adLoader.isLoading()) {
                                insertAdsInMenuItems2();
                            }
                        }
                    }).withAdListener(
                    new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // A native ad failed to load, check if the ad loader has finished loading
                            // and if so, insert the ads into the list.
                            Log.e("MainActivity", "The previous native ad failed to load. Attempting to"
                                    + " load another.");
                            if (!adLoader.isLoading()) {
                                insertAdsInMenuItems2();
                            }
                        }
                    }).build();


            int NUMBER_OF_ADS=downloadImage.size()/spaceBetweenAds;
            adLoader.loadAds(new AdRequest.Builder().build(), NUMBER_OF_ADS);
        }

    private void insertAdsInMenuItems2() {
        if (mNativeAds.size() <= 0) {
            return;
        }

        int offset = spaceBetweenAds+1;
        int index = spaceBetweenAds;
        for (UnifiedNativeAd ad: mNativeAds) {
            downloadImage.add(index, ad);
            index = index + offset;
        }
        Jadapter.clear();
        Jadapter.addAll(downloadImage);

    }





    private void insertAdsInMenuItems() {
        if (mNativeAds.size() <= 0) {
            return;
        }

        int offset = spaceBetweenAds+1;
        int index = spaceBetweenAds;
        for (UnifiedNativeAd ad: mNativeAds) {
            downloadImage.add(index, ad);
            index = index + offset;
        }
        Jadapter =new jadapter(downloadImage,context);
        list.setAdapter(Jadapter);
    }
    private void loadNativeAds() {

        AdLoader.Builder builder = new AdLoader.Builder(context, getString(R.string.ad_unit_id));
        adLoader = builder.forUnifiedNativeAd(
                new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // A native ad loaded successfully, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        mNativeAds.add(unifiedNativeAd);
                        if (!adLoader.isLoading()) {
                            insertAdsInMenuItems();
                        }
                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Log.e("MainActivity", "The previous native ad failed to load. Attempting to"
                                + " load another.");
                        if (!adLoader.isLoading()) {
                            insertAdsInMenuItems();
                        }
                    }
                }).build();


        int NUMBER_OF_ADS=downloadImage.size()/spaceBetweenAds;
        adLoader.loadAds(new AdRequest.Builder().build(), NUMBER_OF_ADS);
    }


}
