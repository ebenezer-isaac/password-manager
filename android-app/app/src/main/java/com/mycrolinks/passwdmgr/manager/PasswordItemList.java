package com.mycrolinks.passwdmgr.manager;

import com.google.crypto.tink.KeysetHandle;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class PasswordItemList {

    JSONArray passwordItemList;
    String documentName;
    KeysetHandle keysetHandle;

    public PasswordItemList() {
        passwordItemList = new JSONArray();
        PasswordItem dummy = new PasswordItem("Dummy", "example password");
        add(dummy);
    }

    public PasswordItemList(byte[] passwordItemList) {
        try {
            this.passwordItemList = new JSONArray(new String(passwordItemList));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void delete(int index) {
        passwordItemList.remove(index);
        saveToDB();
    }

    public void add(PasswordItem passwordItem) {
        passwordItemList.put(passwordItem.getJSONObject());
        saveToDB();
    }

    public void update(int index, PasswordItem passwordItem) {
        try {
            passwordItemList.remove(index);
            passwordItemList.put(index, passwordItem.getJSONObject());
            saveToDB();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public byte[] toByteArray() {
        return passwordItemList.toString().getBytes();
    }


    public int getSize() {
        return passwordItemList.length();
    }

    public PasswordItem getPasswordItem(int index) {
        try {
            return new PasswordItem((JSONObject) passwordItemList.get(index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveToDB() {
        if (documentName != null) {
            byte[] passwordListByteArray = toByteArray();
            byte[] encryptedData = Encryptor.encrypt(keysetHandle, passwordListByteArray);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("root").document(documentName);
            documentReference
                    .update("data", Arrays.toString(encryptedData))
                    .addOnSuccessListener(aVoid -> System.out.println("DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> System.out.println("Error updating document" + e.getMessage()));
        }
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setKeysetHandle(KeysetHandle keysetHandle) {
        this.keysetHandle = keysetHandle;
    }

}
