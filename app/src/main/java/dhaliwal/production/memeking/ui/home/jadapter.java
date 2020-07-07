package dhaliwal.production.memeking.ui.home;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;

import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

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

import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdsManager;
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
import java.util.List;
import java.util.Map;

import dhaliwal.production.memeking.Post;
import dhaliwal.production.memeking.R;
import dhaliwal.production.memeking.UserProfileInfo;

public class jadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private static final int NATIVE_AD_VIEW_TYPE = 1;
    private  NativeAdsManager mNativeAdsManager;
    private List<NativeAd> mAdItems;
    private static final int AD_DISPLAY_FREQUENCY = 3;

    //arraylist of StorageReferences to display images.
    private ArrayList<Object> Downloadimages=new ArrayList<>();
    //context to use in Glide.
    private Context context;



    /*
    *@param Downloadimages 1000 StorageReferences's to display in recylerView.
     */
    jadapter(ArrayList<Object> Downloadimages, Context context, NativeAdsManager mNativeAdsManager){

        this.Downloadimages.addAll(Downloadimages);
        this.context=context;
        this.mNativeAdsManager=mNativeAdsManager;
        mAdItems=new ArrayList<>();

    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch(viewType) {
            case NATIVE_AD_VIEW_TYPE:
                NativeAdLayout Adview = (NativeAdLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ad_unified, viewGroup, false);
                return new AdHolder(Adview);
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
            case NATIVE_AD_VIEW_TYPE:
                NativeAd ad;
                if (mAdItems.size() > position / AD_DISPLAY_FREQUENCY) {
                    ad = mAdItems.get(position / AD_DISPLAY_FREQUENCY);
                } else {
                    ad = mNativeAdsManager.nextNativeAd();
                    if (!ad.isAdInvalidated()) {
                        mAdItems.add(ad);
                    } else {
                        Log.w("HomeFragment", "Ad is invalidated!");
                    }
                }
                AdHolder adHolder=(AdHolder)holdermain;
                adHolder.adChoicesContainer.removeAllViews();
                if (ad != null) {

                    adHolder.tvAdTitle.setText(ad.getAdvertiserName());
                    adHolder.tvAdBody.setText(ad.getAdBodyText());
                    adHolder.tvAdSocialContext.setText(ad.getAdSocialContext());
                    adHolder.tvAdSponsoredLabel.setText(R.string.sponsored);
                    adHolder.btnAdCallToAction.setText(ad.getAdCallToAction());
                    adHolder.btnAdCallToAction.setVisibility(
                            ad.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                    AdOptionsView adOptionsView =
                            new AdOptionsView(context, ad, adHolder.nativeAdLayout);
                    adHolder.adChoicesContainer.addView(adOptionsView, 0);

                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(adHolder.ivAdIcon);
                    clickableViews.add(adHolder.mvAdMedia);
                    clickableViews.add(adHolder.btnAdCallToAction);
                    ad.registerViewForInteraction(
                            adHolder.nativeAdLayout,
                            adHolder.mvAdMedia,
                            adHolder.ivAdIcon,
                            clickableViews);
                }

                break;
            case MENU_ITEM_VIEW_TYPE:
            default:
                //use Glide to set images in recyclerview.
                int index;
                if(position==0){
                    index=0;
                }
                else {
                    index = position - (position / AD_DISPLAY_FREQUENCY) - 1;
                }
                StorageReference thisimage = (StorageReference) Downloadimages.get(index);
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
    private void updatedata(final vholder holder, String postReferenceName,final Context context1) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference memePhotoReferences = database.getReference("memepicture").child(postReferenceName);
        try{
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
                        Map<String, Boolean> map = (Map) dataSnapshot.getValue();
                        if (map != null) {
                            if (map.containsKey(uid)) {
                                holder.tabfollow.setText("following");
                            } else {
                                holder.tabfollow.setText("+follow");
                            }
                        } else {
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
        catch (NullPointerException e){
            System.out.print("NullPointerException Caught");
        }

    }

    private void onfollowClicked(final vholder holder, final String postReferenceName, final Context context1) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference memePhotoReferences = database.getReference("memepicture").child(postReferenceName);
        try{
        memePhotoReferences.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                //uid of the user who uploaded post
                final String uid = post.getUid();
                if (!user.getUid().equals(uid)) {
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
                    final DatabaseReference userReferencePost = database.getReference("user_profile").child(uid);
                    userReferencePost.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            UserProfileInfo userProfileInfo = mutableData.getValue(UserProfileInfo.class);
                            if (userProfileInfo == null) {
                                return Transaction.success(mutableData);
                            }
                            if (userProfileInfo.AudienceId.containsKey(user.getUid())) {
                                userProfileInfo.AudienceId.remove(user.getUid());
                                int number = userProfileInfo.getAudience() - 1;
                                userProfileInfo.setAudience(number);
                            } else {
                                userProfileInfo.AudienceId.put(user.getUid(), true);
                                int number = userProfileInfo.getAudience() + 1;
                                userProfileInfo.setAudience(number);
                            }
                            mutableData.setValue(userProfileInfo);
                            return Transaction.success(mutableData);

                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                    updatedata(holder, postReferenceName, context1);
                }
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

    private void  onStarClicked(final vholder holder, final String postReferenceName,final Context context1) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference memePhotoReferences = database.getReference("memepicture").child(postReferenceName);
        try{
        memePhotoReferences.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);


                if (post == null) {
                    return Transaction.success(mutableData);
                }
                String postAuther = post.getUid();
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference postAutherPoints = database.getReference("user_profile").child(postAuther).child("points");

                if (post.stars.containsKey(user.getUid())) {
                    //post unlit.
                    int lit = post.getLit() - 1;
                    post.setLit(lit);
                    post.stars.remove(user.getUid());
                    if (!(post.getUid().equals(user.getUid()))) {
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
                                updatedata(holder, postReferenceName, context1);
                            }
                        });
                    } else {
                        updatedata(holder, postReferenceName, context1);
                    }

                } else {
                    int lit = post.getLit() + 1;
                    post.setLit(lit);
                    post.stars.put(user.getUid(), true);
                    if (!(post.getUid().equals(user.getUid()))) {
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
                                updatedata(holder, postReferenceName, context1);
                            }
                        });
                    } else {
                        updatedata(holder, postReferenceName, context1);
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
        catch (NullPointerException e){
            System.out.print("NullPointerException Caught");
        }


    }



    @Override
    public int getItemCount() {
        return Downloadimages.size()+mAdItems.size();
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


    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return MENU_ITEM_VIEW_TYPE;
        }
        return position % AD_DISPLAY_FREQUENCY == 0 ? NATIVE_AD_VIEW_TYPE : MENU_ITEM_VIEW_TYPE;
    }
    private static class AdHolder extends RecyclerView.ViewHolder {

        NativeAdLayout nativeAdLayout;
        MediaView mvAdMedia;
        MediaView ivAdIcon;
        TextView tvAdTitle;
        TextView tvAdBody;
        TextView tvAdSocialContext;
        TextView tvAdSponsoredLabel;
        Button btnAdCallToAction;
        LinearLayout adChoicesContainer;

        AdHolder(NativeAdLayout adLayout) {
            super(adLayout);

            nativeAdLayout = adLayout;
            mvAdMedia = adLayout.findViewById(R.id.native_ad_media);
            tvAdTitle = adLayout.findViewById(R.id.native_ad_title);
            tvAdBody = adLayout.findViewById(R.id.native_ad_body);
            tvAdSocialContext = adLayout.findViewById(R.id.native_ad_social_context);
            tvAdSponsoredLabel = adLayout.findViewById(R.id.native_ad_sponsored_label);
            btnAdCallToAction = adLayout.findViewById(R.id.native_ad_call_to_action);
            ivAdIcon = adLayout.findViewById(R.id.native_ad_icon);
            adChoicesContainer = adLayout.findViewById(R.id.ad_choices_container);
        }
    }
}
