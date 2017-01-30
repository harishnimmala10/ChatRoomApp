package com.example.firebaseapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText userFirstName,userLastName,userEmail,userPassword1,userPassword2;
    private ImageView profilePicImgView;
    private Button signUpBtn,cancelBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private Uri profilePicUrl;
    private static final int RESULT_LOAD_IMG=1;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    RadioButton male;
    RadioButton female;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userFirstName= (EditText) findViewById(R.id.editTextFirstName);
        userLastName= (EditText) findViewById(R.id.editTextLastName);
        userEmail= (EditText) findViewById(R.id.editTextEmailID);
        userPassword1= (EditText) findViewById(R.id.editTextPassword1);
        male=(RadioButton)findViewById(R.id.radioButton_male);
        female=(RadioButton)findViewById(R.id.radioButton_female);
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                female.setChecked(false);
            }
        });

        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                male.setChecked(false);
            }
        });

        profilePicImgView= (ImageView) findViewById(R.id.imageViewProfilePic);
        signUpBtn= (Button) findViewById(R.id.buttonSignUp);
        cancelBtn= (Button) findViewById(R.id.buttonCancel);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("demo", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("demo", "onAuthStateChanged:signed_out");
                }
            }
        };

        signUpBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        profilePicImgView.setOnClickListener(this);

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
        if (v.getId()==R.id.buttonSignUp){
            signUp(userEmail.getText().toString(),userPassword1.getText().toString());
        }else if (v.getId()==R.id.buttonCancel){
            Intent intent=new Intent(SignUpActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }else if (v.getId()==R.id.imageViewProfilePic){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                && null != data) {
            Uri url = data.getData();
            InputStream iStream = null;
            try {
                iStream = getContentResolver().openInputStream(url);
                byte[] inputData = getBytes(iStream);
                StorageReference photosRef= storage.getReference("profileImages/" + url.getLastPathSegment());

                UploadTask uploadTask=photosRef.putBytes(inputData);
                uploadTask.addOnProgressListener(SignUpActivity.this, new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d("demo","Upload is " + progress + "% done");
                    }
                });
                uploadTask.addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profilePicUrl= taskSnapshot.getDownloadUrl();
                        Picasso.with(getApplicationContext()).load(profilePicUrl.toString()).into(profilePicImgView);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void signUp(String email,String password){
        if (!validateForm()){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,"Sign Up Failed!",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                                onAuthSuccess(task.getResult().getUser());
                        }
                    }
                });
    }

    public boolean validateForm(){
        boolean valid=true;
        if (userFirstName.getText().toString().isEmpty()){
            userFirstName.setError("Required");
            valid=false;
        } else{
            userFirstName.setError(null);
        }
        if (userLastName.getText().toString().isEmpty()){
            userLastName.setError("Required");
            valid=false;
        } else{
            userLastName.setError(null);
        }
        if (!male.isChecked() && !female.isChecked()){
            valid=false;
            Toast.makeText(getApplicationContext(),"Please select a gender",Toast.LENGTH_SHORT).show();
        }else {
            valid=true;
        }
        if (userEmail.getText().toString().isEmpty()){
            userEmail.setError("Required");
            valid=false;
        } else{
            userEmail.setError(null);
        }
        if (userPassword1.getText().toString().isEmpty()){
            userPassword1.setError("Required");
            valid=false;
        } else{
            userPassword1.setError(null);
        }


        return valid;
    }

    private void onAuthSuccess(FirebaseUser user) {

        String firstName=userFirstName.getText().toString();
        String lastName= userLastName.getText().toString();
        String gender="";
        if(male.isChecked()){
            gender= (String) male.getText().toString();
        }
        if(female.isChecked()){
            gender= (String) female.getText().toString();
        }
        Uri photoUrl=profilePicUrl;

        // Write new user
        if (photoUrl!=null) {
            writeNewUser(user.getUid(), firstName, lastName, gender, photoUrl.toString());
        }else {
            writeNewUser(user.getUid(), firstName, lastName, gender, "");
        }

        Toast.makeText(getApplicationContext(),"User Created",Toast.LENGTH_SHORT).show();

        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
    }

    private void writeNewUser(String userID,String firstName,String lastName, String gender, String picUrl) {
        User newUser= new User();
        newUser.setUid(userID);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setGender(gender);
        newUser.setUserPicUrl(picUrl);
        mDatabase.child("users").child(userID).child("profile").setValue(newUser);
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
