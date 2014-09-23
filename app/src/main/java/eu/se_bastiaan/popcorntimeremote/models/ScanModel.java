package eu.se_bastiaan.popcorntimeremote.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ScanModel implements Parcelable {
    public String ip;
    public String port;
    public String user;
    public String pass;

    protected ScanModel(Parcel in) {
        ip = in.readString();
        port = in.readString();
        user = in.readString();
        pass = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeString(port);
        dest.writeString(user);
        dest.writeString(pass);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ScanModel> CREATOR = new Parcelable.Creator<ScanModel>() {
        @Override
        public ScanModel createFromParcel(Parcel in) {
            return new ScanModel(in);
        }

        @Override
        public ScanModel[] newArray(int size) {
            return new ScanModel[size];
        }
    };
}