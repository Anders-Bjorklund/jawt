package se.hackney.jawt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JwtTest {

    private final String SECRET = "SECRET";
    private final String HEADER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private final String CLAIMS = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4geyB9IDogLCBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9";
    private final String SIGNATURE = "yuX5JHEsMdGPoIAQn6VsoYmV4o7uAsMg4cv0knuTU2w";
    

    @Test
    public void createFromScratchOkTest() {

        Jwt jwt = Jwt.create( SECRET, Algorithm.SHA256)
                .claim( "sub", "1234567890" ).claim( "name", "John { } : , Doe" ).claim( "iat", 1516239022 );

        assertEquals( HEADER, jwt.encodedHeaders());
        assertEquals( CLAIMS, jwt.encodedClaims());
        assertEquals( SIGNATURE, jwt.encodedSignature());

        System.out.println(jwt.toString());

    }

    @Test
    public void createFromSerializedJwtOkTest() {


        Jwt jwt = Jwt.create(SECRET, Algorithm.SHA256,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ._XEngvIuxOcA-j7y_upRUbXli4DLToNf7HxH1XNmxSc");

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", jwt.encodedHeaders());
        assertEquals("eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ", jwt.encodedClaims());
        assertEquals("_XEngvIuxOcA-j7y_upRUbXli4DLToNf7HxH1XNmxSc", jwt.encodedSignature());

    }

    @Test
    public void createFromSerializedJwtWithSpecialCharactersOkTest() {

        Jwt jwt = Jwt.create(SECRET, Algorithm.SHA256,
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJIZWFydCwgb2YgRG0gbSxhcmtuZXNzIiwiaXNzIjoxNjM5MzU3NTAyOTE3fQ.QjL-hNlML3pj9ZTNL_NGjisIjDmwgtfFT1mEftsOHpA");

        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9", jwt.encodedHeaders());
        assertEquals("eyJzdWIiOiJIZWFydCwgb2YgRG0gbSxhcmtuZXNzIiwiaXNzIjoxNjM5MzU3NTAyOTE3fQ", jwt.encodedClaims());
        assertEquals("QjL-hNlML3pj9ZTNL_NGjisIjDmwgtfFT1mEftsOHpA", jwt.encodedSignature());


    }

}
