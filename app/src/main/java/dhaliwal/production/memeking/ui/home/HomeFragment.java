package dhaliwal.production.memeking.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import dhaliwal.production.memeking.R;

public class HomeFragment extends Fragment implements jadapter.OnNoteListener, AdapterView.OnItemSelectedListener {

    private HomeViewModel homeViewModel;
    //Arraylist of StorageReference to pass to jadapter.
    private ArrayList<StorageReference> downloadImage;
    private jadapter Jadapter;
    private RecyclerView list;
    private Context context;

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
        FirebaseStorage storage=FirebaseStorage.getInstance();
        //reference to the Memes folder in the bucket.
        StorageReference memesReference=storage.getReference().child("Memes");
        //set up recylcerview;
        list=root.findViewById(R.id.homerecylerview);
        list.setHasFixedSize(true);
        list.setItemViewCacheSize(20);
        list.setDrawingCacheEnabled(true);
        list.setNestedScrollingEnabled(false);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        list.setLayoutManager(new LinearLayoutManager(getContext()));



        memesReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        downloadImage=new ArrayList<>();
                        downloadImage.addAll(listResult.getItems());
                        //function to attach adapter to Recyclerview
                        setRecyclerView();

                    }
                });
        //setting username and profile from the sign-in provider.

        return root;

    }



    private void setRecyclerView(){
        Jadapter =new jadapter(downloadImage, this,context);
        list.setAdapter(Jadapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onNoteClick(int position) {

    }
}
