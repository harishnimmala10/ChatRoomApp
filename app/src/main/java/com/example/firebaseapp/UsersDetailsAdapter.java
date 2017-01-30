package com.example.firebaseapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class UsersDetailsAdapter extends RecyclerView.Adapter<UsersDetailsAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<User> mData;
    public ItemClickCallBack itemClickCallBack;

    public UsersDetailsAdapter(Context mContext, ArrayList<User> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    public interface ItemClickCallBack {
        public void onItemClick(int p);
    }

    public void setItemClickCallBack(ItemClickCallBack itemClickCallBack) {
        this.itemClickCallBack = itemClickCallBack;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.user_details_layout, parent, false);
        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user=mData.get(position);
        TextView userName=holder.textViewUserName;
        ImageView userPic=holder.imgViewUserPic;

        userName.setText(user.getFirstName()+" "+user.getLastName());
        if (user.getUserPicUrl()!=null && !user.getUserPicUrl().isEmpty()) {
            Picasso.with(getContext())
                    .load(user.getUserPicUrl())
                    .into(userPic);
        }else {
            userPic.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
        }
    }

    @Override
    public int getItemCount() {
        Log.d("demo","getItemCount"+mData.toString());
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textViewUserName;
        ImageView imgViewUserPic;

        View container;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUserName= (TextView) itemView.findViewById(R.id.textViewUserName);
            imgViewUserPic= (ImageView) itemView.findViewById(R.id.imageUserPic);
            container = itemView.findViewById(R.id.user_profile);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

                itemClickCallBack.onItemClick(getAdapterPosition());

        }
    }

}
