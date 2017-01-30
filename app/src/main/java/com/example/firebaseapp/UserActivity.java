package com.example.firebaseapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,UsersDetailsAdapter.ItemClickCallBack{
    private DatabaseReference mDatabase;
    RecyclerView usersRecyclerView;
    UsersDetailsAdapter usersDetailsAdapter;
    ArrayList<User> usersList=new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private RecyclerView.LayoutManager layoutManager;
    private CharSequence options[] = new CharSequence[] {"View Profile", "Send Message"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        usersRecyclerView= (RecyclerView) findViewById(R.id.usersRecyclerView);
       GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("693343351137-jd1umvvpnpuorubh3lm29sl57sk3gvbl.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    DatabaseReference dbRefUser= ds.getRef();
                    dbRefUser.child("profile").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user=dataSnapshot.getValue(User.class);
                            if (user!=null && !user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                usersList.add(user);
                            }
                            showAllUsers();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.user_menu_options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_option_view_profile:
                Intent intent= new Intent(this,ProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_option_logout:
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                LoginManager.getInstance().logOut();
                Intent intent1=new Intent(this, MainActivity.class);
                startActivity(intent1);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAllUsers(){
        if (usersList!=null && !usersList.isEmpty()){
            usersDetailsAdapter= new UsersDetailsAdapter(UserActivity.this,usersList);
            usersRecyclerView.setLayoutManager(layoutManager);
            usersRecyclerView.setAdapter(usersDetailsAdapter);
            usersDetailsAdapter.setItemClickCallBack(this);
            usersDetailsAdapter.notifyDataSetChanged();



        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("demo","ONConnectionFAiled:"+connectionResult);
    }

    @Override
    public void onItemClick(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setTitle("Select an option");
        builder.setCancelable(true);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){

                    User clickedUser=usersList.get(position);
                    Intent intent=new Intent(UserActivity.this,UserProfile.class);
                    intent.putExtra("clickedUser",clickedUser);
                    startActivity(intent);
                    finish();
                }else if (which==1){
                    Intent intent=new Intent(UserActivity.this,ChatActivity.class);
                    User clickedUser=usersList.get(position);
                    intent.putExtra("clickedUser",clickedUser);
                    startActivity(intent);
                    finish();

                }
            }
        });
        builder.show();

    }
}
