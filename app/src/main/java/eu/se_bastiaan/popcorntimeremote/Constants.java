package eu.se_bastiaan.popcorntimeremote;

public final class Constants {

    public static final Boolean LOG_ENABLED = BuildConfig.IS_DEBUG;
    public static final String PREFS_FILE = "PTRemote_Prefs";
    public static final String DATABASE_NAME = "PTRemote_DB.db";
    public static final Integer DATABASE_VERSION = 1;
    public static final String YOUTUBE_KEY = "AIzaSyC4GRG3DH0HyYvDpmLHqwmpKEhgpCBjduo";

    private Constants() throws InstantiationException {
        throw new InstantiationException("This class is not created for instantiation");
    }

}
