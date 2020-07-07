package dhaliwal.production.memeking.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAdsManager;
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

public class HomeFragment extends Fragment implements NativeAdsManager.Listener{

    //Arraylist of StorageReference to pass to jadapter.
    // The number of native ads to load and display.
    private ArrayList<Object> downloadImage;
    private jadapter Jadapter;
    private RecyclerView list;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NativeAdsManager mNativeAdsManager;


    public HomeFragment(){

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //viewmodel should be used later to load the data.
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        //root view should be used instead of getActivity() to find or set items.
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        context=getContext();

        //Firebase setup getInstance().
        final FirebaseStorage storage=FirebaseStorage.getInstance();
        //set up recylcerview;
        list=root.findViewById(R.id.homerecylerview);
        list.setHasFixedSize(true);
        list.setItemViewCacheSize(10);
        list.setDrawingCacheEnabled(true);
        list.setNestedScrollingEnabled(false);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        PreLoadingLinearLayoutManager preLoadingLinearLayoutManager=new PreLoadingLinearLayoutManager(getContext(),15);
        list.setLayoutManager(preLoadingLinearLayoutManager);
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference memes=database.getReference("memes");
            memes.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try{
                    downloadImage = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String memereferid = ds.getValue(String.class);
                        StorageReference memesReference2 = storage.getReference().child("Memes").child(memereferid);
                        downloadImage.add(memesReference2);
                    }
                    Collections.reverse(downloadImage);
                    loadNativeAds();
                }
                    catch (NullPointerException e){
                        System.out.print("NullPointerException Caught");
                    }
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
                            try {
                                downloadImage.clear();
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String memereferid = ds.getValue(String.class);
                                    StorageReference memesReference2 = storage.getReference().child("Memes").child(memereferid);
                                    downloadImage.add(memesReference2);
                                }
                                Collections.reverse(downloadImage);
                                loadNativeAds();

                                swipeRefreshLayout.setRefreshing(false);

                            }
                            catch (NullPointerException e){
                                System.out.print("NullPointerException Caught");
                            }
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










    private void loadNativeAds() {
        AdSettings.addTestDevice("9ffdea42-39d5-4ef0-bfc2-e22f70f632e8");
        mNativeAdsManager= new NativeAdsManager(context,"1952653651534818_2001259383340911",10);
        mNativeAdsManager.loadAds();
        mNativeAdsManager.setListener(this);



    }


    @Override
    public void onAdsLoaded() {
        try {
            ProgressBar progressBar = getActivity().findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            Jadapter = new jadapter(downloadImage, context, mNativeAdsManager);
            list.setAdapter(Jadapter);
        }
        catch (NullPointerException e){
            System.out.print("NullPointerException Caught");
        }
    }





    @Override
    public void onAdError(AdError adError) {

            ProgressBar progressBar = getActivity().findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            Jadapter = new jadapter(downloadImage, context, mNativeAdsManager);
            list.setAdapter(Jadapter);


    }


}
