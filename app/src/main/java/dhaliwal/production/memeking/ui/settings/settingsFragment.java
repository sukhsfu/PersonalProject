package dhaliwal.production.memeking.ui.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dhaliwal.production.memeking.R;
import dhaliwal.production.memeking.UserProfileInfo;

import static android.app.Activity.RESULT_OK;


public class settingsFragment extends Fragment {

    private settingsViewModel settingsViewModel;
    private static final int RESULT_LOAD_IMAGE=1;
    private ImageView profileImageChanger;
    private Uri selectedImage=null;
    private Context context;
    private  String TAG="settings";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(settingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        profileImageChanger=root.findViewById(R.id.profileImageChanger);
        final EditText username=root.findViewById(R.id.settings_username);
        Button savechanges=root.findViewById(R.id.settings_savechanges);
        //gettiing the user from the firebase authentication
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        //get the user from the firebase realtime database and set it in imageview and edittext
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference user_Profile=database.getReference("user_profile").child(user.getUid());
        user_Profile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfileInfo userProfileInfo=dataSnapshot.getValue(UserProfileInfo.class);
                String photoUri=userProfileInfo.getProfile_photo();
                String name=userProfileInfo.getUsername();

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





        profileImageChanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pick photo from the gallery to change profile pick.
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,RESULT_LOAD_IMAGE);
            }
        });
        //OnclickListener for the Done Button.
        savechanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //1.send profile photo to firebase.//send if selectedImage!=null
                //2.send username to firebase.//send if name !=changed name.
                //3.take to memes activity.




            }
        });
        context=getContext();
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
