package dhaliwal.production.memeking.ui.gallery;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import dhaliwal.production.memeking.Post;
import dhaliwal.production.memeking.R;
import dhaliwal.production.memeking.Utils.GridImageAdapter;

import static android.app.Activity.RESULT_OK;

public class GalleryFragment extends Fragment  {
    private static final int RESULT_LOAD_IMAGE=1;

    private GalleryViewModel galleryViewModel;
    private GridView gridView;
    private ArrayList<Uri> imageuritoupload;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        galleryViewModel= new ViewModelProvider(this).get(GalleryViewModel.class);

        ContextThemeWrapper contextThemeWrapper=new ContextThemeWrapper(getContext(),R.style.themeupload);
        LayoutInflater layoutInflater=inflater.cloneInContext(contextThemeWrapper);
        //view root inflated
        View root = layoutInflater.inflate(R.layout.fragment_gallery, container, false);
        final View touch=root;

        Toolbar toolbar=(Toolbar) root.findViewById(R.id.bottom_app_bar);
        toolbar.inflateMenu(R.menu.bottomppbar_menu);
        gridView=(root).findViewById(R.id.grid);
        Menu menu=toolbar.getMenu();
        MenuItem item=menu.getItem(0);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Navigation.findNavController(touch).navigate(R.id.nav_home);
                return false;
            }
        });

        imageuritoupload=new ArrayList<>();
        final FirebaseStorage storage= FirebaseStorage.getInstance();





        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            int request_code = 0;
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},request_code);
        }
        else {
        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,RESULT_LOAD_IMAGE);
        }

        //button clicked to upload image.
        final FloatingActionButton buttonupload=root.findViewById(R.id.fab_gallery);
        buttonupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageuritoupload.size()!=0){
                    //getting user and storage reference
                    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                    final String uid=user.getUid();
                    FirebaseDatabase database=FirebaseDatabase.getInstance();
                    final int[] count = {0,0};
                    buttonupload.setEnabled(false);
                    for(Uri image:imageuritoupload){
                        String path="Memes/"+UUID.randomUUID();
                        final String path2=path.substring(6);
                        String randomString;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            randomString= Instant.now()+path.substring(6,9);
                            randomString=randomString.replace(".","");
                        }
                        else{
                            TimeZone timeZone= TimeZone.getTimeZone("UTC");
                            Calendar calendar=Calendar.getInstance(timeZone);
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);
                            simpleDateFormat.setTimeZone(timeZone);
                            randomString=simpleDateFormat.format(calendar.getTime())+path.substring(6,9);
                            randomString=randomString.replace(".","");
                        }

                        StorageReference storageReference=storage.getReference(path);
                        final DatabaseReference memePhotoReferences=database.getReference("memepicture").child(path2);
                       final  DatabaseReference memes=database.getReference("memes").child(randomString);
                        UploadTask uploadTask=storageReference.putFile(image);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(),"Failed",Toast.LENGTH_SHORT).show();
                                count[1]++;

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Post post=new Post(uid);
                                memePhotoReferences.setValue(post);
                                memes.setValue(path2);

                                count[0]++;
                                if((count[0]+count[1])==imageuritoupload.size())
                                incrementCount(count[0]);
                            }
                        });

                    }

                }

            }
        });

        return root;
    }

    private void incrementCount(final int count) {
        if(count!=0){
            FirebaseDatabase database=FirebaseDatabase.getInstance();
            DatabaseReference countReference=database.getReference("counting").child("count");
            countReference.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer getcount=mutableData.getValue(Integer.class);
                    if(getcount==null){
                        return Transaction.success(mutableData);
                    }
                    getcount=count+getcount;
                    mutableData.setValue(getcount);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    Toast.makeText(getContext(),"Successful",Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestcode,int resultcode,Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        if (requestcode == RESULT_LOAD_IMAGE && resultcode == RESULT_OK && data != null) {
            ArrayList<String> imageURIs = new ArrayList<>();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    imageuritoupload.add(imageUri);
                    imageURIs.add(imageUri.toString());
                    }
                }

             else {
                Uri selectedImage = data.getData();
                imageuritoupload.add(selectedImage);
            imageURIs.add(selectedImage.toString());
            }

            int gridWidth = getResources().getDisplayMetrics().widthPixels;
            int imageWidth = gridWidth/2;
            gridView.setColumnWidth(imageWidth);
            GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.uploadtab,imageURIs);
            gridView.setAdapter(adapter);


        }
    }




}



