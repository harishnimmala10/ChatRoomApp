package com.example.firebaseapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ChatActivity extends AppCompatActivity{
    ArrayList<Message> chatMessages = new ArrayList<>();
    ListView listView;
    MessageAdapter adapter;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    User user;
    Uri selectedImageUri = null;
    String imageUrl="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("Inbox");
        if(getIntent().getExtras().get("clickedUser")!="") {
            TextView textViewUserName = (TextView) findViewById(R.id.chat_userName);
            user = (User) getIntent().getExtras().get("clickedUser");
            textViewUserName.setText(user.firstName+" " +user.lastName);
            ImageButton imageViewUserDP = (ImageButton) findViewById(R.id.chat_userPic);
            if(!user.getUserPicUrl().isEmpty()) Picasso.with(this).load(user.getUserPicUrl()).into(imageViewUserDP);
            listView = (ListView) findViewById(R.id.message_listview);
            final EditText messageEditText = (EditText) findViewById(R.id.editTextMessage);
            adapter = new MessageAdapter(ChatActivity.this, R.layout.message_layout, chatMessages);
            listView.setAdapter(adapter);
            adapter.setNotifyOnChange(true);
            retrieveChatMessages(user);
            listView.smoothScrollToPosition(chatMessages.size());

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    Toast.makeText(getBaseContext(),"Message Deleted",Toast.LENGTH_LONG).show();
                    adapter.notifyDataSetChanged();
                    SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
                    SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:s");
                    Date date=new Date();
                    try {
                        date= format1.parse(chatMessages.get(i).getTime());
                        Log.d("date:" , date.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    mDatabase.child("messages").child(auth.getCurrentUser().getUid()).child(user.getUid()).child(format.format(date)).removeValue();
                    Log.d("deleted:", auth.getCurrentUser().getUid()+"|"+user.getUid()+"|"+format.format(date));
                    chatMessages.remove(i);
                    //retrieveChatMessages(user);

                    return false;
                }
            });
            findViewById(R.id.imageButtonGallery).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImage();


                }
            });
            findViewById(R.id.imageButtonSend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message = messageEditText.getText().toString().trim();
                    if (message == null || message.matches("")) {
                        Toast.makeText(ChatActivity.this, "Message is empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Message chatMessage = new Message(message, "", dateFormat.format(new Date()),"sent");

                    chatMessages.add(chatMessage);
                    messageEditText.setText("");
                    postMessage(user.getUid(), message, imageUrl);
                    adapter.notifyDataSetChanged();
                    listView.smoothScrollToPosition(chatMessages.size());
                }
            });
        }
        else {
            Toast.makeText(this,"Error!",Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void retrieveChatMessages(User user) {
        DatabaseReference databaseReference = firebaseDatabase.getReference("messages/"+auth.getCurrentUser().getUid()+"/"+
                                                user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> tempMap;
                chatMessages.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    tempMap = (Map<String, String>) ds.getValue();
                    Message chatMessage = new Message();
                    chatMessage.setMessage(tempMap.get("message"));
                    chatMessage.setTime(tempMap.get("time"));
                    chatMessage.setImageUrl(tempMap.get("imageUrl"));
                    chatMessage.setStatus(tempMap.get("status"));
                    chatMessages.add(chatMessage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(chatMessages.size());
    }


    public static void postMessage(String otherUserId,String message, String imageUrl)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String curUserId = auth.getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        Message chatMessage = new Message();
        chatMessage.setMessage(message);
        chatMessage.setImageUrl(imageUrl);
        chatMessage.setStatus("sent");
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        DateFormat format1 = new SimpleDateFormat("yyMMddHHmmss");

        String date=format.format(new Date());
        String date1=format1.format(new Date());
        chatMessage.setTime(date);
        mDatabase.child("messages").child(curUserId).child(otherUserId).child(date1).setValue(chatMessage);
        chatMessage.setStatus("received");
        mDatabase.child("messages").child(otherUserId).child(curUserId).child(date1).setValue(chatMessage);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data!=null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            Uri url = data.getData();
            InputStream iStream = null;
            try {
                iStream = getContentResolver().openInputStream(url);
                byte[] inputData = getBytes(iStream);
                StorageReference photosRef= storage.getReference("media/" + url.getLastPathSegment());
                UploadTask uploadTask=photosRef.putBytes(inputData);
                uploadTask.addOnSuccessListener(ChatActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        selectedImageUri= taskSnapshot.getDownloadUrl();
                        imageUrl=selectedImageUri.toString();
                        //Picasso.with(getApplicationContext()).load(selectedImageUri.toString()).into(profilePicImgView);
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Message chatMessage = new Message("", imageUrl, dateFormat.format(new Date()),"sent");

                        chatMessages.add(chatMessage);
                        postMessage(user.getUid(), "", imageUrl);
                        adapter.notifyDataSetChanged();
                        listView.smoothScrollToPosition(chatMessages.size());
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


}
