package com.uyscuti.social.circuit.data.model;

/*
 * Created by troy379 on 04.04.17.
 */
import android.os.Parcel;
import android.os.Parcelable;

import com.uyscuti.social.chatsuit.commons.models.IUser;

import java.util.Date;

public class User implements IUser, Parcelable {

    private String id;
    private String name;
    private String avatar;
    private boolean online;

    private Date lastSeen;

    public User(String id, String name, String avatar, boolean online, Date lastSeen) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public boolean isOnline() {
        return online;
    }

    public  Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatar = in.readString();
        online = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeByte((byte) (online ? 1 : 0));
    }
}