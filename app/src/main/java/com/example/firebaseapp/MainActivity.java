package com.example.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {

    private SignInButton google_login;
    private Button email_login;
    private EditText email,password;
    private TextView signUpLink;
    private LoginButton facebook_login;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;
    final static private int RC_SIGN_IN=9001;
    private FirebaseUser user;
    String first_name,last_name,gender,profilePicUrl,fb_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        google_login= (SignInButton) findViewById(R.id.sign_in_button);
        email_login= (Button) findViewById(R.id.buttonSignIn);
        email= (EditText) findViewById(R.id.editTextEmail);
        password= (EditText) findViewById(R.id.editTextPassword);
        signUpLink= (TextView) findViewById(R.id.textViewSignUpLink);
        facebook_login = (LoginButton) findViewById(R.id.login_button);
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent=new Intent(MainActivity.this,UserActivity.class);
                    startActivity(intent);
                    //finish();
                }
            }
        };


        mCallbackManager = CallbackManager.Factory.create();
        facebook_login.setReadPermissions("email", "public_profile");
        facebook_login.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("693343351137-jd1umvvpnpuorubh3lm29sl57sk3gvbl.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* MainActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        google_login.setOnClickListener(this);
        email_login.setOnClickListener(this);
        signUpLink.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.sign_in_button){
            googleSignIn();
        }else if (v.getId()==R.id.buttonSignIn){
            signIn(email.getText().toString(),password.getText().toString());
        }else if (v.getId()==R.id.textViewSignUpLink){
            Intent intent=new Intent(MainActivity.this,SignUpActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void signIn(String email,String password){
        if (!validateForm()){
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication Failed",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent=new Intent(MainActivity.this,UserActivity.class);
                            startActivity(intent);
                            //finish();
                        }
                    }
                });

    }

    private void googleSignIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean validateForm(){
        boolean valid=true;
        if (email.getText().toString().isEmpty()){
            email.setError("Invalid");
            valid=false;
        } else{
            email.setError(null);
        }

        if (password.getText().toString().isEmpty()){
            password.setError("Invalid");
            valid=false;
        } else{
            password.setError(null);
        }
        return valid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if (result.isSuccess()){
            GoogleSignInAccount acct= result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        }else {

        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            GraphRequest request = GraphRequest.newMeRequest(
                                    AccessToken.getCurrentAccessToken() ,
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(
                                                JSONObject object,
                                                GraphResponse response) {
                                            try {
                                                fb_id=object.getString("id");
                                                first_name= (String) object.getString("first_name");
                                                last_name=object.getString("last_name");
                                                gender=object.getString("gender");
                                                profilePicUrl=object.getJSONObject("picture").getJSONObject("data").getString("url");
                                                User user=new User();
                                                user.setUid(user.getUid());
                                                user.setFirstName(first_name);
                                                user.setLastName(last_name);
                                                user.setGender(gender);
                                                user.setUserPicUrl(profilePicUrl);
                                                mDatabase.child("users").child(user.getUid()).child("profile").setValue(user);
                                                first_name="";
                                                last_name="";
                                                gender="";
                                                profilePicUrl="";
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields","id,name,email,about,cover,birthday,first_name,gender,last_name,picture");
                            request.setParameters(parameters);
                            request.executeAsync();

                            Intent intent=new Intent(MainActivity.this,UserActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                         if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Google Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(acct.getId())){
                                        Log.d("demo","user exits");
                                        //go to User Page activity
                                        Intent intent=new Intent(MainActivity.this,UserActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        User user= new User();
                                        Log.d("demo","FLname"+acct.getFamilyName());
                                        user.setUid(user.getUid());
                                        user.setFirstName(acct.getGivenName());
                                        user.setLastName(acct.getFamilyName());
                                        user.setGender(null);
                                        user.setUserPicUrl(acct.getPhotoUrl().toString());
                                        mDatabase.child("users").child(user.getUid()).child("profile").setValue(user);
                                        Intent intent=new Intent(MainActivity.this,UserActivity.class);
                                        intent.putExtra("existingUser",user);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
    }
}
