package dhaliwal.production.memeking.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dhaliwal.production.memeking.R;
import dhaliwal.production.memeking.UserProfileInfo;


public class Profile extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    //user profile photo
    private ImageView user_profilephoto;
    private TextView user_name;
    private TextView user_follwing;
    private TextView user_Audience;
    private TextView user_points;


    public Profile() {
        // Required empty public constructor
    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_profile, container, false);
        //initate the variables
        user_profilephoto=root.findViewById(R.id.profilefragmentImage);
        user_name=root.findViewById(R.id.profilefragmentusername);
        user_follwing=root.findViewById(R.id.profilefragmentfollowingnum);
        user_Audience=root.findViewById(R.id.profilefragmentaudiencenumber);
        user_points=root.findViewById(R.id.profilefragmentpoints);
        //get the user
        final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        //get the firebase realtime database and fill the profile
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference user_Profile=database.getReference("user_profile").child(user.getUid());
        user_Profile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get the UserProfile object
                UserProfileInfo userProfileInfo=dataSnapshot.getValue(UserProfileInfo.class);
                String photoUri=userProfileInfo.getProfile_photo();
                Glide.with(getContext())
                        .load(photoUri)
                        .placeholder(R.mipmap.placeholder)
                        .centerCrop()
                        .into(user_profilephoto);
                String following=String.valueOf(userProfileInfo.getFollowing());
                String audience=String.valueOf(userProfileInfo.getAudience());
                String points=String.valueOf(userProfileInfo.getPoints());
                user_name.setText(userProfileInfo.getUsername());
                user_follwing.setText(following);
                user_Audience.setText(audience);
                user_points.setText(points);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return root;
    }
}
