package com.mycrolinks.passwdmgr;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.crypto.tink.KeysetHandle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mycrolinks.passwdmgr.manager.CustomAdapter;
import com.mycrolinks.passwdmgr.manager.Encryptor;
import com.mycrolinks.passwdmgr.manager.EnvelopeEncryption;
import com.mycrolinks.passwdmgr.manager.MasterPassword;
import com.mycrolinks.passwdmgr.manager.PasswordItem;
import com.mycrolinks.passwdmgr.manager.PasswordItemList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class vault extends AppCompatActivity {

    GoogleSignInAccount signInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        final ListView list = findViewById(R.id.list);
        String documentName = getIntent().getStringExtra("data");
        DocumentReference documentReference = db.collection("root").document(documentName);
        documentReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String encryptedPasswordList = (String) Objects.requireNonNull(document.getData()).get("data");
                            String encryptedKeysetHandle = (String) Objects.requireNonNull(document.getData()).get("key");
                            System.out.println("encrypted" + encryptedKeysetHandle);
                            String decrypted = EnvelopeEncryption.decrypt(encryptedKeysetHandle);
                            System.out.println("decrypted" + decrypted);
                            KeysetHandle keysetHandle = Encryptor.stringToKey(decrypted);
                            PasswordItemList passwordItemList = new PasswordItemList(Encryptor.decrypt(keysetHandle, Encryptor.ToByteArray(encryptedPasswordList)));
                            passwordItemList.setDocumentName(documentName);
                            passwordItemList.setKeysetHandle(keysetHandle);
                            CustomAdapter customAdapter = new CustomAdapter(this, passwordItemList);
                            list.setAdapter(customAdapter);
                            Button exit = findViewById(R.id.exit);
                            exit.setOnClickListener(v -> {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            });
                            Button add = findViewById(R.id.add);
                            add.setOnClickListener(v -> {
                                LayoutInflater layoutInflater = LayoutInflater.from(this);
                                final View alertBox = layoutInflater.inflate(R.layout.password_item_dialog, null);
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                                alertDialog.setTitle("Credentials");
                                alertDialog.setView(alertBox);
                                EditText itemTitle = alertBox.findViewById(R.id.alert_title);
                                EditText itemPwd = alertBox.findViewById(R.id.alert_password);
                                alertDialog.setIcon(R.drawable.lock_key);
                                alertDialog.setPositiveButton("Save",
                                        (dialog, which) -> {
                                            Date today = Calendar.getInstance().getTime();
                                            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                            passwordItemList.add(new PasswordItem(itemTitle.getText().toString(), itemPwd.getText().toString(), df.format(today)));
                                            Toast.makeText(this, "Credentials Saved", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, vault.class);
                                            intent.putExtra("data", documentName);
                                            this.startActivity(intent);
                                        });
                                alertDialog.setNeutralButton("Copy",
                                        (dialog, which) -> {
                                            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText(itemTitle.getText().toString(), itemPwd.getText().toString());
                                            clipboard.setPrimaryClip(clip);
                                            dialog.cancel();
                                            Toast.makeText(this, "Password Copied to Clipboard", Toast.LENGTH_SHORT).show();
                                        });
                                alertDialog.show();

                            });
                            Button delete = findViewById(R.id.del_but);
                            delete.setOnClickListener(view -> documentReference.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        MasterPassword.delUser(signInAccount.getEmail());
                                        Toast.makeText(vault.this, "Vault successfully deleted!", Toast.LENGTH_SHORT).show();
                                        exit.performClick();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(vault.this, "Error deleting vault", Toast.LENGTH_SHORT).show();
                                        System.out.println(e.getMessage());
                                    }));
                        } else {
                            System.out.println("No such document");
                        }
                    } else {
                        System.out.println("get failed with " + task.getException());
                    }
                });


    }
}