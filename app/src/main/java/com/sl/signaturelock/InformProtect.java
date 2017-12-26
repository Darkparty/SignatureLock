package com.sl.signaturelock;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public final class InformProtect {

    private final static class MySecretKey implements SecretKey {

        private byte[] key = new byte[8];

        public MySecretKey(byte[] key) {
            this.key = key;
        }

        @Override
        public String getAlgorithm() {
            return "DES";
        }

        @Override
        public String getFormat() {
            return "RAW";
        }

        @Override
        public byte[] getEncoded() {
            return key;
        }

    }

    private SecretKey key;

    private Cipher ecipher;
    private Cipher dcipher;

    public InformProtect(byte[] key) {
        this.key = new MySecretKey(key);
        try {
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, this.key);
            dcipher.init(Cipher.DECRYPT_MODE, this.key);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }


    public byte[] encrypt(byte[] input) {
        try {
            byte[] utf8 = Base64.encode(input, Base64.DEFAULT);
            return ecipher.doFinal(utf8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public byte[] decrypt(byte[] input)  {
        try {
            byte[] utf8 = dcipher.doFinal(input);
            return Base64.decode(utf8, Base64.DEFAULT);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
