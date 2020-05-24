package dhaliwal.production.memeking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        //setting username and profile from the sign-in provider.
        try {
            retrieveuserinformation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //get user-name and profile photo from the sign-providers and set it on side bar
    private void retrieveuserinformation() throws IOException {
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        //getting the imageview and textview id's
        NavigationView navigationView=findViewById(R.id.nav_view);
        View mview=navigationView.getHeaderView(0);
        ImageView profileimage=mview.findViewById(R.id.profileImage);
        TextView username=mview.findViewById(R.id.textview_MeMeKing);
        if(user!=null){
            String name = null;
            Uri photoUri = null;
           for(UserInfo profile:user.getProviderData()){
               if(name==null)
                name=profile.getDisplayName();
               //if(photoUri==null) {
                   if (profile.getPhotoUrl() != null)
                       photoUri = profile.getPhotoUrl();

               //}

           }
          // Intent intent=getIntent();
          // intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
          // getContentResolver().takePersistableUriPermission(photoUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);



            Glide.with(this)
                       .load(photoUri)
                       .placeholder(R.mipmap.placeholder)
                       .centerCrop()
                       .into(profileimage);
               username.setText(name);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}
