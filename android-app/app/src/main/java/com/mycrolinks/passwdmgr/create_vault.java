package com.mycrolinks.passwdmgr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.crypto.tink.KeysetHandle;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mycrolinks.passwdmgr.manager.Encryptor;
import com.mycrolinks.passwdmgr.manager.EnvelopeEncryption;
import com.mycrolinks.passwdmgr.manager.MasterPassword;
import com.mycrolinks.passwdmgr.manager.PasswordItemList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class create_vault extends AppCompatActivity {
    EditText pass_new, pass_conf;
    Button create;

    GoogleSignInAccount signInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vault);
        pass_new = findViewById(R.id.pass_new);
        pass_conf = findViewById(R.id.pass_conf);
        create = findViewById(R.id.create);
        create.setOnClickListener(view -> {
            if (pass_new.getText().toString().equals(pass_conf.getText().toString())) {
                signInAccount = GoogleSignIn.getLastSignedInAccount(this);
                assert signInAccount != null;
                String email = signInAccount.getEmail();
                String rawPassword = pass_conf.getText().toString();
                String hashPassword = Encryptor.hash_wrapper(rawPassword);
                String documentName = Encryptor.hash_wrapper(email + hashPassword);
                Map<String, Object> document = new HashMap<>();
                PasswordItemList passwordItemList = new PasswordItemList();
                KeysetHandle keyHandle = Encryptor.generateKey();
                byte[] passwordListByteArray = passwordItemList.toByteArray();
                byte[] encryptedData = Encryptor.encrypt(keyHandle, passwordListByteArray);
                String keyString = Encryptor.keyToString(keyHandle);
                String encryptedKey = EnvelopeEncryption.encrypt(keyString);
                System.out.println("keyhandle text : " + keyString);
                System.out.println("cipher text : " + encryptedKey);
                document.put("key", encryptedKey);
                document.put("data", Arrays.toString(encryptedData));
                document.put("author", email);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("root").document(documentName).set(document)
                        .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "New Vault Created", Toast.LENGTH_SHORT).show();
                                    MasterPassword.setUser(email, hashPassword);
                                    Toast.makeText(this, "Vault created successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, vault_pin.class));
                                }
                        )
                        .addOnFailureListener(e -> {
                            System.out.println("Error writing document" + e.getMessage());
                            startActivity(new Intent(this, create_vault.class));
                        });
            } else {
                Toast.makeText(this, "Passwords Didn't Match", Toast.LENGTH_SHORT).show();
            }
        });
    }
}