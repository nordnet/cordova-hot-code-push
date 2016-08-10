package com.nordnetab.chcp.main.config;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.main.model.ManifestFile;
import android.util.Base64;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

/**
 * Model for manifest signature.
 * The manifest signature contains a signed hash of the filenames and hashes in the content manifest.
 * Used to cryptographically verify the integrity of the update.
 */
public class ManifestSignature {

    private static final String PKCS8_BEGIN_MARKER = "-----BEGIN PUBLIC KEY-----";
    private static final String PKCS8_END_MARKER = "-----END PUBLIC KEY-----";

    // region Json

    // keys to parse json
    private static class JsonKeys {
        public static final String ALGORITHM = "algorithm";
        public static final String CONTENT_SIGNATURE = "contentSignature";
    }

    /**
     * Create instance of the object from JSON string.
     * JSON string is a content of the chcp.signature file.
     *
     * @param json JSON string to parse
     * @return manifest signature instance
     */
    public static ManifestSignature fromJson(String json) {
        ManifestSignature signature = new ManifestSignature();
        signature.jsonString = json;

        try {
            JsonNode signatureNode = new ObjectMapper().readTree(json);
            String algorithm = signatureNode.get(JsonKeys.ALGORITHM).asText();
            String contentSignature = signatureNode.get(JsonKeys.CONTENT_SIGNATURE).asText();

            signature.jsonString = json;
            signature.algorithm = algorithm;
            signature.contentSignature = contentSignature;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return signature;
    }

    /**
     * Convert object into JSON string
     *
     * @return JSON string
     */
    @Override
    public String toString() {
        if (TextUtils.isEmpty(jsonString)) {
            jsonString = generateJson();
        }

        return jsonString;
    }

    private String generateJson() {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode signatureNode = nodeFactory.objectNode();
        signatureNode.set(JsonKeys.ALGORITHM, nodeFactory.textNode(algorithm));
        signatureNode.set(JsonKeys.CONTENT_SIGNATURE, nodeFactory.textNode(contentSignature));

        return signatureNode.toString();
    }

    // endregion

    private String jsonString;
    private String algorithm;
    private String contentSignature;

    private ManifestSignature() { }

    /**
     * Getter for signature encryption algorithm
     *
     * @return signature encryption algorithm string
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Getter for content signature
     *
     * @return content signature string
     */
    public String getContentSignature() {
        return contentSignature;
    }

    public boolean isContentManifestValid(ContentManifest manifest, String signingPubkey) {

        try {
            int startMarker = signingPubkey.indexOf(PKCS8_BEGIN_MARKER);
            int endMarker = signingPubkey.indexOf(PKCS8_END_MARKER);
            String innerKeyPEM = signingPubkey.substring(startMarker+PKCS8_BEGIN_MARKER.length(), endMarker).trim();
            byte[] innerKeyDER = Base64.decode(innerKeyPEM, Base64.DEFAULT);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(innerKeyDER));
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(pubKey);
            for(ManifestFile manifestFile : manifest.getFiles()) {
                sign.update(manifestFile.name.getBytes("UTF-8"));
                sign.update(manifestFile.hash.getBytes("UTF-8"));
            }
            byte[] signatureBytes = new byte[contentSignature.length()/2];
            for(int i=0; i<contentSignature.length()/2; i++) {
                signatureBytes[i] = (byte) (Integer.parseInt(contentSignature.substring(i*2, i*2+2),16) & 0xff);
            }
            boolean result = sign.verify(signatureBytes);
            return result;
        } catch (Exception e) {
            return false;
        }
    }

}
