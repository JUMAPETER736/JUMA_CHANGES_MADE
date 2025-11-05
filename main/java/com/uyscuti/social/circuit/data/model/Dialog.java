package com.uyscuti.social.circuit.data.model;

import java.util.ArrayList;

/*
 * Created by troy379 on 04.04.17.
 */
import android.os.Parcel;
import android.os.Parcelable;

import com.uyscuti.social.chatsuit.commons.models.IDialog;

public class Dialog implements IDialog<Message>, Parcelable {

    private String id;
    private String dialogPhoto;
    private String dialogName;
    private ArrayList<User> users;
    private Message lastMessage;

    private Boolean isSelected = false;
    private int unreadCount;

    public Dialog(String id, String name, String photo,
                  ArrayList<User> users, Message lastMessage, int unreadCount) {

        this.id = id;
        this.dialogName = name;
        this.dialogPhoto = photo;
        this.users = users;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public ArrayList<User> getUsers() {
        return users;
    }

    @Override
    public Message getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public boolean getIsSelected() {
        return this.isSelected;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    // Parcelable implementation
    protected Dialog(Parcel in) {
        id = in.readString();
        dialogPhoto = in.readString();
        dialogName = in.readString();
        users = in.createTypedArrayList(User.CREATOR);
        lastMessage = in.readParcelable(Message.class.getClassLoader());
        unreadCount = in.readInt();
        isSelected = in.readByte() != 0;
    }



    public static final Creator<Dialog> CREATOR = new Creator<Dialog>() {
        @Override
        public Dialog createFromParcel(Parcel in) {
            return new Dialog(in);
        }

        @Override
        public Dialog[] newArray(int size) {
            return new Dialog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(dialogPhoto);
        dest.writeString(dialogName);
        dest.writeTypedList(users);
        dest.writeParcelable(lastMessage, flags);
        dest.writeInt(unreadCount);
        dest.writeInt(isSelected ? 1 : 0);
    }
}
