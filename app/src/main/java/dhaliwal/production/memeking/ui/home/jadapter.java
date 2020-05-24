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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import dhaliwal.production.memeking.R;

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
    public void onBindViewHolder( vholder holder, int position) {
       //use Glide to set images in recyclerview.
        StorageReference thisimage = Downloadimages.get(position);
        Glide.with(context)
                .load(thisimage)
                .placeholder(R.drawable.memeimage)
                .into(holder.memes);

    }

    @Override
    public int getItemCount() {
        return Downloadimages.size();
    }

    public class vholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView memes;
        TextView username;
        OnNoteListener onNoteLister;
        public vholder( View itemView,OnNoteListener  onNoteLister) {
            super(itemView);
            memes = itemView.findViewById(R.id.imageView2);
            username=itemView.findViewById(R.id.textView);
            this.onNoteLister=onNoteLister;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteLister.onNoteClick(getAdapterPosition());

        }
    }
}
