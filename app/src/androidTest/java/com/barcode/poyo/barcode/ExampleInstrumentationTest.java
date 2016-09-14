package com.barcode.poyo.barcode;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentationTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.barcode.poyo.barcode", appContext.getPackageName());
    }

    @Test
    public void encEqEnc() throws Exception {
    }

    @Test
    public void decEqDec() throws Exception {
        assertEquals(decrypt("WldSMU1tcHhOV3AzWVdzeWRHWTBjVzR5ZFhJelpXWnk9MDRmYzY4YWYtNmJlZC00OTY3LWI5ZjItMmM4ZjNhYTkyOTI3").length()>0, true);
    }

    @Test
    public void decEqEnc() throws Exception {
        assertEquals("Thisisastring", decrypt(encrypt("Thisisastring")));
        assertEquals("Thisisastring", decrypt(encrypt("Thisisastring")));
    }


    private String encrypt(String stringToSign) {
        byte[] data = null;
        try {
            data = stringToSign.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        Log.d("base64", base64);

        String uuid = UUID.randomUUID().toString();
        base64 += uuid;


        return base64;
    }

    private String decrypt(String secret) {
        Log.d("Secret ",secret.substring(0,4));

        byte[] data1 = Base64.decode(secret, Base64.DEFAULT);

        // Receiving side
        try {
            secret = new String(data1, "UTF-8");
            secret = secret.substring(0, secret.lastIndexOf("="));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch(StringIndexOutOfBoundsException e){
            e.printStackTrace();
        }

        Log.d("Secret ",secret.substring(0,secret.length()));


        data1 = Base64.decode(secret, Base64.DEFAULT);
        String text1 = null;
        try {
            text1 = new String(data1, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(" ", text1);

        return text1;
    }

}