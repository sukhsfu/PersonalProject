package dhaliwal.production.memeking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private List<AuthUI.IdpConfig> providers;
    private static final int RC_SIGN_IN = 123;
    private final String tag="Login";


    public static Intent makeIntent(Context context){
        return new Intent(context,LoginActivity.class);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //setup authentication from the firebase.
        setupauthentication();
    }
    private void setupauthentication() {
        providers= Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build()
        );
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.fui_ic_facebook_white_22dp)//make memehub logo and update this.
                .build(),RC_SIGN_IN);



    }
    //Overriding onActivity Result to setup Authentication
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==RC_SIGN_IN){
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK){
                //successfully signed-in
                final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

                //firebase realtime database reference
                FirebaseDatabase database=FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference=database.getReference("user_profile");

                //set the user_name and profilephoto only if he signed up first time.
                //make sure it is single value event.
                //datareference to whole "user_profile"
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //make sure user does not exist.
                        String name = null;
                        Uri photoUri = null;
                        if (!dataSnapshot.hasChild(user.getUid())) {
                            for (UserInfo profile : user.getProviderData()) {
                                name = profile.getDisplayName();
                                photoUri = profile.getPhotoUrl();
                            }
                            //make UserProfile Info Object to insert in realtime database.
                            UserProfileInfo userProfileInfo=new UserProfileInfo(photoUri.toString(),name);
                            databaseReference.child(user.getUid()).setValue(userProfileInfo);
                            Log.d(tag,"User has been successfully added to realtime database.");

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



                Intent intent = display.makeIntent(LoginActivity.this);
                startActivity(intent);
            }
            else{
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...

            }
        }
    }


}
