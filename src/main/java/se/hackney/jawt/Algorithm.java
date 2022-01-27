package se.hackney.jawt;

public enum Algorithm {

    SHA256( "HS256", "HmacSHA256" ),
    SHA384( "HS384", "HmacSHA384" ),
    SHA512( "HS512", "HmacSHA512" );
    
    public final String shortName;
    public final String descriptiveName;

    private Algorithm( String shortName, String descriptiveName ) {
        this.shortName = shortName;
        this.descriptiveName = descriptiveName;
    }
    
}
