package se.hackney.jawt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import se.hackney.jawt.internal.Regex;

public class Jwt {

    private Mac mac;
    private String algorithm;

    boolean isDirty = true;
    private Map<String, Object> headers = new LinkedHashMap<>();
    private Map<String, Object> claims = new LinkedHashMap<>();

    private String encodedHeaders = "";
    private String encodedClaims = "";
    private String encodedSignature = "";

    public static Jwt create(String secret, Algorithm algorithm) {

        if (secret == null || secret.length() == 0) {
            throw new RuntimeException("NoSecretGivenByUserException");
        }

        if (algorithm == null) {
            throw new RuntimeException("NoAlgorithmSelectedByUserException");
        }

        return new Jwt(secret, algorithm);

    }

    public static Jwt create(String secret, Algorithm algorithm, String jwtAsAString) {

        Jwt jwt = new Jwt(secret, algorithm);

        if (isEmpty(jwtAsAString)) {
            throw new RuntimeException("EmptyJwtException");
        }

        // We only allow Jwt's with three segments. Protect the user.
        String[] segments = jwtAsAString.split("\\.");
        String encodedHeaders = segments[0];
        String encodedClaims = segments[1];
        String encodedSignature = segments[2];

        if (segments.length != 3 || isEmpty(encodedHeaders) || isEmpty(encodedClaims) || isEmpty(encodedSignature)) {
            throw new RuntimeException("WrongNumberOfOrEmptySegmentsException");
        }

        jwt.isDirty = true;

        map(encodedHeaders, jwt.headers);
        map(encodedClaims, jwt.claims);

        String resultingHeaders = jwt.encodedHeaders();
        String resultingClaims = jwt.encodedClaims();
        String resultingSignature = jwt.encodedSignature();


        // Do not accept JWT:s where the signature does not match. Protect the user.
        if (isEmpty(resultingSignature) || isEmpty(encodedSignature) || !resultingSignature.equals(encodedSignature)) {
            throw new RuntimeException("FraudulentSignatureException");
        }

        return jwt;

    }

    // Do not allow the creation of a JWT without a signature or if signed, using an algorithm currently considered too weak. Protect the user.
    private Jwt(String secret, Algorithm algorithm) {

        SecretKeySpec secretKey;

        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm.descriptiveName);

        try {
            this.mac = Mac.getInstance(algorithm.descriptiveName);
            this.algorithm = algorithm.shortName;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException");
        }
        try {
            this.mac.init(secretKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("InvalidKeyException");
        }

    }

    public Jwt header(String key, Object value) {

        isDirty = true;

        // Fail fast in case of missing key
        if (key == null || key.length() == 0) {
            throw new RuntimeException("Missing header name exception.");
        }

        // Don't allow setting ALG-header outside of algorithm choice. Protect the user.
        if ( key.equals("alg" ) ) {
            return this;
        }

        if( key.equals("typ") ) {
            return this;
        }

        if ( value == null ) {
            headers.remove(key);
        } else {
            headers.put(key, value);
        }

        return this;

    }

    public Object header(String key) {
        return headers.get(key);
    }

    public Jwt claim(String key, Object value) {

        isDirty = true;

        // Fail fast in case of missing key
        if (key == null || key.length() == 0) {
            throw new RuntimeException("Missing claim name exception.");
        }

        if (value == null) {
            claims.remove(key);
        } else {
            claims.put(key, value);
        }

        return this;
    }

    public Object claim(String key) {
        return claims.get(key);
    }

    public String encodedHeaders() {

        if (isDirty) {
            toString();
        }

        return encodedHeaders;
    }

    public String encodedClaims() {

        if (isDirty) {
            toString();
        }

        return encodedClaims;
    }

    public String encodedSignature() {

        if (isDirty) {
            toString();
        }

        return encodedSignature;
    }

    public String toString() {

        if (isDirty) {

            // Setting alg and typ here in order to preserve order-of-entry. Values are always set here though.
            // If values are not set before, they will be set now and insertion order will be fixed for these two headers.

            // This is the only way to set the algorithm.
            headers.put("alg", algorithm);

            // Setting default type. Can't be changed.
            headers.put("typ", "JWT");

            encodedHeaders = encode("{" + render(headers) + "}");
            encodedClaims = encode("{" + render(claims) + "}");
            encodedSignature = sign(encodedHeaders + "." + encodedClaims);
        }

        isDirty = false;

        return encodedHeaders + "." + encodedClaims + "." + encodedSignature;

    }

    private String render(Map<String, Object> map) {

        StringBuilder result = new StringBuilder();

        boolean isFirst = true;

        for (String key : map.keySet()) {

            if (!isFirst) {
                result.append(",");
            } else {
                isFirst = false;
            }

            result.append("\"" + key + "\":");

            Object value = map.get(key);

            if (value instanceof String) {
                result.append("\"" + value + "\"");
            } else {
                result.append(("" + value).toLowerCase());
            }

        }

        return result.toString();
    }

    private String encode(String input) {

        byte[] outputBase64UrlEncoded = Base64.getUrlEncoder().withoutPadding()
                .encode(input.getBytes(StandardCharsets.UTF_8));
        return new String(outputBase64UrlEncoded);

    }

    private static String decode(String input) {

        byte[] outputDecoded = Base64.getUrlDecoder()
                .decode(input.getBytes(StandardCharsets.UTF_8));

        return new String(outputDecoded);

    }

    private String sign(String input) {
        byte[] signatureBytes;

        try {
            signatureBytes = mac.doFinal((input).getBytes(StandardCharsets.UTF_8));
        } catch (IllegalStateException e) {
            throw new RuntimeException("IllegalStateException", e);
        }

        byte[] signatureBase64UrlEncoded = Base64.getUrlEncoder().withoutPadding().encode(signatureBytes);
        return new String( signatureBase64UrlEncoded );

    }

    private static void map(String encodedSegment, Map<String, Object> map) {

        String decodedSegment = decode(encodedSegment);

        if (!decodedSegment.startsWith("{") || !decodedSegment.endsWith("}")) {
            throw new RuntimeException("MalformedSegmentException");
        }

        String mapString = decodedSegment.substring(1, decodedSegment.length() - 1);
        Matcher tupleMatcher = Regex.TUPLE.matcher(mapString);

        while (tupleMatcher.find()) {
            String name = tupleMatcher.group("name");
            String string = tupleMatcher.group("string");
            String other = tupleMatcher.group("other");

            if (!isEmpty(string)) {
                map.put(name, string);

            } else {

                if (other.indexOf(".") != -1) {
                    Double ddouble = Double.valueOf(other);
                    map.put(name, ddouble);

                } else {

                    if (other.indexOf("true") != -1 || other.indexOf("false") != -1) {
                        map.put(name, Boolean.valueOf(other));

                    } else {

                        try {
                            Long llong = Long.parseLong(other);
                            map.put(name, llong);

                        } catch (Exception exception) {
                            exception.printStackTrace();
                            ;
                        }

                    }
                }
            }
        }

    }

    private static boolean isEmpty(String input) {

        if (input == null || input.length() == 0) {
            return true;
        }

        return false;

    }

}
