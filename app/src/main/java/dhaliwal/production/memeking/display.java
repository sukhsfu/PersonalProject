package dhaliwal.production.memeking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class display extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;




    public static Intent makeIntent(Context context){
        return new Intent(context, display.class);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                navController.getGraph())
                .setDrawerLayout(drawer)
                .build();
        //R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow





        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);//set up drawer.
      /*  navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.nav_gallery||destination.getId() == R.id.nav_slideshow) {
                    FloatingActionButton fab=findViewById(R.id.fab);
                    fab.setVisibility(View.GONE);
                }
                else{
                    FloatingActionButton fab=findViewById(R.id.fab);
                    fab.setVisibility(View.VISIBLE);
                }
            }
        });*/
        //setting username and profile from the realtime data provider..

            retrieveuserinformation();

    }
    //get user-name and profile photo from the realtime database  and set it on side bar
    private void retrieveuserinformation()  {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //getting the imageview and textview id's
            NavigationView navigationView = findViewById(R.id.nav_view);
            View mview = navigationView.getHeaderView(0);
            final ImageView profileimage = mview.findViewById(R.id.profileImage);
            final TextView username = mview.findViewById(R.id.textview_MeMeKing);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            //get the datareference of profile_photo and username
            final DatabaseReference profile_photoReference = database.getReference("user_profile").child(user.getUid()).child("profile_photo");
            DatabaseReference usernameReference = database.getReference("user_profile").child(user.getUid()).child("username");
            //addValueListener to profile_photoreference
            profile_photoReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String photoUri = dataSnapshot.getValue(String.class);
                    Glide.with(display.this)
                            .load(photoUri)
                            .placeholder(R.mipmap.placeholder)
                            .centerCrop()
                            .into(profileimage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            usernameReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue(String.class);
                    username.setText(name);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        catch (NullPointerException e){
            System.out.print("NullPointerException Caught");
        }




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.action_settings) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check this cool app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}
