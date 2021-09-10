package com.mycrolinks.passwdmgr.manager;

import com.google.common.primitives.Bytes;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Encryptor {

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static String hash(String password, String salt) {
        MessageDigest digest;
        String hash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update((password + salt).getBytes());
            hash = bytesToHexString(digest.digest());
            return hash;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static String hash_wrapper(String password) {
        String hash = password;
        for (int x = 0; x < password.length(); x++) {
            hash = hash(hash, password.charAt(x) + "");
        }
        return hash;
    }

    public static KeysetHandle generateKey() {
        KeysetHandle keyHandle = null;
        try {
            AeadConfig.register();
            keyHandle = KeysetHandle.generateNew(KeyTemplates.get("AES128_GCM"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyHandle;
    }

    public static String keyToString(KeysetHandle keysetHandle) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withOutputStream(oos));
            oos.close();
            return Arrays.toString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] ToByteArray(String array) {
        array = array.replace("[", "").replace("]", "").replace(" ", "");
        List<Byte> list = new ArrayList<>();
        String[] splitArray = array.split(",");
        for (String x : splitArray) {
            list.add((byte) Integer.parseInt(x));
        }
        return Bytes.toArray(list);
    }

    public static KeysetHandle stringToKey(String keysetString) {
        byte[] data = ToByteArray(keysetString);
        System.out.println(data.length);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withInputStream(ois));
            ois.close();
            return keysetHandle;
        } catch (IOException e) {
            data[5] = (byte) -16;
            return stringToKey(Arrays.toString(data));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(KeysetHandle keyHandle, byte[] cipher) {
        try {
            AeadConfig.register();
            Aead aead = keyHandle.getPrimitive(Aead.class);
            return aead.decrypt(cipher, ("password-manager").getBytes());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(KeysetHandle keyHandle, byte[] data) {
        try {
            Aead aead = keyHandle.getPrimitive(Aead.class);
            return aead.encrypt(data, ("password-manager").getBytes());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


}
