package com.pramati.ts.oauth.server.security;


import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Example: Signing Requests with Signature Version in Java.
 *
 * @author nagarajan
 * @version 1.0
 * @since <pre>7/8/17 1:07 PM</pre>
 */
public class HmacSignatureBuilder {

    public static final String X_TS_DATE_HEADER = "X-Ts-date";
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final Logger log = LoggerFactory.getLogger(HmacSignatureBuilder.class);
    private static final int REQUEST_EXPIRE_SECONDS = 30 * 1000;
    /* Other variables */
    private final String HMACAlgorithm = "HmacSHA256";
    //private final String HMACAlgorithm = "Hmac";

    private String algorithm = HMACAlgorithm;
    private String apiKey;
    private String apiSecret;
    private String httpMethod;
    private String canonicalURI;
    private TreeMap<String, String> queryParams;
    private TreeMap<String, String> headers;
    private String payload;
    private boolean debug = false;
    private String strSignedHeader;
    private String xTsDate;
    private String currentDate;

    private HmacSignatureBuilder() {
    }

    private HmacSignatureBuilder(Builder builder) {
        apiKey = builder.apiKey;
        apiSecret = builder.apiSecret;
        httpMethod = builder.httpMethod;
        canonicalURI = builder.canonicalURI;
        queryParams = builder.queryParams;
        headers = builder.headers;
        payload = builder.payload;
        debug = builder.debug;
        if (builder.algorithm != null) {
            algorithm = builder.algorithm;
        }
        /* Get current timestamp value.(UTC) */
        xTsDate = builder.date;
        if (xTsDate == null) {
            xTsDate = getTimeStamp();
        }
        currentDate = getDate();
    }

