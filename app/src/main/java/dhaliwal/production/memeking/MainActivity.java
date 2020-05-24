package dhaliwal.production.memeking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private boolean signed_in=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //all the layout stuff.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        //Authentication initializer.
        mAuth=FirebaseAuth.getInstance();
        /* Handler to start the next activity.
        * Two cases-if the user is already signed in navigate to display activity.
        * otherwise take to the login activity. */
        Handler myhandler = new Handler();
        myhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if(signed_in) {
                     intent = display.makeIntent(MainActivity.this);

                }
                else{
                     intent = LoginActivity.makeIntent(MainActivity.this);
                }
                startActivity(intent);

            }
        }, 600);
    }
    //Overriding the Onstart in case the user is already signed to the app;
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
            signed_in=true;
    }








}
