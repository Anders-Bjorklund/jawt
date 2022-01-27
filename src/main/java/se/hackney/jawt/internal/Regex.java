package se.hackney.jawt.internal;

import java.util.regex.Pattern;

public class Regex {

    public static final Pattern TUPLE = Pattern.compile( "(\"(?<name>[^\"]+)\":(\"(?<string>[^\"]+)\"|(?<other>[^\\,]+))[\\,]?)+?" );
    
}
