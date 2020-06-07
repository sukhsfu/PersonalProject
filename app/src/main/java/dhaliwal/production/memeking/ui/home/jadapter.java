package dhaliwal.production.memeking.ui.home;
/*for recycleview list reference is taken from tutorial on youtube.
   Video Name:Android RecyclerView Tutorial - Working Example In Hindi | Cheezy Code Hindi
   Video Link:https://youtu.be/IGGT_jfZQrA


*  */
/*
for implementing the clicks on the recyleview  list reference has been taken from tutorial on youtube.
Video Name: RecyclerView OnClickListener (Best practice way)
Video Link :https://youtu.be/69C1ljfDvl0
 */

import android.content.Context;
import android.net.Uri;
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
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

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
    public jadapter(ArrayList<StorageReference> Downloadimages, OnNoteListener monNoteListener,Context context){
        this.monNoteListener=monNoteListener;
        this.Downloadimages=Downloadimages;
        this.context=context;

    }

    @Override
    public vholder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater flate= LayoutInflater.from(parent.getContext());
        //Edit main Activity tab.
        View view=flate.inflate(R.layout.mainactivitytab,parent,false);
        return new vholder(view,monNoteListener);
    }

    @Override
    public void onBindViewHolder(final vholder holder, int position) {
       //use Glide to set images in recyclerview.
        StorageReference thisimage = Downloadimages.get(position);
        String postReferenceName=thisimage.getName();

        final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference memePhotoReferences=database.getReference("memepicture").child(postReferenceName);
        memePhotoReferences.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post=dataSnapshot.getValue(Post.class);
                String uid=post.getUid();
                int litnumber=post.getLit();
                holder.litcount.setText(String.valueOf(litnumber));
                if(post.stars.containsKey(user.getUid())){
                                       Glide.with(context)
                            .load(R.drawable.firecolor)
                            .into(holder.fire);
                }
                else{
                    Glide.with(context)
                            .load(R.drawable.firebw)
                            .into(holder.fire);

                }
                DatabaseReference userReference=database.getReference("user_profile").child(uid);
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserProfileInfo userProfileInfo=dataSnapshot.getValue(UserProfileInfo.class);
                        String username=userProfileInfo.getUsername();
                        String PhotoUri=userProfileInfo.getProfile_photo();
                        holder.username.setText(username);
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
        Glide.with(context)
                .load(thisimage)
                .placeholder(R.drawable.memeimage)
                .into(holder.memes);

        //fireimage on click listener
        holder.fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStarClicked(memePhotoReferences);
            }

        });





    }
    private void  onStarClicked(DatabaseReference memePhotoReferences){
        final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        memePhotoReferences.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post post=mutableData.getValue(Post.class);
                if(post==null){
                    return Transaction.success(mutableData);
                }
                if(post.stars.containsKey(user.getUid())){
                    int lit=post.getLit()-1;
                    post.setLit(lit);
                    post.stars.remove(user.getUid());

                }
                else{
                    int lit=post.getLit()+1;
                    post.setLit(lit);
                    post.stars.put(user.getUid(),true);
                }
                mutableData.setValue(post);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                Log.d("Post", "postTransaction:onComplete:" + databaseError);
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


        OnNoteListener onNoteLister;
        public vholder( View itemView,OnNoteListener  onNoteLister) {
            super(itemView);
            memes = itemView.findViewById(R.id.imageView2);
            fire=itemView.findViewById(R.id.tabfireimage);
            litcount=itemView.findViewById(R.id.tablitnumber);
            username=itemView.findViewById(R.id.tabusername);
            profilePicture=itemView.findViewById(R.id.tabprofilepicture);
            this.onNoteLister=onNoteLister;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteLister.onNoteClick(getAdapterPosition());

        }
    }
}
