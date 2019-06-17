package com.example.geohospital;


import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;



public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
   // private FirebaseAuth.AuthStateListener mAuth2;
    private FirebaseAuth mAuth;
    static final int RC_SIGN_IN =0;
    private String nombre;
    private Uri personPhoto;

    @Override
    public void onStart() {
        super.onStart();
        //Validar que no haya inicializado.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializar();


       findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId())
                {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }

            }
        });


    }


    private  void inicializar()
    {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build();

        mGoogleSignInClient= GoogleSignIn.getClient(getApplicationContext(),gso);

        mAuth = FirebaseAuth.getInstance();


    }

    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data)
    {


        super.onActivityResult(requestCode, resultCode, data);

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);



        if (requestCode == RC_SIGN_IN)
        {
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);


                firebaseAuthWithGoogle(account);

            }
            catch (ApiException e)
            {
                Toast.makeText(getApplicationContext(),"Error al autenticar: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {

         nombre= acct.getDisplayName();
         personPhoto = acct.getPhotoUrl();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Toast.makeText(getBaseContext(),"Bienvenido",Toast.LENGTH_LONG).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                            String n=  nombre;
                            Uri foto= personPhoto;


                            Intent intent=new Intent(getApplicationContext(),MainActivity.class );
                            intent.putExtra("nombre", nombre);
                            intent.setData(personPhoto);
                            startActivity(intent);


                        } else {

                            updateUI(null);
                        }


                    }
                });
    }


    private void updateUI(FirebaseUser user) {

        if (user != null) {


            findViewById(R.id.sign_in_button).setVisibility(View.GONE);

        } else {


            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);

        }
    }



}
