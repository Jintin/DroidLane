#!/usr/bin/env groovy

package com.jintin.droidlane.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import java.security.Key

class AESUtils {

    static String encrypt(String plainText, String encryptionKey) throws Exception {
        def cipher = Helper.getCipher()
        try {
            cipher.init(Cipher.ENCRYPT_MODE, Helper.buildKey(encryptionKey), Helper.getIvParameterSpec())
            return Helper.byteArrayToHex(cipher.doFinal(plainText.getBytes()))
        } catch (Exception ignore) {
            return null
        }
    }

    static String decrypt(String text, String encryptionKey) {
        def cipher = Helper.getCipher()
        try {
            cipher.init(Cipher.DECRYPT_MODE, Helper.buildKey(encryptionKey), Helper.getIvParameterSpec())
            byte[] cipherText = Helper.toByteArray(text)
            return new String(cipher.doFinal(cipherText))
        } catch (Exception ignore) {
            return null
        }
    }

    static class Helper {

        static Cipher getCipher() {
            return Cipher.getInstance("AES/CBC/PKCS5Padding")
        }

        static Key buildKey(String password) {
            while (password.length() < 16) {
                password += "0"
            }
            byte[] data = password.getBytes()
            if (data.length > 16) {
                data = Arrays.copyOfRange(data, 0, 16);
            }

            return new SecretKeySpec(data, "AES")
        }

        static IvParameterSpec getIvParameterSpec() {
            def iv = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] as byte[]
            return new IvParameterSpec(iv)
        }

        static byte[] toByteArray(String s) {
            return DatatypeConverter.parseHexBinary(s)
        }

        static String byteArrayToHex(byte[] a) {
            def sb = new StringBuilder(a.length * 2)
            for (def b : a)
                sb.append(String.format("%02x", b & 0xff))
            return sb.toString()
        }
    }
}
