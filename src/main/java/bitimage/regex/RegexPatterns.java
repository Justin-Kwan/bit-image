package bitimage.regex;

public final class RegexPatterns {
  public static final String SPACE = "\\s+";
  public static final String ALPHA_NUMERIC = "^[A-Za-z0-9]+$";
  public static final String ALPHA_NUMERIC_SPACE = "[^a-zA-Z0-9 -]";
  public static final String BOOLEAN = "^true$|^false$";
  public static final String HASH_MD5 = "^[a-f0-9]{32}$";
  public static final String UUID =
      "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
}
