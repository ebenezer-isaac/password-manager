package com.mycrolinks.passwdmgr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.firebase.auth.FirebaseAuth;
import com.mycrolinks.passwdmgr.manager.Encryptor;
import com.mycrolinks.passwdmgr.manager.MasterPassword;

@SuppressWarnings("deprecation")
public class vault_pin extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {
    GoogleSignInAccount signInAccount;
    Button unlock, logout, delete;
    CheckBox checkbox;
    GoogleApiClient googleApiClient;
    String SITE_KEY = "6LcbWZgbAAAAAGj9z9VVweX8s5htypF-5ykAzM5V";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_pin);
        unlock = findViewById(R.id.signin);
        logout = findViewById(R.id.log_but);
        delete = findViewById(R.id.del_but);
        checkbox = findViewById(R.id.checkbox);
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(SafetyNet.API).addConnectionCallbacks(vault_pin.this).build();
        googleApiClient.connect();
        //checkbox.setSelected(true);
        checkbox.setOnClickListener(v -> {
            if (checkbox.isChecked()) {
                SafetyNet.SafetyNetApi.verifyWithRecaptcha(googleApiClient, SITE_KEY).setResultCallback(recaptchaTokenResult -> {
                    Status status = recaptchaTokenResult.getStatus();
                    if (status.isSuccess()) {
                        Toast.makeText(vault_pin.this, "Captcha Verified Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        checkbox.setSelected(false);
                    }
                });
            }
        });

        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
        unlock.setOnClickListener(view -> {
            EditText passwordEditText = findViewById(R.id.password_editText);
            String hashPassword = Encryptor.hash_wrapper(passwordEditText.getText().toString());

            if(checkbox.isChecked()){
                String email = signInAccount.getEmail();
                if (MasterPassword.checkCredentials(email,hashPassword)) {
                    Intent intent = new Intent(getApplicationContext(), vault.class);
                    String documentName = Encryptor.hash_wrapper(email + hashPassword);
                    intent.putExtra("data", documentName);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Verify ReCAPTCHA", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onConnected(@Nullable @org.jetbrains.annotations.Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}