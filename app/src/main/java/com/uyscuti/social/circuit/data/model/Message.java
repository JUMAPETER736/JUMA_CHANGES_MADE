package com.uyscuti.social.circuit.data.model;

import android.content.Context;

import androidx.annotation.Nullable;

import com.uyscuti.social.chatsuit.commons.models.IMessage;
import com.uyscuti.social.chatsuit.commons.models.IUser;
import com.uyscuti.social.chatsuit.commons.models.MessageContentType;


import java.util.Date;

/*
 * Created by troy379 on 04.04.17.
 */
import android.os.Parcel;
import android.os.Parcelable;

public class Message implements IMessage,
        MessageContentType.Image,
        MessageContentType,
        Parcelable {

    private String id;
    private String text;
    private Date createdAt;
    private User user;
    private Image image;
    private Voice voice;
    private String messageStatus;

    private Audio audio;
    private Video video; // New video field
    private Document doc; // New document field


    public Message(String id, User user, String text) {
        this(id, user, text, new Date());
    }

    public Message(String id, User user, String text, Date createdAt) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    // Other methods...

    // Parcelable implementation
    protected Message(Parcel in) {
        id = in.readString();
        text = in.readString();
        createdAt = new Date(in.readLong());
        user = in.readParcelable(User.class.getClassLoader());
        image = in.readParcelable(Image.class.getClassLoader());
        voice = in.readParcelable(Voice.class.getClassLoader());
        messageStatus = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(text);
        dest.writeLong(createdAt.getTime());
        dest.writeParcelable(user, flags);
        dest.writeParcelable(image, flags);
        dest.writeParcelable(voice, flags);
        dest.writeString(messageStatus);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }

    @Override
    public Date getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public String getStatus() {
        return this.messageStatus;
    }

    @Override
    public String getImageUrl() {
        return image == null ? null : image.url;
    }

    @Nullable
    @Override
    public String getVideoThumbUrl(Context context, String url) {
        return this.voice.url;
    }

    @Nullable
    @Override
    public String getVideoUrl() {
        return video == null ? null : video.url;
    }

    @Nullable
    @Override
    public String getAudioUrl() {
        return audio == null ? null : audio.url;
    }

    @Nullable
    @Override
    public String getAudio() {
        return audio == null ? null : audio.url;
    }

    @Nullable
    @Override
    public String getAudioTitle() {
        return audio == null ? null : audio.title;
    }

    @Nullable
    @Override
    public String getDocTitle() {
        return doc == null ? null : doc.title;
    }

    @Nullable
    @Override
    public String getDocSize() {
        return doc == null ? null : doc.size;
    }

    @Override
    public long getAudioDuration() {
        return 0;
    }

    @Nullable
    @Override
    public String getDocUrl() {
        return doc == null ? null : doc.url;

    }

    @Nullable
    @Override
    public String getVoiceUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getMessageStatus() {
        return messageStatus;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(User user) {
        this.user= user;
    }

    public Voice getVoice() { return voice; }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public void  setStatus(String status) {
        messageStatus = status;
    }

    // New setter methods for video and document fields
    public void setVideo(Video video) {
        this.video = video;
    }

    public void setDocument(Document doc) {
        this.doc = doc;
    }


    public void setVoice(Voice voice) {
        this.voice = voice;
    }

    public void setCreatedAt(Date time) {
        this.createdAt = time;
    }

    public static class Image implements Parcelable {

        private String url;

        public Image(String url) {
            this.url = url;
        }

        // Parcelable implementation for Image
        protected Image(Parcel in) {
            url = in.readString();
        }

        public static final Creator<Image> CREATOR = new Creator<Image>() {
            @Override
            public Image createFromParcel(Parcel in) {
                return new Image(in);
            }

            @Override
            public Image[] newArray(int size) {
                return new Image[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
        }
    }

    public static class Video implements Parcelable {

        private String url;

        public Video(String url) {
            this.url = url;
        }

        // Parcelable implementation for Video
        protected Video(Parcel in) {
            url = in.readString();
        }

        public static final Creator<Video> CREATOR = new Creator<Video>() {
            @Override
            public Video createFromParcel(Parcel in) {
                return new Video(in);
            }

            @Override
            public Video[] newArray(int size) {
                return new Video[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
        }
    }

    public static class Voice implements Parcelable {

        private String url;
        private int duration;

        public Voice(String url, int duration) {
            this.url = url;
            this.duration = duration;
        }

        // Parcelable implementation for Voice
        protected Voice(Parcel in) {
            url = in.readString();
            duration = in.readInt();
        }

        public static final Creator<Voice> CREATOR = new Creator<Voice>() {
            @Override
            public Voice createFromParcel(Parcel in) {
                return new Voice(in);
            }

            @Override
            public Voice[] newArray(int size) {
                return new Voice[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeInt(duration);
        }

        public String getUrl() {
            return url;
        }

        public int getDuration() {
            return duration;
        }
    }

    public static class Audio implements Parcelable {

        private String url;

        private String title;
        private Long duration;

        public Audio(String url, Long duration, String title) {
            this.url = url;
            this.duration = duration;
            this.title = title;
        }

        // Parcelable implementation for Audio
        protected Audio(Parcel in) {
            url = in.readString();
            duration = in.readLong();
            title = in.readString();
        }

        public static final Creator<Audio> CREATOR = new Creator<Audio>() {
            @Override
            public Audio createFromParcel(Parcel in) {
                return new Audio(in);
            }

            @Override
            public Audio[] newArray(int size) {
                return new Audio[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeLong(duration);
            dest.writeString(title);
        }

        public String getUrl() {
            return url;
        }

        public Long getDuration() {
            return duration;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class Document implements Parcelable {

        private String url;

        private String title;
        private String size;

        public Document(String url, String size, String title) {
            this.url = url;
            this.size = size;
            this.title = title;
        }

        // Parcelable implementation for Document
        protected Document(Parcel in) {
            url = in.readString();
            size = in.readString();
            title = in.readString();
        }

        public static final Creator<Document> CREATOR = new Creator<Document>() {
            @Override
            public Document createFromParcel(Parcel in) {
                return new Document(in);
            }

            @Override
            public Document[] newArray(int size) {
                return new Document[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(size);
            dest.writeString(title);
        }

        public String getUrl() {
            return url;
        }

        public String getSize() {
            return size;
        }

        public String getTitle() {
            return title;
        }
    }
}
