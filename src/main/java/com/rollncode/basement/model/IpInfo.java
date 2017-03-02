package com.rollncode.basement.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rollncode.basement.interfaces.JsonEntity;
import com.rollncode.basement.utility.BaseUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 02/03/17
 */
public final class IpInfo
        implements JsonEntity, Parcelable {

    private String mRaw;

    private String mIp;
    private String mHostname;
    private String mOrganization;

    private String mCountry;
    private String mRegion;
    private String mCity;
    private String mPostal;

    private double mLat;
    private double mLon;

    public IpInfo() {
    }

    private IpInfo(Parcel in) {
        mRaw = in.readString();
        mIp = in.readString();
        mHostname = in.readString();
        mOrganization = in.readString();
        mCountry = in.readString();
        mRegion = in.readString();
        mCity = in.readString();
        mPostal = in.readString();
        mLat = in.readDouble();
        mLon = in.readDouble();
    }

    @Override
    public void fromJson(@NonNull JSONObject object) throws JSONException {
        mRaw = object.toString();

        mIp = BaseUtils.toString(object, "ip");
        mHostname = BaseUtils.toString(object, "hostname");
        mOrganization = BaseUtils.toString(object, "org");

        mCountry = BaseUtils.toString(object, "country");
        mRegion = BaseUtils.toString(object, "region");
        mCity = BaseUtils.toString(object, "city");
        mPostal = BaseUtils.toString(object, "postal");

        final String loc = BaseUtils.toString(object, "loc");
        if (!TextUtils.isEmpty(loc)) {
            try {
                final String[] strings = loc.split(",");

                mLat = Double.parseDouble(strings[0]);
                mLon = Double.parseDouble(strings[1]);

            } catch (Exception ignore) {
            }
        }
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject object = new JSONObject();

        object.put("ip", mIp);
        object.put("hostname", mHostname);
        object.put("org", mOrganization);

        object.put("country", mCountry);
        object.put("region", mRegion);
        object.put("city", mCity);
        object.put("postal", mPostal);

        object.put("loc", mLat + "," + mLon);

        return object;
    }

    @Override
    public String toString() {
        return mRaw;
    }

    @Override
    public int hashCode() {
        return mRaw.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IpInfo && mRaw.equals(obj.toString());
    }

    public String getIp() {
        return mIp;
    }

    public String getHostname() {
        return mHostname;
    }

    public String getOrganization() {
        return mOrganization;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getRegion() {
        return mRegion;
    }

    public String getCity() {
        return mCity;
    }

    public String getPostal() {
        return mPostal;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mRaw);
        dest.writeString(mIp);
        dest.writeString(mHostname);
        dest.writeString(mOrganization);
        dest.writeString(mCountry);
        dest.writeString(mRegion);
        dest.writeString(mCity);
        dest.writeString(mPostal);
        dest.writeDouble(mLat);
        dest.writeDouble(mLon);
    }

    public static final Creator<IpInfo> CREATOR = new Creator<IpInfo>() {
        @Override
        public IpInfo createFromParcel(Parcel source) {
            return new IpInfo(source);
        }

        @Override
        public IpInfo[] newArray(int size) {
            return new IpInfo[size];
        }
    };
}