# jawt
Secure Java JWT library. Defaults to proper use.

Does not care about what the JWT has to say about the choice of algorithm. Does not care about what the JWT has to say about certificates.
Does not care about the JWT at all until its signature has been proven correct using your secret.

You will still need to validate any and all claims such as

"iss" (Issuer) Claim
"sub" (Subject) Claim
"aud" (Audience) Claim
"exp" (Expiration Time) Claim
"nbf" (Not Before) Claim
"iat" (Issued At) Claim
"jti" (JWT ID) Claim
"iat" (Issued At) Claim

<br><br>
<b>Example use ( creating a JWT from scratch ):</b>
<br><br>
String SECRET ="SECRET";                       // I'd suggest using a better secret.<br>
Jwt jwt = Jwt.create( SECRET, Algorithm.SHA256).claim( "sub", "My new JWT" ).claim( "aud", "Java developers" ).claim( "iat", 1516239022 );<br>
<br>
By calling jwt.toString() you will receive the serialized version<br>
<b>eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNeSBuZXcgSldUIiwiYXVkIjoiSmF2YSBkZXZlbG9wZXJzIiwiaWF0IjoxNTE2MjM5MDIyfQ.OkdYsq8gjdarkp8haVWsIvXFu_YAeDYbpbgI-DIR3VA</b>
<br>
<br>
<br>
<b>Example use ( reading from an incomming JWT ):<br></b>
<br>
String SECRET ="SECRET";                       // I'd suggest using a better secret.<br>
Jwt jwt = Jwt.create(SECRET, Algorithm.SHA256,<br>
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNeSBuZXcgSldUIiwiYXVkIjoiSmF2YSBkZXZlbG9wZXJzIiwiaWF0IjoxNTE2MjM5MDIyfQ.OkdYsq8gjdarkp8haVWsIvXFu_YAeDYbpbgI-DIR3VA");<br>
    <br>
By calling jwt.claim( "sub" ) you will receive the value<br>
<b>My new JWT</b>
