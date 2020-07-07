package dhaliwal.production.memeking.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import dhaliwal.production.memeking.R;
import dhaliwal.production.memeking.UserProfileInfo;
import dhaliwal.production.memeking.ui.gallery.GalleryFragment;
import dhaliwal.production.memeking.ui.home.HomeFragment;

import static android.app.Activity.RESULT_OK;


public class settingsFragment extends Fragment {

    private settingsViewModel settingsViewModel;
    private static final int RESULT_LOAD_IMAGE=1;
    private ImageView profileImageChanger;
    private Uri selectedImage=null;
    private String changedname=null;
    private Context context;
    private  String TAG="settings";
    private String name;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(settingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final View touch=root;

        profileImageChanger=root.findViewById(R.id.profileImageChanger);
        final EditText username=root.findViewById(R.id.settings_username);
        final Button savechanges=root.findViewById(R.id.settings_savechanges);
        try {
            //gettiing the user from the firebase authentication
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //get the user from the firebase realtime database and set it in imageview and edittext
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference user_Profile = database.getReference("user_profile").child(user.getUid());
            user_Profile.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserProfileInfo userProfileInfo = dataSnapshot.getValue(UserProfileInfo.class);
                    String photoUri = userProfileInfo.getProfile_photo();
                    name = userProfileInfo.getUsername();

                    Glide.with(getContext())
                            .load(photoUri)
                            .placeholder(R.mipmap.placeholder)
                            .centerCrop()
                            .into(profileImageChanger);

                    username.setText(name);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //set text watcher on edittext
            username.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    changedname = String.valueOf(s);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    changedname = String.valueOf(s);

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            //click on the image to change profile_picture.
            profileImageChanger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Pick photo from the gallery to change profile pick.
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                }
            });
            //set click listener on the textview.
            //...

            //OnclickListener for the Done Button.
            savechanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savechanges.setEnabled(false);
                    //1.send profile photo to firebase.//send if selectedImage!=null
                    //2.send username to firebase.//send if name !=changed name.
                    //3.take to memes activity.

                    //if the user set image
                    if (selectedImage != null) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        String path = "UserProfilePhoto/" + user.getUid() + "pic";
                        final StorageReference storageReference = storage.getReference(path);
                        UploadTask uploadTask = storageReference.putFile(selectedImage);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                user_Profile.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final UserProfileInfo userProfileInfo = dataSnapshot.getValue(UserProfileInfo.class);
                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                userProfileInfo.setProfile_photo(uri.toString());
                                                user_Profile.setValue(userProfileInfo);
                                                //add fragmentTransaction here.
                                                Navigation.findNavController(touch).navigate(R.id.nav_home);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }
                    //if the username is changed.
                    if (!(changedname.equals(name))) {
                        user_Profile.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                UserProfileInfo userProfileInfo = dataSnapshot.getValue(UserProfileInfo.class);
                                userProfileInfo.setUsername(changedname);
                                user_Profile.setValue(userProfileInfo);
                                if (selectedImage == null) {
                                    Navigation.findNavController(touch).navigate(R.id.nav_home);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                    if (selectedImage == null && changedname.equals(name)) {
                        Navigation.findNavController(touch).navigate(R.id.nav_home);
                    }


                }
            });
            context = getContext();
        }
        catch (NullPointerException e){
            System.out.print("NullPointerException Caught");
        }

        return root;

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_LOAD_IMAGE &&resultCode==RESULT_OK&&data!=null){
             selectedImage=data.getData();
            Glide.with(context)
                    .load(selectedImage)
                    .placeholder(R.mipmap.placeholder)
                    .centerCrop()
                    .into(profileImageChanger);
        }

    }
}
