package dhaliwal.production.memeking.ui.home;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class jadapter extends RecyclerView.Adapter<jadapter.vholder> {

    //arraylist of StorageReferences to display images.
    private ArrayList<StorageReference> Downloadimages;
    //context to use in Glide.
    private Context context;
    private OnNoteListener monNoteListener;

    public interface OnNoteListener{
        void onNoteClick(int position);
    }
    /*
    *@param Downloadimages 1000 StorageReferences's to display in recylerView.
     */
    jadapter(ArrayList<StorageReference> Downloadimages, OnNoteListener monNoteListener, Context context){
        this.monNoteListener=monNoteListener;
        this.Downloadimages=Downloadimages;
        this.context=context;

    }

    @NotNull
    @Override
    public vholder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater flate= LayoutInflater.from(parent.getContext());
        //Edit main Activity tab.
        View view=flate.inflate(R.layout.mainactivitytab,parent,false);
        return new vholder(view,monNoteListener);
    }

    @Override
    public void onBindViewHolder(@NotNull final vholder holder, int position) {
       //use Glide to set images in recyclerview.
        StorageReference thisimage = Downloadimages.get(position);
        final String postReferenceName=thisimage.getName();


        updatedata(holder,postReferenceName);

        Glide.with(context)
                .load(thisimage)
                .placeholder(R.mipmap.placeholder)
                .into(holder.memes);

        //fireimage on click listener
        holder.fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStarClicked(holder,postReferenceName);

            }

        });
        //on follow button clicked
        holder.tabfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onfollowClicked(holder,postReferenceName);

            }
        });




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

                userReferenceApp.addValueEventListener(new ValueEventListener() {
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

    public class vholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView memes;
        ImageView profilePicture;
        ImageView fire;
        TextView litcount;
        TextView username;
        TextView totalpoints;
        TextView tabfollow;


        OnNoteListener onNoteLister;
        vholder(View itemView, OnNoteListener onNoteLister) {
            super(itemView);
            memes = itemView.findViewById(R.id.imageView2);
            fire=itemView.findViewById(R.id.tabfireimage);
            litcount=itemView.findViewById(R.id.tablitnumber);
            username=itemView.findViewById(R.id.tabusername);
            profilePicture=itemView.findViewById(R.id.tabprofilepicture);
            totalpoints=itemView.findViewById(R.id.tabtotalpoints);
            tabfollow=itemView.findViewById(R.id.tabfollow);
            this.onNoteLister=onNoteLister;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteLister.onNoteClick(getAdapterPosition());

        }
    }
}
