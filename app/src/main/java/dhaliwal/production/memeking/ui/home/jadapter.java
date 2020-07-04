package dhaliwal.production.memeking.ui.home;


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
import com.google.android.gms.ads.NativeExpressAdView;
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

public class jadapter extends RecyclerView.Adapter {

    //arraylist of StorageReferences to display images.
    private ArrayList<Object> Downloadimages;
    //context to use in Glide.
    private Context context;

    //
    private static final int DATA_VIEW_TYPE = 0;
    private static final int NATIVE_EXPRESS_AD_VIEW_TYPE = 1;
    private int spaceBetweenAds;


    /*
    *@param Downloadimages 1000 StorageReferences's to display in recylerView.
     */
    jadapter(ArrayList<Object> Downloadimages,Context context,int spaceBetweenAds){
        this.Downloadimages=Downloadimages;
        this.context=context;
        this.spaceBetweenAds=spaceBetweenAds;

    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case DATA_VIEW_TYPE:
                LayoutInflater flate = LayoutInflater.from(parent.getContext());
                View view = flate.inflate(R.layout.mainactivitytab, parent, false);
                return  new vholder(view);
            case NATIVE_EXPRESS_AD_VIEW_TYPE:
            default:
                LayoutInflater flate2 = LayoutInflater.from(parent.getContext());
                View view2 = flate2.inflate(R.layout.adlayout, parent, false);
                return new NativeExpressAdViewHolder(view2);


        }

    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder2, int position) {
        int viewType=getItemViewType(position);
        switch (viewType) {
            case DATA_VIEW_TYPE:
                //use Glide to set images in recyclerview.
                StorageReference thisimage = (StorageReference)Downloadimages.get(position);
                final String postReferenceName = thisimage.getName();
                final vholder holder = (vholder) holder2;

                updatedata(holder, postReferenceName);

                Glide.with(context)
                        .load(thisimage)
                        .placeholder(R.mipmap.placeholder)
                        .into(holder.memes);

                //fireimage on click listener
                holder.fire.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStarClicked(holder, postReferenceName);

                    }

                });
                //on follow button clicked
                holder.tabfollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onfollowClicked(holder, postReferenceName);

                    }
                });
                break;
            case NATIVE_EXPRESS_AD_VIEW_TYPE:
            default:
                NativeExpressAdViewHolder nativeExpressHolder = (NativeExpressAdViewHolder) holder2;
                NativeExpressAdView adView = (NativeExpressAdView) Downloadimages.get(position);
                ViewGroup adCardView = (ViewGroup) nativeExpressHolder.itemView;

                if (adCardView.getChildCount() > 0) {
                    adCardView.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }
                adCardView.addView(adView);
        }




    }
    private void updatedata(final vholder holder, String postReferenceName){
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
                    Glide.with(context)
                            .load(R.drawable.firecolor)
                            .into(holder.fire);
                } else {
                    Glide.with(context)
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
                        Glide.with(context)
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

    private void onfollowClicked(final vholder holder, final String postReferenceName) {
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
                   updatedata(holder,postReferenceName);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void  onStarClicked(final vholder holder, final String postReferenceName){
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
                                updatedata(holder,postReferenceName);
                            }
                        });
                    }
                    else{
                        updatedata(holder,postReferenceName);
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
                                updatedata(holder,postReferenceName);
                            }
                        });
                    }
                    else{
                        updatedata(holder,postReferenceName);
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

    // View Holder for Admob Native Express Ad Unit
    public class NativeExpressAdViewHolder extends RecyclerView.ViewHolder {
        NativeExpressAdViewHolder(View view) {
            super(view);
        }
    }

    @Override
    public int getItemCount() {
        return Downloadimages.size();
    }
    @Override
    public int getItemViewType(int position) {
        return (position % (spaceBetweenAds + 1) == spaceBetweenAds) ? NATIVE_EXPRESS_AD_VIEW_TYPE: DATA_VIEW_TYPE;
    }

    public class vholder extends RecyclerView.ViewHolder {
        ImageView memes;
        ImageView profilePicture;
        ImageView fire;
        TextView litcount;
        TextView username;
        TextView totalpoints;
        TextView tabfollow;



        vholder(View view) {
            super(view);
            memes = view.findViewById(R.id.imageView2);
            fire=view.findViewById(R.id.tabfireimage);
            litcount=view.findViewById(R.id.tablitnumber);
            username=view.findViewById(R.id.tabusername);
            profilePicture=view.findViewById(R.id.tabprofilepicture);
            totalpoints=view.findViewById(R.id.tabtotalpoints);
            tabfollow=view.findViewById(R.id.tabfollow);


        }


    }
    public void clear(){
        Downloadimages.clear();
        notifyDataSetChanged();
    }
    public void addAll(ArrayList<StorageReference> Downloadimages){
        this.Downloadimages.addAll(Downloadimages);
        notifyDataSetChanged();
    }
}
