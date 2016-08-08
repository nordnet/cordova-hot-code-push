package com.nordnetab.chcp.main.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p/>
 * Utility class to calculate hashes.
 *
 * @see MessageDigest
 */
public class Hasher {

    private MessageDigest digest;
    private String hashAlgorithm;

    /**
     * Class constructor.
     */
    public Hasher(String algorithm) {
        try {
            digest = getDigest(algorithm);
            hashAlgorithm = algorithm;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write bytes, based on which we will calculate hash later on.
     *
     * @param bytes  bytes
     * @param length number of bytes to take
     */
    public void write(byte[] bytes, int length) {
        if (digest == null) {
            return;
        }

        digest.update(bytes, 0, length);
    }

    /**
     * Calculate hash based on the received bytes.
     *
     * @return hash string
     */
    public String calculateHash() {
        if (digest == null) {
            return "";
        }

        byte[] hash = digest.digest();
        StringBuilder builder = new StringBuilder();
        if(hashAlgorithm != "md5") {
            builder.append(":");
            builder.append(hashAlgorithm);
            builder.append(":");
        }
        for(byte b : hash) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    /**
     * Constructs appropriate digest for a given hashAlgorithm
     *
     * @return message digest
     */
    private static MessageDigest getDigest(String hashAlgorithm) throws NoSuchAlgorithmException {
        if(hashAlgorithm == "md5") {
            return MessageDigest.getInstance("MD5");
        } else if(hashAlgorithm == "sha1") {
            return MessageDigest.getInstance("SHA-1");            
        } else if(hashAlgorithm == "sha256") {
            return MessageDigest.getInstance("SHA-256");            
        }
        else throw new NoSuchAlgorithmException();
    } 

    /**
     * Identifies the hash algorithm based on the hash string.
     *
     * @return hash algorithm
     */
    public static String identifyHashAlgorithm(String hash) {
        if(hash.startsWith(":md5:")) {
            return "md5";
        } else if(hash.startsWith(":sha1:")) {
            return "sha1";
        } else if(hash.startsWith(":sha256:")) {
            return "sha256";
        } else {
            return "md5";
        }
    }

}