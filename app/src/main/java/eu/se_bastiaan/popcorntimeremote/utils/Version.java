package eu.se_bastiaan.popcorntimeremote.utils;

import android.util.Log;

public class Version implements Comparable<Version> {

    private String version;

    public final String get() {
        return this.version;
    }

    public Version(String version) {
        if(version == null)
            throw new IllegalArgumentException("Version can not be null");
        if(!version.matches("[0-9]+(\\.[0-9]+)*"))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    @Override
    public int compareTo(Version that) {
        if(that == null)
            return 1;
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for(int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if(thisPart < thatPart)
                return -1;
            if(thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object that) {
        if(this == that)
            return true;
        if(that == null)
            return false;
        if(((Object) this).getClass() != that.getClass())
            return false;
        return this.compareTo((Version) that) == 0;
    }

    /**
     * Test if version1 is higher than version2.
     * @param version1
     * @param version2
     * @return {code: true} when version1 is higher than version2.
     */
    public static boolean compare(String version1, String version2) {
        Version v1 = new Version(version1);
        Version v2 = new Version(version2);
        if(v1.compareTo(v2) > 0) {
            return true;
        }
        return false;
    }

}