package dhaliwal.production.memeking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.AudienceNetworkAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


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
        AudienceNetworkAds.initialize(this);
        //may need the explict initalisation.



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
        }, 100);
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
