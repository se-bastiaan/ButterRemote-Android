package eu.se_bastiaan.popcorntimeremote.database;

import android.provider.BaseColumns;

public final class InstanceEntry implements BaseColumns {
    public static final String TABLE_NAME = "instances";
    public static final String COLUMN_NAME_NAME = "id";
    public static final String COLUMN_NAME_IP = "ip";
    public static final String COLUMN_NAME_PORT = "port";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_PASSWORD = "password";

    private InstanceEntry() throws InstantiationException {
        throw new InstantiationException("This class is not created for instantiation");
    }

}