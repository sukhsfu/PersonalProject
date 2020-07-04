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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;

import dhaliwal.production.memeking.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private int spaceBetweenAds = 12;
    //Arraylist of StorageReference to pass to jadapter.
    private ArrayList<Object> downloadImage;
    private jadapter Jadapter;
    private RecyclerView list;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;

    public HomeFragment() {

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //viewmodel should be used later to load the data.
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        //root view should be used instead of getActivity() to find or set items.
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();

        //Firebase setup getInstance().
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        //set up recylcerview;
        list = root.findViewById(R.id.homerecylerview);
        list.setHasFixedSize(true);
        list.setItemViewCacheSize(20);
        list.setDrawingCacheEnabled(true);
        list.setNestedScrollingEnabled(false);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference memes = database.getReference("memes");
        memes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                downloadImage = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String memereferid = ds.getValue(String.class);
                    StorageReference memesReference2 = storage.getReference().child("Memes").child(memereferid);
                    downloadImage.add(memesReference2);
                }
                Collections.reverse(downloadImage);
                setRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return root;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        swipeRefreshLayout = view.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //
                final FirebaseStorage storage = FirebaseStorage.getInstance();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference memes = database.getReference("memes");
                memes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<StorageReference> downloadImage2 = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String memereferid = ds.getValue(String.class);
                            StorageReference memesReference2 = storage.getReference().child("Memes").child(memereferid);
                            downloadImage2.add(memesReference2);
                        }
                        Collections.reverse(downloadImage2);

                        Jadapter.clear();
                        Jadapter.addAll(downloadImage2);
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


    private void setRecyclerView() {
        addNativeAdvanceAds();
        Jadapter = new jadapter(downloadImage, context, spaceBetweenAds);
        list.setAdapter(Jadapter);
    }

    private void addNativeAdvanceAds() {
        for (int i = spaceBetweenAds; i <= downloadImage.size(); i += (spaceBetweenAds + 1)) {
            //change to advance
            NativeExpressAdView adView = new NativeExpressAdView(context);
            //replace with your admob id;
            adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
            downloadImage.add(i, adView);
        }
        list.post(new Runnable() {
            @Override
            public void run() {
                float scale = HomeFragment.this.getResources().getDisplayMetrics().density;
                int adWidth = (int) (list.getWidth() - (2 * HomeFragment.this.getResources().getDimension(R.dimen.activity_horizontal_margin)));
                // you should check admob's site for possible ads size
                AdSize adSize = new AdSize((int) (adWidth / scale), 150);
                for (int i = spaceBetweenAds; i <= downloadImage.size(); i += (spaceBetweenAds + 1)) {
                    NativeExpressAdView adViewToSize = (NativeExpressAdView) downloadImage.get(i);
                    adViewToSize.setAdSize(adSize);
                }
                loadNativeExpressAd(spaceBetweenAds);
            }
        });


    }
    private void loadNativeExpressAd(final int index) {
        if (index >= downloadImage.size()) {
            return;
        }
        Object item = downloadImage.get(index);
        if (!(item instanceof NativeExpressAdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a Native"
                    + " Express ad.");
        }
        final NativeExpressAdView adView = (NativeExpressAdView) item;
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous Native Express ad loaded successfully, call this method again to
                // load the next ad in the items list.
                loadNativeExpressAd(index + spaceBetweenAds + 1);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous Native Express ad failed to load. Call this method again to load
                // the next ad in the items list.
                Log.e("AdmobMainActivity", "The previous Native Express ad failed to load. Attempting to"
                        + " load the next Native Express ad in the items list.");
                loadNativeExpressAd(index + spaceBetweenAds + 1);
            }
        });
        //remove test device
        adView.loadAd(new AdRequest.Builder().build());
    }

}
