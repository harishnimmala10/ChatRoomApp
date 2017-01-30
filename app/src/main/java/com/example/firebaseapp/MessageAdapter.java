package com.example.firebaseapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ocpsoft.pretty.time.PrettyTime;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MessageAdapter extends ArrayAdapter<Message> {
    Context mContext;
    int mResource;
    ArrayList<Message> messages;

    public MessageAdapter(Context context, int resource, ArrayList<Message> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.messages = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
        }

        Message message = messages.get(position);
        if(message.getStatus().equals("sent")) convertView.setBackgroundColor(Color.GRAY);
        TextView messageText = (TextView) convertView.findViewById(R.id.message_text);
        messageText.setText(message.getMessage());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.message_image);
        if(message.getImageUrl().matches(""))
        {
            imageView.setVisibility(View.INVISIBLE);
        }else {
            Picasso.with(mContext).load(message.getImageUrl()).into(imageView);
        }
        //Print pretty time
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            date=dateFormat.parse(message.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TextView messageTime = (TextView) convertView.findViewById(R.id.message_time);
        PrettyTime prettyTime = new PrettyTime();
        messageTime.setText(prettyTime.format(date));
        return convertView;
    }
}