    /**
     * Task 1: Create a Canonical Request for Signature Version 4.
     *
     * @return
     */
    private String prepareCanonicalRequest() {
        StringBuilder canonicalURL = new StringBuilder("");

        /* Step 1.1 Start with the HTTP request httpMethod (GET, PUT, POST, etc.), followed by a newline character. */
        canonicalURL.append(httpMethod.toLowerCase()).append("\n");

        /* Step 1.2 Add the canonical URI parameter, followed by a newline character. */
        canonicalURI = canonicalURI == null || canonicalURI.trim().isEmpty() ? "/" : canonicalURI;
        canonicalURL.append(canonicalURI).append("\n");

        /* Step 1.3 Add the canonical query string, followed by a newline character. */
        StringBuilder queryString = new StringBuilder("");
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> entrySet : queryParams.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                queryString.append(key).append("=").append(encodeParameter(value)).append("&");
            }
            /* @co-author https://github.com/dotkebi @git #1 @date 16th March, 2017 */
            queryString.deleteCharAt(queryString.lastIndexOf("&"));
            queryString.append("\n");
        } else {
            queryString.append("\n");
        }
        canonicalURL.append(queryString);

        /* Step 1.4 Add the canonical headers, followed by a newline character. */
        StringBuilder signedHeaders = new StringBuilder("");
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entrySet : headers.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                signedHeaders.append(key).append(";");
                canonicalURL.append(key).append(":").append(value).append("\n");
            }

            /* Note: Each individual header is followed by a newline character, meaning the complete list ends with a newline character. */
            canonicalURL.append("\n");
        } else {
            canonicalURL.append("\n");
        }

        /* Step 1.5 Add the signed headers, followed by a newline character. */
        if (signedHeaders.length() > 0) {
            strSignedHeader = signedHeaders.substring(0, signedHeaders.length() - 1); // Remove last ";"
        }
        canonicalURL.append(strSignedHeader).append("\n");

        /* Step 1.6 Use a hash (digest) function like SHA256 to create a hashed value from the payload in the body of the HTTP or HTTPS. */
        payload = payload == null ? "" : payload;
        canonicalURL.append(generateHex(payload));

        if (debug) {
            log.debug("##Canonical Request:\n" + canonicalURL.toString());
        }

        return canonicalURL.toString();
    }

    /**
     * Task 2: Create a String to Sign for Signature Version 4.
     *
     * @param canonicalURL
     * @return
     */
    private String prepareStringToSign(String canonicalURL) {
        String stringToSign = "";

        /* Step 2.1 Start with the algorithm designation, followed by a newline character. */
        stringToSign = algorithm + "\n";

        /* Step 2.2 Append the request date value, followed by a newline character. */
        stringToSign += xTsDate + "\n";

        /* Step 2.3 Append the credential scope value, followed by a newline character. */
        // stringToSign += currentDate + "/" + aws4Request + "\n";

        /* Step 2.4 Append the hash of the canonical request that you created in Task 1: Create a Canonical Request for Signature Version 4. */
        String canonicalHash = generateHex(canonicalURL);

        stringToSign += canonicalHash;

        if (debug) {
            log.debug("##String to sign:\n" + stringToSign);
        }

        return stringToSign;
    }


    /**
     * Task 3: Calculate the AWS Signature Version 4.
     *
     * @param stringToSign
     * @return
     */
    private String calculateSignature(String stringToSign) {
        try {
            /* Step 3.1 Derive your signing key */
            byte[] signatureKey = getSignatureKey(apiSecret, currentDate);

            /* Step 3.2 Calculate the signature. */
            byte[] signature = Hmac(signatureKey, stringToSign);
            //byte[] signature = Hmac(signatureKey, "abc");

            /* Step 3.2.1 Encode signature (byte[]) to Hex */
            return bytesToHex(signature);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Task 4: Add the Signing Information to the Request. We'll return Map of
     * all headers put this headers in your request.
     *
     * @return
     */
    public Map<String, String> getHeaders() {

        /* Execute Task 1: Create a Canonical Request for Signature Version 4. */
        String signature = buildSignature();
        String authorizationString = buildAuthorizationString(signature);

        if (signature != null) {
            Map<String, String> header = new HashMap<>(0);
            header.put(X_TS_DATE_HEADER, xTsDate);
            header.put("Authorization", authorizationString);

            if (debug) {
                log.debug("##Signature:\n" + signature);
                log.debug("##Header:");
                for (Map.Entry<String, String> entrySet : header.entrySet()) {
                    log.debug(entrySet.getKey() + " = " + entrySet.getValue());
                }
                log.debug("================================");
            }
            return header;
        } else {
            if (debug) {
                log.debug("##Signature:\n" + signature);
            }
            return null;
        }
    }

    private String buildSignature() {
        if (headers == null) {
            headers = new TreeMap<>();
            headers.put("x-ts-date", xTsDate);
        }
        String canonicalURL = prepareCanonicalRequest();

        /* Execute Task 2: Create a String to Sign for Signature Version 4. */
        String stringToSign = prepareStringToSign(canonicalURL);

        /* Execute Task 3: Calculate the AWS Signature Version 4. */
        return calculateSignature(stringToSign);
    }

    /**
     * Build string for Authorization header.
     *
     * @param strSignature
     * @return
     */
    private String buildAuthorizationString(String strSignature) {
        return algorithm + " " + "Credential=" + apiKey + "/" + getDate() + "," + "SignedHeaders=" + strSignedHeader + "," + "Signature=" + strSignature;
    }

    /**
     * Generate Hex code of String.
     *
     * @param data
     * @return
     */
    private String generateHex(String data) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Apply Hmac on data using given key.
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     * @reference: http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
     */
    private byte[] Hmac(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    /**
     * Generate AWS signature key.
     *
     * @param key
     * @param date
     * @return
     * @throws Exception
     * @reference http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
     */
    private byte[] getSignatureKey(String key, String date) throws Exception {
        byte[] kSecret = (key).getBytes("UTF8");
        return Hmac(kSecret, date);
    }

    /**
     * Convert byte array to Hex
     *
     * @param bytes
     * @return
     */
    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    /**
     * Get timestamp. yyyyMMdd'T'HHmmss'Z'
     *
     * @return
     */
    public static String getTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//server timezone
        return dateFormat.format(new Date());
    }

    /**
     * Get timestamp. yyyyMMdd'T'HHmmss'Z'
     *
     * @return
     */
    private boolean isExpired() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        try {
            Date parse = dateFormat.parse(this.xTsDate);
            long requestDate = parse.getTime();
            ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
            long expireDate = utc.toInstant().toEpochMilli() + REQUEST_EXPIRE_SECONDS;
            return requestDate > expireDate;
        } catch (ParseException e) {
            return true;
        }


    }

    /**
     * Get date. yyyyMMdd
     *
     * @return
     */
    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//server timezone
        return dateFormat.format(new Date());
    }

    /**
     * Using {@link URLEncoder#encode(String, String) } instead of
     * {@link URLEncoder#encode(String) }
     *
     * @param param
     * @return
     * @co-author https://github.com/dotkebi
     * @date 16th March, 2017
     * @git #1
     */
    private String encodeParameter(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            return URLEncoder.encode(param);
        }
    }

    public boolean verify(String expectedSignature) {
        if (isExpired()) {
            return false;
        }
        final String signature = buildSignature();
        if (debug) {
            log.debug(" Signature generated : {}, expected : {}", signature, expectedSignature);
        }
        return MessageDigest.isEqual(signature.getBytes(), expectedSignature.getBytes());
    }

    public String sign() {
        String signature = this.buildSignature();
        return "HmacSHA256 Credential=" + this.apiKey + ", " +
                "SignedHeaders=" + strSignedHeader + ", " +
                "Signature=" + signature;
        //return signature;
    }
    
    public static String createHmacSignature(String path, String signatureTimestamp, String appId, String appSecret) {
        final HmacSignatureBuilder signatureBuilder = new HmacSignatureBuilder.Builder(appId, appSecret)
                .scheme("https")
                .httpMethod(HttpGet.METHOD_NAME)
                .canonicalURI(path)
                //.headers(getCanonicalizeHeaders(conn))
                .date(signatureTimestamp)
                .build();

        String authHeader = signatureBuilder.sign();
        return authHeader;
    }
    
    public static TreeMap<String, String> getCanonicalizeHeaders(HttpURLConnection conn) {

        TreeMap<String, String> canonicalizeHeaders = new TreeMap<>();
        
        Set<Map.Entry<String, List<String>>> entries = conn.getRequestProperties().entrySet();
        for (Map.Entry<String, List<String>> e : entries) {
            canonicalizeHeaders.put(e.getKey().toLowerCase(), e.getValue().get(0));
        }
        return canonicalizeHeaders;
    }


    public static class Builder {

        private String apiKey;
        private String apiSecret;
        private String httpMethod;
        private String canonicalURI;
        private TreeMap<String, String> queryParams;
        private TreeMap<String, String> headers;
        private String payload;
        private boolean debug = false;
        private String algorithm;
        private String scheme;
        private String host;
        private String date;

        public Builder(String apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public Builder httpMethod(String httpMethodName) {
            this.httpMethod = httpMethodName;
            return this;
        }

        public Builder canonicalURI(String canonicalURI) {
            this.canonicalURI = canonicalURI;
            return this;
        }

        public Builder queryParams(TreeMap<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public Builder headers(TreeMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder debug() {
            this.debug = true;
            return this;
        }

        public HmacSignatureBuilder build() {
            return new HmacSignatureBuilder(this);
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder date(String date) {
            this.date = date;
            return this;
        }
    }
}
