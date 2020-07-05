package dhaliwal.production.memeking.ui.home;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

import dhaliwal.production.memeking.Post;
import dhaliwal.production.memeking.R;
import dhaliwal.production.memeking.UserProfileInfo;

public class jadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

    //arraylist of StorageReferences to display images.
    private ArrayList<Object> Downloadimages=new ArrayList<>();
    //context to use in Glide.
    private Context context;



    /*
    *@param Downloadimages 1000 StorageReferences's to display in recylerView.
     */
    jadapter(ArrayList<Object> Downloadimages, Context context){

        this.Downloadimages.addAll(Downloadimages);
        this.context=context;

    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch(viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                View Adview = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ad_unified, viewGroup, false);
                return new UnifiedNativeAdViewHolder(Adview);
            case MENU_ITEM_VIEW_TYPE:
            default:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mainactivitytab, viewGroup, false);
                return new vholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holdermain, int position) {
        int viewType=getItemViewType(position);
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                UnifiedNativeAd nativeAd = (UnifiedNativeAd) Downloadimages.get(position);
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holdermain).getAdView());
                break;
            case MENU_ITEM_VIEW_TYPE:
            default:
                //use Glide to set images in recyclerview.
                StorageReference thisimage = (StorageReference) Downloadimages.get(position);
                final String postReferenceName = thisimage.getName();
                final vholder holder = (vholder) holdermain;

                updatedata(holder, postReferenceName,context);

                Glide.with(context)
                        .load(thisimage)
                        .placeholder(R.mipmap.placeholder)
                        .into(holder.memes);

                //fireimage on click listener
                holder.fire.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStarClicked(holder, postReferenceName,v.getContext());

                    }

                });
                //on follow button clicked
                holder.tabfollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onfollowClicked(holder, postReferenceName,v.getContext());

                    }
                });


        }
    }
    private void updatedata(final vholder holder, String postReferenceName,final Context context1){
        final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference memePhotoReferences=database.getReference("memepicture").child(postReferenceName);
        memePhotoReferences.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Post post = dataSnapshot.getValue(Post.class);



                    //uid of the user who uploaded post.
                    final String uid = post.getUid();
                int litnumber = post.getLit();
                holder.litcount.setText(String.valueOf(litnumber));
                if (post.stars.containsKey(user.getUid())) {
                    Glide.with(context1)
                            .load(R.drawable.firecolor)
                            .into(holder.fire);
                } else {
                    Glide.with(context1)
                            .load(R.drawable.firebw)
                            .into(holder.fire);

                }
                //user of app
                DatabaseReference userReferenceApp = database.getReference("user_profile").child(user.getUid()).child("followingId");

                userReferenceApp.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String,Boolean> map = (Map)dataSnapshot.getValue();
                        if(map!=null) {
                            if (map.containsKey(uid)) {
                                holder.tabfollow.setText("following");
                            } else {
                                holder.tabfollow.setText("+follow");
                            }
                        }
                        else{
                            holder.tabfollow.setText("+follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //user  of the post
                DatabaseReference userReferencePost = database.getReference("user_profile").child(uid);
                userReferencePost.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserProfileInfo userProfileInfo = dataSnapshot.getValue(UserProfileInfo.class);
                        String username = userProfileInfo.getUsername();
                        String PhotoUri = userProfileInfo.getProfile_photo();
                        int totalpoints = userProfileInfo.getPoints();
                        holder.username.setText(username);
                        holder.totalpoints.setText(String.valueOf(totalpoints));
                        Glide.with(context1)
                                .load(PhotoUri)
                                .into(holder.profilePicture);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void onfollowClicked(final vholder holder, final String postReferenceName, final Context context1) {
        final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference memePhotoReferences=database.getReference("memepicture").child(postReferenceName);
        memePhotoReferences.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post=dataSnapshot.getValue(Post.class);
                //uid of the user who uploaded post
               final String uid= post.getUid();
               if(!user.getUid().equals(uid)) {
                   final FirebaseDatabase database = FirebaseDatabase.getInstance();
                   final DatabaseReference userReferenceApp = database.getReference("user_profile").child(user.getUid());
                   userReferenceApp.addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           UserProfileInfo userProfileInfo = dataSnapshot.getValue(UserProfileInfo.class);
                           if (userProfileInfo.followingId.containsKey(uid)) {
                               userProfileInfo.followingId.remove(uid);
                               int number = userProfileInfo.getFollowing() - 1;
                               userProfileInfo.setFollowing(number);
                           } else {
                               userProfileInfo.followingId.put(uid, true);
                               int number = userProfileInfo.getFollowing() + 1;
                               userProfileInfo.setFollowing(number);
                           }
                           userReferenceApp.setValue(userProfileInfo);
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });
                   final DatabaseReference userReferencePost= database.getReference("user_profile").child(uid);
                   userReferencePost.runTransaction(new Transaction.Handler() {
                       @NonNull
                       @Override
                       public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                           UserProfileInfo userProfileInfo=mutableData.getValue(UserProfileInfo.class);
                           if(userProfileInfo==null){
                               return Transaction.success(mutableData);
                           }
                           if(userProfileInfo.AudienceId.containsKey(user.getUid())){
                               userProfileInfo.AudienceId.remove(user.getUid());
                               int number=userProfileInfo.getAudience()-1;
                               userProfileInfo.setAudience(number);
                           }
                           else{
                               userProfileInfo.AudienceId.put(user.getUid(),true);
                               int number=userProfileInfo.getAudience()+1;
                               userProfileInfo.setAudience(number);
                           }
                           mutableData.setValue(userProfileInfo);
                           return Transaction.success(mutableData);

                       }

                       @Override
                       public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                       }
                   });
                   updatedata(holder,postReferenceName,context1);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void  onStarClicked(final vholder holder, final String postReferenceName,final Context context1){
        final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference memePhotoReferences=database.getReference("memepicture").child(postReferenceName);
        memePhotoReferences.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post post=mutableData.getValue(Post.class);


                if(post==null){
                    return Transaction.success(mutableData);
                }
                String postAuther=post.getUid();
                final FirebaseDatabase database=FirebaseDatabase.getInstance();
                final DatabaseReference postAutherPoints=database.getReference("user_profile").child(postAuther).child("points");

                if(post.stars.containsKey(user.getUid())){
                    //post unlit.
                    int lit=post.getLit()-1;
                    post.setLit(lit);
                    post.stars.remove(user.getUid());
                    if(!(post.getUid().equals(user.getUid()))) {
                        postAutherPoints.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Integer points = mutableData.getValue(Integer.class);
                                if (points == null) {
                                    return Transaction.success(mutableData);
                                }
                                int newpoints = points - 1;
                                mutableData.setValue(newpoints);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                updatedata(holder,postReferenceName,context1);
                            }
                        });
                    }
                    else{
                        updatedata(holder,postReferenceName,context1);
                    }

                }
                else{
                    int lit=post.getLit()+1;
                    post.setLit(lit);
                    post.stars.put(user.getUid(),true);
                    if(!(post.getUid().equals(user.getUid()))) {
                        postAutherPoints.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Integer points = mutableData.getValue(Integer.class);
                                if (points == null) {
                                    return Transaction.success(mutableData);
                                }
                                int newpoints = points + 1;
                                mutableData.setValue(newpoints);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                updatedata(holder,postReferenceName,context1);
                            }
                        });
                    }
                    else{
                        updatedata(holder,postReferenceName,context1);
                    }
                }
                mutableData.setValue(post);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });


    }



    @Override
    public int getItemCount() {
        return Downloadimages.size();
    }

    public class vholder extends RecyclerView.ViewHolder {
        ImageView memes;
        ImageView profilePicture;
        ImageView fire;
        TextView litcount;
        TextView username;
        TextView totalpoints;
        TextView tabfollow;



        vholder(View itemView) {
            super(itemView);
            memes = itemView.findViewById(R.id.imageView2);
            fire=itemView.findViewById(R.id.tabfireimage);
            litcount=itemView.findViewById(R.id.tablitnumber);
            username=itemView.findViewById(R.id.tabusername);
            profilePicture=itemView.findViewById(R.id.tabprofilepicture);
            totalpoints=itemView.findViewById(R.id.tabtotalpoints);
            tabfollow=itemView.findViewById(R.id.tabfollow);


        }

    }
    public void clear(){
        Downloadimages.clear();
        notifyDataSetChanged();
    }
    public void addAll(ArrayList<Object> Downloadimages){
        this.Downloadimages.addAll(Downloadimages);
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        Object recyclerViewItem = Downloadimages.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd) {
            return UNIFIED_NATIVE_AD_VIEW_TYPE;
        }
        return MENU_ITEM_VIEW_TYPE;
    }
    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }
}
