package com.uyscuti.social.chatsuit.messages;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;


import com.uyscuti.social.chatsuit.R;
import com.uyscuti.social.chatsuit.commons.ImageLoader;
import com.uyscuti.social.chatsuit.commons.ViewHolder;
import com.uyscuti.social.chatsuit.commons.models.IMessage;
import com.uyscuti.social.chatsuit.commons.models.MessageContentType;
import com.uyscuti.social.chatsuit.utils.CacheManager;
import com.uyscuti.social.chatsuit.utils.DateFormatter;
import com.uyscuti.social.chatsuit.utils.RoundedImageView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MessageHolders {

    private static final short VIEW_TYPE_DATE_HEADER = 130;
    private static final short VIEW_TYPE_TEXT_MESSAGE = 131;
    private static final short VIEW_TYPE_IMAGE_MESSAGE = 132;
    private static final short VIEW_TYPE_VIDEO_MESSAGE = 133;
    private static final short VIEW_TYPE_AUDIO_MESSAGE = 134;
    private static final short VIEW_TYPE_DOCUMENT_MESSAGE = 135;
    private static final short VIEW_TYPE_VOICE_MESSAGE = 136;

    private Class<? extends ViewHolder<Date>> dateHeaderHolder;
    private int dateHeaderLayout;

    private HolderConfig<IMessage> incomingTextConfig;
    private HolderConfig<IMessage> outcomingTextConfig;
    private HolderConfig<MessageContentType.Image> incomingImageConfig;
    private HolderConfig<MessageContentType.Image> outcomingImageConfig;

    private HolderConfig<MessageContentType.Image> outGoingVideoConfig;
    private HolderConfig<MessageContentType.Image> outGoingVoiceConfig;
    private HolderConfig<MessageContentType.Image> outGoingAudioConfig;
    private HolderConfig<MessageContentType.Image> outGoingDocConfig;


    private HolderConfig<MessageContentType.Image> inComingVideoConfig;
    private HolderConfig<MessageContentType.Image> inComingVoiceConfig;
    private HolderConfig<MessageContentType.Image> inComingAudioConfig;
    private HolderConfig<MessageContentType.Image> inComingDocConfig;



    private List<ContentTypeConfig> customContentTypes = new ArrayList<>();
    private ContentChecker contentChecker;



    public MessageHolders() {
        // Date Header
        this.dateHeaderHolder = DefaultDateHeaderViewHolder.class;
        this.dateHeaderLayout = R.layout.item_new_date;

        // Outgoing Messages
        this.outcomingTextConfig = new HolderConfig<>(DefaultOutcomingTextMessageViewHolder.class, R.layout.item_outcoming_text_message);
        this.outcomingImageConfig = new HolderConfig<>(DefaultOutcomingImageMessageViewHolder.class, R.layout.item_outcoming_image_message);
        this.outGoingVideoConfig = new HolderConfig<>(DefaultOutGoingVideoMessageViewHolder.class, R.layout.item_outgoing_video);
        this.outGoingAudioConfig = new HolderConfig<>(DefaultOutGoingAudioMessageViewHolder.class, R.layout.item_outgoing_audio);
        this.outGoingDocConfig = new HolderConfig<>(DefaultOutGoingDocMessageViewHolder.class, R.layout.item_outgoing_doc);
        this.outGoingVoiceConfig = new HolderConfig<>(DefaultOutGoingVoiceMessageViewHolder.class, R.layout.item_outgoing_voice_message);

        // Incoming Messages
        this.incomingTextConfig = new HolderConfig<>(DefaultIncomingTextMessageViewHolder.class, R.layout.item_incoming_text_message);
        this.incomingImageConfig = new HolderConfig<>(DefaultIncomingImageMessageViewHolder.class, R.layout.item_incoming_image_message);
        this.inComingVideoConfig = new HolderConfig<>(DefaultIncomingVideoMessageViewHolder.class, R.layout.item_incoming_video_message);
        this.inComingAudioConfig = new HolderConfig<>(DefaultInComingAudioMessageViewHolder.class, R.layout.item_incoming_audio_message);
        this.inComingDocConfig = new HolderConfig<>(DefaultInComingDocMessageViewHolder.class, R.layout.item_incoming_doc_message);
        this.inComingVoiceConfig = new HolderConfig<>(DefaultInComingVoiceMessageViewHolder.class, R.layout.item_incoming_voice_message);

    }


    private static class ContentTypeConfig<TYPE extends MessageContentType> {

        private byte type;
        private HolderConfig<TYPE> incomingConfig;
        private HolderConfig<TYPE> outcomingConfig;

        private ContentTypeConfig(
                byte type, HolderConfig<TYPE> incomingConfig, HolderConfig<TYPE> outcomingConfig) {

            this.type = type;
            this.incomingConfig = incomingConfig;
            this.outcomingConfig = outcomingConfig;
        }
    }

    private static class HolderConfig<T extends IMessage> {

        protected Class<? extends BaseMessageViewHolder<? extends T>> holder;
        protected int layout;
        protected Object payload;

        HolderConfig(Class<? extends BaseMessageViewHolder<? extends T>> holder, int layout) {
            this.holder = holder;
            this.layout = layout;
        }

        HolderConfig(Class<? extends BaseMessageViewHolder<? extends T>> holder, int layout, Object payload) {
            this.holder = holder;
            this.layout = layout;
            this.payload = payload;
        }
    }

    @SuppressWarnings("unchecked")
    private short getContentViewType(IMessage message) {

        if (message instanceof MessageContentType.Image) {
            MessageContentType.Image imageMessage = (MessageContentType.Image) message;

            // Check for voice message FIRST (highest priority)
            if (imageMessage.getVoiceUrl() != null || imageMessage.getVoiceDuration() > 0) {
                Log.d("Holder Attachments", "Voice Found, Path: " + imageMessage.getVoiceUrl());
                return VIEW_TYPE_VOICE_MESSAGE;
            }

            // Fallback: Check if audio URL contains voice note patterns
            if (imageMessage.getAudioUrl() != null) {
                String audioPath = imageMessage.getAudioUrl();
                if (audioPath.contains("/vn/") || audioPath.contains("rec_")) {
                    Log.d("Holder Attachments", "Voice Found (from audioUrl pattern), Path: " + audioPath);
                    return VIEW_TYPE_VOICE_MESSAGE;
                }
                Log.d("Holder Attachments", "Audio Found, Path: " + audioPath);
                return VIEW_TYPE_AUDIO_MESSAGE;
            }

            // Another fallback: Check if imageUrl is an mp3 (as your ViewHolder does)
            if (imageMessage.getImageUrl() != null && imageMessage.getImageUrl().endsWith(".mp3")) {
                Log.d("Holder Attachments", "Voice Found (from imageUrl), Path: " + imageMessage.getImageUrl());
                return VIEW_TYPE_VOICE_MESSAGE;
            }

            // Check for video
            if (imageMessage.getVideoUrl() != null) {
                Log.d("Holder Attachments", "Video Found, Path: " + imageMessage.getVideoUrl());
                return VIEW_TYPE_VIDEO_MESSAGE;
            }

            // Check for document
            if (imageMessage.getDocUrl() != null) {
                Log.d("Holder Attachments", "Doc Found, Path: " + imageMessage.getDocUrl());
                return VIEW_TYPE_DOCUMENT_MESSAGE;
            }

            // Check for image (regular images)
            if (imageMessage.getImageUrl() != null && !imageMessage.getImageUrl().endsWith(".mp3")) {
                Log.d("Holder Attachments", "Image Found, Image Path: " + imageMessage.getImageUrl());
                return VIEW_TYPE_IMAGE_MESSAGE;
            }
        }

        // Check custom content types
        if (message instanceof MessageContentType) {
            for (int i = 0; i < customContentTypes.size(); i++) {
                ContentTypeConfig config = customContentTypes.get(i);
                if (contentChecker == null) {
                    throw new IllegalArgumentException("ContentChecker cannot be null when using custom content types!");
                }
                boolean hasContent = contentChecker.hasContentFor(message, config.type);
                if (hasContent) return config.type;
            }
        }

        // Default to text message
        return VIEW_TYPE_TEXT_MESSAGE;
    }

    public MessageHolders setIncomingTextConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
            @LayoutRes int layout) {
        this.incomingTextConfig.holder = holder;
        this.incomingTextConfig.layout = layout;
        return this;
    }

    public MessageHolders setIncomingTextConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
            @LayoutRes int layout,
            Object payload) {
        this.incomingTextConfig.holder = holder;
        this.incomingTextConfig.layout = layout;
        this.incomingTextConfig.payload = payload;
        return this;
    }

    public MessageHolders setIncomingTextHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder) {
        this.incomingTextConfig.holder = holder;
        return this;
    }


    public MessageHolders setIncomingTextHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
            Object payload) {
        this.incomingTextConfig.holder = holder;
        this.incomingTextConfig.payload = payload;
        return this;
    }


    public MessageHolders setIncomingTextLayout(@LayoutRes int layout) {
        this.incomingTextConfig.layout = layout;
        return this;
    }


    public MessageHolders setIncomingTextLayout(@LayoutRes int layout, Object payload) {
        this.incomingTextConfig.layout = layout;
        this.incomingTextConfig.payload = payload;
        return this;
    }


    public MessageHolders setOutcomingTextConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
            @LayoutRes int layout) {
        this.outcomingTextConfig.holder = holder;
        this.outcomingTextConfig.layout = layout;
        return this;
    }


    public MessageHolders setOutcomingTextConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
            @LayoutRes int layout,
            Object payload) {
        this.outcomingTextConfig.holder = holder;
        this.outcomingTextConfig.layout = layout;
        this.outcomingTextConfig.payload = payload;
        return this;
    }


    public MessageHolders setOutcomingTextHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder) {
        this.outcomingTextConfig.holder = holder;
        return this;
    }


    public MessageHolders setOutcomingTextHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends IMessage>> holder,
            Object payload) {
        this.outcomingTextConfig.holder = holder;
        this.outcomingTextConfig.payload = payload;
        return this;
    }


    public MessageHolders setOutcomingTextLayout(@LayoutRes int layout) {
        this.outcomingTextConfig.layout = layout;
        return this;
    }

    public MessageHolders setOutcomingTextLayout(@LayoutRes int layout, Object payload) {
        this.outcomingTextConfig.layout = layout;
        this.outcomingTextConfig.payload = payload;
        return this;
    }


    public MessageHolders setIncomingImageConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder,
            @LayoutRes int layout) {
        this.incomingImageConfig.holder = holder;
        this.incomingImageConfig.layout = layout;
        return this;
    }


    public MessageHolders setIncomingImageConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder,
            @LayoutRes int layout,
            Object payload) {
        this.incomingImageConfig.holder = holder;
        this.incomingImageConfig.layout = layout;
        this.incomingImageConfig.payload = payload;
        return this;
    }


    public MessageHolders setIncomingImageHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder) {
        this.incomingImageConfig.holder = holder;
        return this;
    }


    public MessageHolders setIncomingImageHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder,
            Object payload) {
        this.incomingImageConfig.holder = holder;
        this.incomingImageConfig.payload = payload;
        return this;
    }


    public MessageHolders setIncomingImageLayout(@LayoutRes int layout) {
        this.incomingImageConfig.layout = layout;
        return this;
    }


    public MessageHolders setIncomingImageLayout(@LayoutRes int layout, Object payload) {
        this.incomingImageConfig.layout = layout;
        this.incomingImageConfig.payload = payload;
        return this;
    }


    public MessageHolders setOutcomingImageConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder,
            @LayoutRes int layout) {
        this.outcomingImageConfig.holder = holder;
        this.outcomingImageConfig.layout = layout;
        return this;
    }


    public MessageHolders setOutcomingImageConfig(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder,
            @LayoutRes int layout,
            Object payload) {
        this.outcomingImageConfig.holder = holder;
        this.outcomingImageConfig.layout = layout;
        this.outcomingImageConfig.payload = payload;
        return this;
    }


    public MessageHolders setOutcomingImageHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder) {
        this.outcomingImageConfig.holder = holder;
        return this;
    }


    public MessageHolders setOutcomingImageHolder(
            @NonNull Class<? extends BaseMessageViewHolder<? extends MessageContentType.Image>> holder,
            Object payload) {
        this.outcomingImageConfig.holder = holder;
        this.outcomingImageConfig.payload = payload;
        return this;
    }

    public MessageHolders setOutcomingImageLayout(@LayoutRes int layout) {
        this.outcomingImageConfig.layout = layout;
        return this;
    }

    public MessageHolders setOutcomingImageLayout(@LayoutRes int layout, Object payload) {
        this.outcomingImageConfig.layout = layout;
        this.outcomingImageConfig.payload = payload;
        return this;
    }


    public MessageHolders setDateHeaderConfig(
            @NonNull Class<? extends ViewHolder<Date>> holder,
            @LayoutRes int layout) {
        this.dateHeaderHolder = holder;
        this.dateHeaderLayout = layout;
        return this;
    }


    public MessageHolders setDateHeaderHolder(@NonNull Class<? extends ViewHolder<Date>> holder) {
        this.dateHeaderHolder = holder;
        return this;
    }

    public MessageHolders setDateHeaderLayout(@LayoutRes int layout) {
        this.dateHeaderLayout = layout;
        return this;
    }


    public <TYPE extends MessageContentType>
    MessageHolders registerContentType(
            byte type, @NonNull Class<? extends BaseMessageViewHolder<TYPE>> holder,
            @LayoutRes int incomingLayout,
            @LayoutRes int outcomingLayout,
            @NonNull ContentChecker contentChecker) {

        return registerContentType(type,
                holder, incomingLayout,
                holder, outcomingLayout,
                contentChecker);
    }


    public <TYPE extends MessageContentType>
    MessageHolders registerContentType(
            byte type,
            @NonNull Class<? extends BaseMessageViewHolder<TYPE>> incomingHolder, @LayoutRes int incomingLayout,
            @NonNull Class<? extends BaseMessageViewHolder<TYPE>> outcomingHolder, @LayoutRes int outcomingLayout,
            @NonNull ContentChecker contentChecker) {

        if (type == 0)
            throw new IllegalArgumentException("content type must be greater or less than '0'!");

        customContentTypes.add(
                new ContentTypeConfig<>(type,
                        new HolderConfig<>(incomingHolder, incomingLayout),
                        new HolderConfig<>(outcomingHolder, outcomingLayout)));
        this.contentChecker = contentChecker;
        return this;
    }


    public <TYPE extends MessageContentType>
    MessageHolders registerContentType(
            byte type,
            @NonNull Class<? extends BaseMessageViewHolder<TYPE>> incomingHolder, Object incomingPayload, @LayoutRes int incomingLayout,
            @NonNull Class<? extends BaseMessageViewHolder<TYPE>> outcomingHolder, Object outcomingPayload, @LayoutRes int outcomingLayout,
            @NonNull ContentChecker contentChecker) {

        if (type == 0)
            throw new IllegalArgumentException("content type must be greater or less than '0'!");

        customContentTypes.add(
                new ContentTypeConfig<>(type,
                        new HolderConfig<>(incomingHolder, incomingLayout, incomingPayload),
                        new HolderConfig<>(outcomingHolder, outcomingLayout, outcomingPayload)));
        this.contentChecker = contentChecker;
        return this;
    }


    public interface ContentChecker<MESSAGE extends IMessage> {

        boolean hasContentFor(MESSAGE message, byte type);
    }


    private int selectedItemsCount = 0;

    @SuppressWarnings("unchecked")
    protected void bind(final ViewHolder holder, final Object item, boolean isSelected, boolean isGroup,
                        final ImageLoader imageLoader,
                        final View.OnClickListener onMessageClickListener,
                        final View.OnLongClickListener onMessageLongClickListener,
                        final DateFormatter.Formatter dateHeadersFormatter,
                        final SparseArray<MessagesListAdapter.OnMessageViewClickListener> clickListenersArray,
                        final MessagesListAdapter.DateFormatterListener dateFormatterListener,
                        final MessagesListAdapter.OnDownloadListener downloadListener,
                        final MessagesListAdapter.OnMediaClickListener mediaClickListener,
                        final MessagesListAdapter.OnAudioPlayListener audioPlayListener,
                        final MessagesListAdapter adapter  // Replace YourMessageType with your actual message type
    ) {

        if (item instanceof IMessage) {
            ((BaseMessageViewHolder) holder).isSelected = isSelected;
            ((BaseMessageViewHolder) holder).isGroup = isGroup;
            ((BaseMessageViewHolder) holder).adapter = adapter;
            ((BaseMessageViewHolder) holder).imageLoader = imageLoader;
            ((BaseMessageViewHolder) holder).downloadListener = downloadListener;
            ((BaseMessageViewHolder) holder).mediaClickListener = mediaClickListener;
            ((BaseMessageViewHolder) holder).audioPlayListener = audioPlayListener;
            holder.itemView.setOnLongClickListener(onMessageLongClickListener);
            holder.itemView.setOnClickListener(onMessageClickListener);



            for (int i = 0; i < clickListenersArray.size(); i++) {
                final int key = clickListenersArray.keyAt(i);
                final View view = holder.itemView.findViewById(key);
                if (view != null) {
                    view.setOnClickListener(v ->


                            clickListenersArray.get(key).onMessageViewClick(view, (IMessage) item));
                }
            }
        } else if (item instanceof Date) {

            ((DefaultDateHeaderViewHolder) holder).dateListener = dateFormatterListener;


        }

        holder.onBind(item);
    }


    protected int getViewType(Object item, String senderId) {
        boolean isOutcoming = false;
        int viewType;

        if (item instanceof IMessage) {
            IMessage message = (IMessage) item;
            isOutcoming = message.getUser().getId().contentEquals(senderId);
            viewType = getContentViewType(message);

        } else viewType = VIEW_TYPE_DATE_HEADER;

        return isOutcoming ? viewType * -1 : viewType;
    }

    private ViewHolder getHolder(ViewGroup parent, HolderConfig holderConfig,
                                 MessagesListStyle style) {
        return getHolder(parent, holderConfig.layout, holderConfig.holder, style, holderConfig.payload);
    }

    private <HOLDER extends ViewHolder>
    ViewHolder getHolder(ViewGroup parent, @LayoutRes int layout, Class<HOLDER> holderClass,
                         MessagesListStyle style, Object payload) {

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        try {
            Constructor<HOLDER> constructor = null;
            HOLDER holder;
            try {
                constructor = holderClass.getDeclaredConstructor(View.class, Object.class);
                constructor.setAccessible(true);
                holder = constructor.newInstance(v, payload);
            } catch (NoSuchMethodException e) {
                constructor = holderClass.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                holder = constructor.newInstance(v);
            }
            if (holder instanceof DefaultMessageViewHolder && style != null) {
                ((DefaultMessageViewHolder) holder).applyStyle(style);
            }
            return holder;
        } catch (Exception e) {
            throw new UnsupportedOperationException("Somehow we couldn't create the ViewHolder for message. Please, report this issue on GitHub with full stacktrace in description.", e);
        }
    }


    protected ViewHolder getHolder(ViewGroup parent, int viewType, MessagesListStyle messagesListStyle) {
        switch (viewType) {

            case VIEW_TYPE_DATE_HEADER:
                return getHolder(parent, dateHeaderLayout, dateHeaderHolder, messagesListStyle, null);

            case VIEW_TYPE_TEXT_MESSAGE:
                return getHolder(parent, incomingTextConfig, messagesListStyle);

            case -VIEW_TYPE_TEXT_MESSAGE:
                return getHolder(parent, outcomingTextConfig, messagesListStyle);

            case VIEW_TYPE_IMAGE_MESSAGE:
                return getHolder(parent, incomingImageConfig, messagesListStyle);

            case VIEW_TYPE_VIDEO_MESSAGE:
                return getHolder(parent, inComingVideoConfig, messagesListStyle);

            case VIEW_TYPE_AUDIO_MESSAGE:
                return getHolder(parent, inComingAudioConfig, messagesListStyle);

            case VIEW_TYPE_VOICE_MESSAGE:
                return getHolder(parent, inComingVoiceConfig, messagesListStyle);

            case -VIEW_TYPE_VOICE_MESSAGE:
                return getHolder(parent, outGoingVoiceConfig, messagesListStyle);

            case -VIEW_TYPE_IMAGE_MESSAGE:
                return getHolder(parent, outcomingImageConfig, messagesListStyle);

            case -VIEW_TYPE_VIDEO_MESSAGE:
                return getHolder(parent, outGoingVideoConfig, messagesListStyle);

            case -VIEW_TYPE_AUDIO_MESSAGE:
                return getHolder(parent, outGoingAudioConfig, messagesListStyle);

            case -VIEW_TYPE_DOCUMENT_MESSAGE:
                return getHolder(parent, outGoingDocConfig, messagesListStyle);

            case VIEW_TYPE_DOCUMENT_MESSAGE:
                return getHolder(parent, inComingDocConfig, messagesListStyle);
        }
        throw new IllegalStateException("Wrong message view type...");
    }



    public static void getFileSizeFromUrl(String url, OnFileSizeReceivedListener listener) {
        new Thread(() -> {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                long fileSize = (long) urlConnection.getContentLength();
                urlConnection.disconnect();

                listener.onFileSizeReceived(fileSize);
            } catch (IOException e) {
                // Handle exceptions appropriately
            }
        }).start();
    }

    public static void getAudioDuration(String url, OnAudioDuration listener) {
        new Thread(() -> {
            MediaMetadataRetriever mediaMetadataRetriever = null;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(url);

                    String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                    long durationLong = duration != null ? Long.parseLong(duration) : 0;
                    // durationTextView.setText(formatDuration(durationLong));

                    listener.onDuration(durationLong);
                }

            } catch (Exception e) {
                Log.d("AudioDuration", "Error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Ensure that the MediaMetadataRetriever is released regardless of success or failure
                if (mediaMetadataRetriever != null) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                            mediaMetadataRetriever.release();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }


    public static void getFileSizeFromUrl(Context context, String url, OnFileSizeReceivedListener listener) {
        long cachedFileSize = CacheManager.getCachedFileSize(context, url);

        if (cachedFileSize != -1) {
            // Use the cached file size if available
            listener.onFileSizeReceived(cachedFileSize);
        } else {
            new Thread(() -> {
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    long fileSize = (long) urlConnection.getContentLength();
                    urlConnection.disconnect();

                    // Cache the obtained file size
                    CacheManager.cacheFileSize(context, url, fileSize);

                    listener.onFileSizeReceived(fileSize);
                } catch (IOException e) {
                    // Handle exceptions appropriately
                }
            }).start();
        }
    }


    // Function to format the fileSize to a human-readable format
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public interface OnFileSizeReceivedListener {
        void onFileSizeReceived(long fileSize);
    }

    public interface OnAudioDuration {
        void onDuration(long duration);
    }



    interface DefaultMessageViewHolder {
        void applyStyle(MessagesListStyle style);
    }



    public static abstract class BaseMessageViewHolder<MESSAGE extends IMessage>
            extends ViewHolder<MESSAGE> {

        boolean isSelected;

        protected MessagesListAdapter<MESSAGE> adapter;

        int selectedItemCount;

        boolean isGroup;


        protected Object payload;


        protected ImageLoader imageLoader;

        protected MessagesListAdapter.OnDownloadListener downloadListener;

        protected MessagesListAdapter.OnMediaClickListener mediaClickListener;

        protected MessagesListAdapter.OnAudioPlayListener audioPlayListener;

        @Deprecated
        public BaseMessageViewHolder(View itemView) {
            super(itemView);
        }

        public BaseMessageViewHolder(View itemView, Object payload) {
            super(itemView);
            this.payload = payload;
        }


        public boolean isSelected() {
            return isSelected;
        }

        public boolean isGroup() {
            return isGroup;
        }


        public boolean isSelectionModeEnabled() {
            return MessagesListAdapter.isSelectionModeEnabled;
        }


        public ImageLoader getImageLoader() {
            return imageLoader;
        }

        protected void configureLinksBehavior(final TextView text) {
            text.setLinksClickable(false);
            text.setMovementMethod(new LinkMovementMethod() {
                @Override
                public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                    boolean result = false;
                    if (!MessagesListAdapter.isSelectionModeEnabled) {
                        result = super.onTouchEvent(widget, buffer, event);
                    }
                    itemView.onTouchEvent(event);
                    return result;
                }
            });
        }
    }


    public static class IncomingTextMessageViewHolder<MESSAGE extends IMessage>
            extends BaseIncomingMessageViewHolder<MESSAGE> {

        protected ViewGroup bubble;
        protected TextView text;

        @Deprecated
        public IncomingTextMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public IncomingTextMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (bubble != null) {
                bubble.setSelected(isSelected());
            }

            if (text != null) {
                text.setText(message.getText());
            }
        }

        @Override
        public void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);
            if (bubble != null) {
                bubble.setPadding(style.getIncomingDefaultBubblePaddingLeft(),
                        style.getIncomingDefaultBubblePaddingTop(),
                        style.getIncomingDefaultBubblePaddingRight(),
                        style.getIncomingDefaultBubblePaddingBottom());
                ViewCompat.setBackground(bubble, style.getIncomingBubbleDrawable());
            }

            if (text != null) {
                text.setTextColor(style.getIncomingTextColor());
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingTextSize());
                text.setTypeface(text.getTypeface(), Typeface.NORMAL);
                text.setAutoLinkMask(style.getTextAutoLinkMask());
                text.setLinkTextColor(style.getIncomingTextLinkColor());
                configureLinksBehavior(text);
            }
        }

        private void init(View itemView) {
            bubble = itemView.findViewById(R.id.bubble);
            text = itemView.findViewById(R.id.messageText);
        }

        @Override
        public void onViewRecycled() {

        }
    }


    public static class OutcomingTextMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseOutcomingMessageViewHolder<MESSAGE> {

        protected ViewGroup bubble;
        protected TextView text;
        protected ProgressBar progressBar;

        @Deprecated
        public OutcomingTextMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public OutcomingTextMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);

            if (bubble != null) {
                bubble.setSelected(isSelected());
            }

            if (text != null) {
                text.setText(message.getText());
            }

            if (progressBar != null) {
                if (Objects.equals(message.getMessageStatus(), "Sending")) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            if (status != null) {
                if (Objects.equals(message.getStatus(), "Sent")) {
                    status.setBackgroundResource(R.drawable.status___sent);
                } else if (Objects.equals(message.getStatus(), "Seen")) {
                    status.setBackgroundResource(R.drawable.status_____seen);
                } else if (Objects.equals(message.getStatus(), "Sending")) {
                    status.setBackgroundResource(R.drawable.status___sending);
                } else if (Objects.equals(message.getStatus(), "Delivered")) {
                    status.setBackgroundResource(R.drawable.status___received);
                } else {
                    status.setBackgroundResource(R.drawable.status_seen);
                }
            }
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);
            if (bubble != null) {
                bubble.setPadding(style.getOutcomingDefaultBubblePaddingLeft(),
                        style.getOutcomingDefaultBubblePaddingTop(),
                        style.getOutcomingDefaultBubblePaddingRight(),
                        style.getOutcomingDefaultBubblePaddingBottom());
                ViewCompat.setBackground(bubble, style.getOutcomingBubbleDrawable());
            }

            if (text != null) {
                text.setTextColor(style.getOutcomingTextColor());
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingTextSize());
                text.setTypeface(text.getTypeface(), Typeface.NORMAL);
                text.setAutoLinkMask(style.getTextAutoLinkMask());
                text.setLinkTextColor(style.getOutcomingTextLinkColor());
                configureLinksBehavior(text);
            }
        }

        private void init(View itemView) {
            bubble = itemView.findViewById(R.id.bubble);
            text = itemView.findViewById(R.id.messageText);
            progressBar = itemView.findViewById(R.id.fileSendProgress);
        }

        @Override
        public void onViewRecycled() {

        }
    }

    //    Incoming Video Holder
    public static class IncomingVideoMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseIncomingMessageViewHolder<MESSAGE> {

        protected ImageView video;

        protected ImageView download;

        protected LinearLayout down;

        protected RelativeLayout downProgress;

        protected TextView vidSize;
        protected View imageOverlay;
        protected View downOverlay;

        protected ImageView playVideo;

        protected ProgressBar progressBar;

        @Deprecated
        public IncomingVideoMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public IncomingVideoMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (video != null && imageLoader != null) {
                imageLoader.loadImage(video, message.getVideoUrl(), getPayloadForImageLoader(message));

                video.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        itemView.performLongClick();
                        return true;
                    }
                });
            }

            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }



            if (down != null) {
                String imageUrl = message.getVideoUrl();
                assert imageUrl != null;

                if (imageUrl.startsWith("/storage/") || imageUrl.startsWith("file://")) {
                    // It's a local file
                    down.setVisibility(View.GONE);
                    download.setVisibility(View.GONE);
                    File imageFile = new File(imageUrl);
                    if (imageFile.isFile()) {
                        down.setVisibility(View.GONE);
                    } else {
                        down.setVisibility(View.VISIBLE);
                    }

                    if (playVideo != null) {
                        playVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (adapter.getSelectedItemsCount() == 0){
                                    mediaClickListener.onMediaClick(message.getVideoUrl(), v, message);
                                } else  {
                                    itemView.performClick();
                                }
                            }
                        });
                    }
                } else {
                    // It's a remote URL
                    down.setVisibility(View.VISIBLE);
                    download.setVisibility(View.VISIBLE);
                    getFileSizeFromUrl(imageUrl, new OnFileSizeReceivedListener() {
                        @Override
                        public void onFileSizeReceived(long fileSize) {
                            Log.d("Download File Size", "File size received: " + fileSize);

                            // Format the fileSize
                            String formattedSize = formatFileSize(fileSize);

                            // Update the UI on the main thread
                            vidSize.post(new Runnable() {
                                @Override
                                public void run() {
                                    // Check if vidSize is not null before setting the text
                                    if (vidSize != null) {
                                        vidSize.setText(formattedSize);
                                    }
                                }
                            });
                        }
                    });

                    if (down != null) {
                        down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                down.setVisibility(View.GONE);
                                download.setVisibility(View.GONE);
                                down.setEnabled(false);
                                down.setClickable(false);
                                down.setFocusable(false);
                                down.setFocusableInTouchMode(false);
                                down.setLongClickable(false);
                                down.setPressed(false);


                                if (downProgress != null) {
                                    downProgress.setVisibility(View.VISIBLE);

                                    downloadListener.onDownloadClick(imageUrl, vidSize, progressBar, video, video, "Videos", message);

                                    downProgress.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            downProgress.setVisibility(View.GONE);
                                            downProgress.setEnabled(false);
                                            downProgress.setClickable(false);
                                            downProgress.setFocusable(false);
                                            downProgress.setFocusableInTouchMode(false);
                                            downProgress.setLongClickable(false);
                                            downProgress.setPressed(false);

                                            down.setVisibility(View.VISIBLE);
                                            download.setVisibility(View.VISIBLE);
                                            down.setEnabled(true);
                                            down.setClickable(true);
                                            down.setFocusable(true);
                                            down.setFocusableInTouchMode(true);
                                            down.setLongClickable(true);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);
            if (time != null) {
                time.setTextColor(style.getIncomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, style.getIncomingImageOverlayDrawable());
            }

            if (downOverlay != null) {
                ViewCompat.setBackground(downOverlay, style.getIncomingImageOverlayDrawable());
            }

            if (vidSize != null) {
                vidSize.setTextColor(style.getOutcomingTextColor());
            }
        }


        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            video = itemView.findViewById(R.id.video);
            imageOverlay = itemView.findViewById(R.id.imageOverlay);
            download = itemView.findViewById(R.id.download);
            vidSize = itemView.findViewById(R.id.vidSize);
            downOverlay = itemView.findViewById(R.id.downloadOverLay);
            downProgress = itemView.findViewById(R.id.downProgress);
            down = itemView.findViewById(R.id.down);
            progressBar = itemView.findViewById(R.id.progressBar);
            playVideo = itemView.findViewById(R.id.playVideo);

            if (video instanceof RoundedImageView) {
                ((RoundedImageView) video).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0
                );
            }
        }

        @Override
        public void onViewRecycled() {

        }
    }

    public static class IncomingImageMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseIncomingMessageViewHolder<MESSAGE> {

        protected ImageView image;

        protected ImageView download;
        protected View imageOverlay;

        protected TextView imgSize;

        protected ProgressBar progressBar;


        protected RelativeLayout downProgress;

        protected LinearLayout down;


        @Deprecated
        public IncomingImageMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public IncomingImageMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (image != null && imageLoader != null) {
                imageLoader.loadImage(image, message.getImageUrl(), getPayloadForImageLoader(message));

                image.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        itemView.performLongClick();
                        return true;
                    }
                });
            }

            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());

            }

            if (download != null) {
                String imageUrl = message.getImageUrl();
                assert imageUrl != null;

                if (imageUrl.startsWith("/storage/") || imageUrl.startsWith("file:/")) {
                    // It's a local file
                    File imageFile = new File(imageUrl);
                    if (imageFile.isFile()) {
                        download.setVisibility(View.GONE);
                        down.setVisibility(View.GONE);
                        down.setVisibility(View.GONE);
                        down.setEnabled(false);
                        down.setClickable(false);
                        down.setFocusable(false);
                        down.setFocusableInTouchMode(false);
                        down.setLongClickable(false);
                        down.setPressed(false);

                        download.setEnabled(false);
                        download.setClickable(false);
                        download.setFocusable(false);
                        download.setFocusableInTouchMode(false);
                        download.setLongClickable(false);
                        download.setPressed(false);

                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (adapter.getSelectedItemsCount() == 0) {
                                    mediaClickListener.onMediaClick(imageUrl, v, message);
                                } else {
                                    itemView.performClick();
                                }
                            }
                        });
                    } else {

                    }
                } else {
                    // It's a remote URL
                    download.setVisibility(View.VISIBLE);
                    down.setVisibility(View.VISIBLE);

                    getFileSizeFromUrl(imageUrl, new OnFileSizeReceivedListener() {
                        @Override
                        public void onFileSizeReceived(long fileSize) {
                            Log.d("Download File Size", "File size received: " + fileSize);

                            // Format the fileSize
                            String formattedSize = formatFileSize(fileSize);

                            // Update the UI on the main thread
                            imgSize.post(new Runnable() {
                                @Override
                                public void run() {
                                    // Check if vidSize is not null before setting the text
                                    if (imgSize != null) {
                                        imgSize.setText(formattedSize);
                                    }
                                }
                            });
                        }
                    });

                    if (down != null) {
                        down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                down.setVisibility(View.GONE);
                                down.setEnabled(false);
                                down.setClickable(false);
                                down.setFocusable(false);
                                down.setFocusableInTouchMode(false);
                                down.setLongClickable(false);
                                down.setPressed(false);


                                if (downProgress != null) {
                                    downProgress.setVisibility(View.VISIBLE);

                                    downloadListener.onDownloadClick(imageUrl, imgSize, progressBar, image, image, "Images", message);

                                    downProgress.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            downProgress.setVisibility(View.GONE);
                                            downProgress.setEnabled(false);
                                            downProgress.setClickable(false);
                                            downProgress.setFocusable(false);
                                            downProgress.setFocusableInTouchMode(false);
                                            downProgress.setLongClickable(false);
                                            downProgress.setPressed(false);

                                            down.setVisibility(View.VISIBLE);
                                            down.setEnabled(true);
                                            down.setClickable(true);
                                            down.setFocusable(true);
                                            down.setFocusableInTouchMode(true);
                                            down.setLongClickable(true);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);
            if (time != null) {
                time.setTextColor(style.getIncomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, style.getIncomingImageOverlayDrawable());
            }

            if (imgSize != null) {
                imgSize.setTextColor(style.getOutcomingTextColor());
            }
        }


        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            image = itemView.findViewById(R.id.image);
            imageOverlay = itemView.findViewById(R.id.imageOverlay);
            download = itemView.findViewById(R.id.download);
            imgSize = itemView.findViewById(R.id.imgSize);
            progressBar = itemView.findViewById(R.id.progressBar);
            down = itemView.findViewById(R.id.down);
            downProgress = itemView.findViewById(R.id.downProgress);

            if (image instanceof RoundedImageView) {
                ((RoundedImageView) image).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0
                );
            }
        }

        @Override
        public void onViewRecycled() {

        }
    }
    public static class OutcomingImageMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseOutcomingMessageViewHolder<MESSAGE> {

        protected ImageView image;

        protected ProgressBar progressBar;
        protected View imageOverlay;

        @Deprecated
        public OutcomingImageMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public OutcomingImageMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);

            if (message.getId().startsWith("Image")) {
                // Your code here for messages starting with "Image:"
                if (image != null && imageLoader != null) {
                    Log.d("MessageHolder", "Binding Image : " + image);
                    if (Objects.equals(message.getMessageStatus(), "Sending")) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                    imageLoader.loadImage(image, message.getImageUrl(), getPayloadForImageLoader(message));

                    // Inside MessageHolders$OutcomingImageMessageViewHolder.onBind method
                    if (adapter != null) {
                        // Disable the click listener if the item is selected or if there are selected items
                        if (!isSelected()) {
                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (adapter.getSelectedItemsCount() == 0) {
                                        mediaClickListener.onMediaClick(message.getImageUrl(), v, message);
                                    } else {

                                        itemView.performClick();

                                    }
                                }
                            });
                        } else {

                        }
                        // Rest of your code
                    } else {
                        // Handle the case when the adapter is null
                    }

                    image.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            itemView.performLongClick();
                            return true;
                        }
                    });


                }

                if (imageOverlay != null) {
                    imageOverlay.setSelected(isSelected());
                }
            } else {

                Log.d("NotFromLocal", "Image");
            }

        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);
            if (time != null) {
                time.setTextColor(style.getOutcomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, style.getOutcomingImageOverlayDrawable());
            }
        }


        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            image = itemView.findViewById(R.id.image);
            progressBar = itemView.findViewById(R.id.fileSendProgress);
            imageOverlay = itemView.findViewById(R.id.imageOverlay);

            if (image instanceof RoundedImageView) {
                ((RoundedImageView) image).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0,
                        R.dimen.message_bubble_corners_radius
                );
            }
        }

        @Override
        public void onViewRecycled() {

        }
    }

    //    InComing Doc Holder
    public static class InComingDocMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseIncomingMessageViewHolder<MESSAGE> {

        protected ImageView documentImageView;

        protected ViewGroup bubble;

        protected TextView docSize;
        protected TextView docTitle;

        protected ImageView downIcon;

        protected RelativeLayout downProgress;

        protected ProgressBar progressBar;


        @Deprecated
        public InComingDocMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public InComingDocMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);

            if (bubble != null) {
                bubble.setSelected(isSelected());
            }

            if (docSize != null) {
                docSize.setText(message.getDocTitle());
            }


            Log.d("AudioDoc", "Document Found Title" + message.getDocTitle());
            Log.d("AudioDoc", "Document Found  Size" + message.getDocSize());
            Log.d("AudioDoc", "Document Found  Url" + message.getDocUrl());


            if (downIcon != null) {
                String url = message.getDocUrl();
                if (url != null) {
                    if (url.startsWith("file:/") || url.startsWith("/storage/")) {
                        downIcon.setVisibility(View.GONE);
                    } else {
                        downIcon.setVisibility(View.VISIBLE);
                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                downIcon.setVisibility(View.GONE);
                                downProgress.setVisibility(View.VISIBLE);
                                downloadListener.onDownloadClick(url, docTitle, progressBar, documentImageView, downIcon, "Documents", message);
                            }
                        });
                    }
                }
            }

            if (docTitle != null) {
                docTitle.setText(message.getDocSize());
                docTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaClickListener.onMediaClick(message.getDocUrl(), v, message);
                    }
                });
            }
            if (documentImageView != null) {
                String docTitle = message.getDocSize();

                if (docTitle != null) {
                    if (docTitle.endsWith(".pdf")) {
                        documentImageView.setBackgroundResource(R.drawable.pdf_document_svgrepo_com); // Set the background for PDF
                    } else if (docTitle.endsWith(".doc") || docTitle.endsWith(".docx")) {
                        documentImageView.setBackgroundResource(R.drawable.word_document_svgrepo_com); // Set the background for DOC
                    } else if (docTitle.endsWith(".txt")) {
                        documentImageView.setBackgroundResource(R.drawable.txt_document_svgrepo_com); // Set the background for DOC
                    } else {
                        documentImageView.setBackgroundResource(R.drawable.gdoc_document_svgrepo_com); // Set the background for DOC
                    }
                }
            }
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);

            if (bubble != null) {
                bubble.setPadding(4,
                        4,
                        4,
                        4);
                ViewCompat.setBackground(bubble, style.getIncomingBubbleDrawable());
            }

            if (time != null) {
                time.setTextColor(style.getIncomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (docSize != null) {
                docSize.setTextColor(style.getIncomingTimeTextColor());
                docSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingImageTimeTextSize());
                docSize.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (docTitle != null) {
                docTitle.setTextColor(style.getIncomingTextColor());
                docTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingTextSize());
                docTitle.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }
        }


        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            docTitle = itemView.findViewById(R.id.docTitle);
            docSize = itemView.findViewById(R.id.docSize);

            documentImageView = itemView.findViewById(R.id.documentImageView);

            bubble = itemView.findViewById(R.id.bubble);

            downIcon = itemView.findViewById(R.id.downIcon);

            downProgress = itemView.findViewById(R.id.downProgress);

            progressBar = itemView.findViewById(R.id.progressBar);

            if (documentImageView instanceof RoundedImageView) {
                ((RoundedImageView) documentImageView).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius
                );
            }
        }

        @Override
        public void onViewRecycled() {

        }
    }

    //    OutGoing Doc Holder
    public static class OutGoingDocMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseOutcomingMessageViewHolder<MESSAGE> {

        protected ImageView documentImageView;

        protected ViewGroup bubble;

        protected TextView docSize;
        protected TextView docTitle;
        protected ProgressBar progressBar;



        @Deprecated
        public OutGoingDocMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public OutGoingDocMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (Objects.equals(message.getMessageStatus(), "Sent")) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (bubble != null) {
                bubble.setSelected(isSelected());
            }

            if (docSize != null) {
                docSize.setText(message.getDocTitle());
            }

            if (Objects.equals(message.getMessageStatus(), "Sending")) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (status != null) {
                if (Objects.equals(message.getStatus(), "Sent")) {
                    status.setBackgroundResource(R.drawable.status___sent);
                } else if (Objects.equals(message.getStatus(), "Seen")) {
                    status.setBackgroundResource(R.drawable.status_____seen);
                } else if (Objects.equals(message.getStatus(), "Sending")) {
                    status.setBackgroundResource(R.drawable.status___sending);
                } else if (Objects.equals(message.getStatus(), "Delivered")) {
                    status.setBackgroundResource(R.drawable.status___received);
                } else {
                    status.setBackgroundResource(R.drawable.status_seen);
                }
            }

            Log.d("AudioDoc", "Document Found Title" + message.getDocTitle());
            Log.d("AudioDoc", "Document Found  Size" + message.getDocSize());
            Log.d("AudioDoc", "Document Found  Url" + message.getDocUrl());


            if (docTitle != null) {
                docTitle.setText(message.getDocSize());
                docTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaClickListener.onMediaClick(message.getDocUrl(), v, message);
                    }
                });
            }
            if (documentImageView != null) {
                String docTitle = message.getDocSize();
                if (docTitle != null) {
                    if (docTitle.endsWith(".pdf")) {
                        documentImageView.setBackgroundResource(R.drawable.pdf_document_svgrepo_com); // Set the background for PDF
                    } else if (docTitle.endsWith(".doc") || docTitle.endsWith(".docx")) {
                        documentImageView.setBackgroundResource(R.drawable.word_document_svgrepo_com); // Set the background for DOC
                    } else if (docTitle.endsWith(".txt")) {
                        documentImageView.setBackgroundResource(R.drawable.txt_document_svgrepo_com); // Set the background for TXT
                    } else {
                        documentImageView.setBackgroundResource(R.drawable.gdoc_document_svgrepo_com); // Set the background for DOC
                    }
                }
            }
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);

            if (bubble != null) {
                bubble.setPadding(4,
                        4,
                        4,
                        4);
                ViewCompat.setBackground(bubble, style.getOutcomingBubbleDrawable());
            }

            if (time != null) {
                time.setTextColor(style.getOutcomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (docSize != null) {
                docSize.setTextColor(style.getOutcomingTimeTextColor());
                docSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingImageTimeTextSize());
                docSize.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (docTitle != null) {
                docTitle.setTextColor(style.getOutcomingTextColor());
                docTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingTextSize());
                docTitle.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }
        }

        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            docTitle = itemView.findViewById(R.id.docTitle);
            docSize = itemView.findViewById(R.id.docSize);
            progressBar = itemView.findViewById(R.id.fileSendProgress);

            documentImageView = itemView.findViewById(R.id.documentImageView);

            bubble = itemView.findViewById(R.id.bubble);

            if (documentImageView instanceof RoundedImageView) {
                ((RoundedImageView) documentImageView).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius
                );
            }
        }

        @Override
        public void onViewRecycled() {

        }
    }

    //    InComing Audio Holder
    public static class InComingAudioMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseIncomingMessageViewHolder<MESSAGE> {

        protected ImageView downIcon;

        protected ViewGroup bubble;

        protected SeekBar audioSeekBar;

        protected TextView audioDuration;

        protected TextView title;
        protected ImageView playAudio;

        protected RelativeLayout downProgress;

        protected ProgressBar progressBar;



        @Deprecated
        public InComingAudioMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public InComingAudioMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);

            if (bubble != null) {
                bubble.setSelected(isSelected());
            }


            if (playAudio != null) {
                playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        audioPlayListener.onAudioPlayClick(message.getAudio(), playAudio, audioDuration, audioSeekBar, message);
                    }
                });
            }



            if (audioDuration != null) {
                long durationSeconds = 200;

                long dueMinutes = durationSeconds / 60;
                long dueSeconds = durationSeconds % 60;
                String formattedDuration = String.format(Locale.getDefault(), "%02d:%02d", dueMinutes, dueSeconds);

                audioDuration.setText(formattedDuration);
            }

            if (title != null) {
                title.setText(message.getAudioTitle());
            }
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);

            if (bubble != null) {
                bubble.setPadding(4,
                        4,
                        4,
                        4);
                ViewCompat.setBackground(bubble, style.getIncomingBubbleDrawable());
            }

            if (time != null) {
                time.setTextColor(style.getIncomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (audioDuration != null) {
                audioDuration.setTextColor(style.getIncomingTimeTextColor());
                audioDuration.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingImageTimeTextSize());
                audioDuration.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (title != null) {
                title.setTextColor(style.getIncomingTextColor());
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingImageTimeTextSize());
                title.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }
        }


        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            title = itemView.findViewById(R.id.audioTitle);
            playAudio = itemView.findViewById(R.id.playAudio);
            audioSeekBar = itemView.findViewById(R.id.audioSeekBar);
            audioDuration = itemView.findViewById(R.id.audioDuration);

            downIcon = itemView.findViewById(R.id.downIcon);
            downProgress = itemView.findViewById(R.id.downProgress);
            progressBar = itemView.findViewById(R.id.progressBar);

//        playAudio = itemView.findViewById(R.id.playVideo);
            bubble = itemView.findViewById(R.id.bubble);


        }

        @Override
        public void onViewRecycled() {

        }
    }


    //    OutGoing Audio Holder
    public static class OutGoingAudioMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseOutcomingMessageViewHolder<MESSAGE> {



        protected ViewGroup bubble;

        protected SeekBar audioSeekBar;

        protected TextView audioDuration;
        protected TextView title;
        protected ImageView playAudio;
        protected ProgressBar progressBar;



        @Deprecated
        public OutGoingAudioMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public OutGoingAudioMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (Objects.equals(message.getMessageStatus(), "Sending")) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
            if (bubble != null) {
                bubble.setSelected(isSelected());
            }


            if (audioDuration != null) {
                long durationSeconds = 100;

                audioDuration.setText("02:33");


            }


            if (Objects.equals(message.getMessageStatus(), "Sending")) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (status != null) {
                if (Objects.equals(message.getStatus(), "Sent")) {
                    status.setBackgroundResource(R.drawable.status___sent);
                } else if (Objects.equals(message.getStatus(), "Seen")) {
                    status.setBackgroundResource(R.drawable.status_____seen);
                } else if (Objects.equals(message.getStatus(), "Sending")) {
                    status.setBackgroundResource(R.drawable.status___sending);
                } else if (Objects.equals(message.getStatus(), "Delivered")) {
                    status.setBackgroundResource(R.drawable.status___received);
                } else {
                    status.setBackgroundResource(R.drawable.status_seen);
                }
            }


            if (playAudio != null) {
                playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        audioPlayListener.onAudioPlayClick(message.getAudio(), playAudio, audioDuration, audioSeekBar, message);
                    }
                });
            }

            if (title != null) {
                title.setText(message.getAudioTitle());
            }
        }

        public static long getCachedOrCalculateAudioDuration(String audioFilePath) {


            if (audioFilePath.startsWith("file://") || audioFilePath.startsWith("/storage/")) {
                // Create or obtain a reference to the SharedPreferences

                long startTime = System.currentTimeMillis();

                try {
                    MediaMetadataRetriever retriever = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                        retriever = new MediaMetadataRetriever();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                        retriever.setDataSource(audioFilePath);
                    }

                    String durationStr = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                        durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    }

                    if (durationStr != null) {
                        long endTime = System.currentTimeMillis();
                        long executionTime = endTime - startTime;

                        Log.d("Audio Duration", "Execution Time: " + executionTime);

                        long duration = Long.parseLong(durationStr);


                        return duration;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                Log.d("Audio Duration", "Execution Time: " + executionTime);

            }
            return 0;
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);

            if (bubble != null) {
                bubble.setPadding(4,
                        4,
                        4,
                        4);
                ViewCompat.setBackground(bubble, style.getOutcomingBubbleDrawable());
            }

            if (time != null) {
                time.setTextColor(style.getOutcomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (audioDuration != null) {
                audioDuration.setTextColor(style.getOutcomingTimeTextColor());
                audioDuration.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingImageTimeTextSize());
                audioDuration.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (title != null) {
                title.setTextColor(style.getOutcomingTimeTextColor());
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingImageTimeTextSize());
                title.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }
        }


        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            title = itemView.findViewById(R.id.audioTitle);
            playAudio = itemView.findViewById(R.id.playAudio);
            audioSeekBar = itemView.findViewById(R.id.audioSeekBar);
            audioDuration = itemView.findViewById(R.id.audioDuration);
            bubble = itemView.findViewById(R.id.bubble);
            progressBar = itemView.findViewById(R.id.fileSendProgress);


        }

        @Override
        public void onViewRecycled() {

        }
    }



    public static class OutGoingVideoMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseOutcomingMessageViewHolder<MESSAGE> {

        protected ImageView video;
        protected ProgressBar progressBar;
        protected ImageView playVideo;
        protected View imageOverlay;

        @Deprecated
        public OutGoingVideoMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public OutGoingVideoMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (video != null && imageLoader != null) {
                Log.d("MessageHolder", "Binding Image : " + video);
                if (Objects.equals(message.getMessageStatus(), "Sending")) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }

                video.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        itemView.performLongClick();
                        return true;
                    }
                });

                imageLoader.loadImage(video, message.getVideoUrl(), getPayloadForImageLoader(message));
            }

            if (Objects.equals(message.getMessageStatus(), "Sending")) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (status != null) {
                if (Objects.equals(message.getStatus(), "Sent")) {
                    status.setBackgroundResource(R.drawable.status___sent);
                } else if (Objects.equals(message.getStatus(), "Seen")) {
                    status.setBackgroundResource(R.drawable.status_____seen);
                } else if (Objects.equals(message.getStatus(), "Sending")) {
                    status.setBackgroundResource(R.drawable.status___sending);
                } else if (Objects.equals(message.getStatus(), "Delivered")) {
                    status.setBackgroundResource(R.drawable.status___received);
                } else {
                    status.setBackgroundResource(R.drawable.status_seen);
                }
            }

            if (playVideo != null) {
                playVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (adapter.getSelectedItemsCount() == 0){
                            mediaClickListener.onMediaClick(message.getVideoUrl(), v, message);
                        } else {
                            itemView.performClick();
                        }
                    }
                });


            }

            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }
        }

        @Override
        public final void applyStyle(MessagesListStyle style) {
            super.applyStyle(style);
            if (time != null) {
                time.setTextColor(style.getOutcomingImageTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingImageTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, style.getOutcomingImageOverlayDrawable());
            }
        }


        protected Object getPayloadForImageLoader(MESSAGE message) {
            return null;
        }

        private void init(View itemView) {
            video = itemView.findViewById(R.id.video);
            progressBar = itemView.findViewById(R.id.fileSendProgress);
            imageOverlay = itemView.findViewById(R.id.videoOverLay);
            playVideo = itemView.findViewById(R.id.playVideo);

            if (video instanceof RoundedImageView) {
                ((RoundedImageView) video).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0,
                        R.dimen.message_bubble_corners_radius
                );
            }
        }

        @Override
        public void onViewRecycled() {

        }
    }

    public static class DefaultDateHeaderViewHolder extends ViewHolder<Date>
            implements DefaultMessageViewHolder {


        protected TextView text;
        protected String dateFormat;

        protected MessagesListAdapter.DateFormatterListener dateListener;

        private MessagesListAdapter.OnDownloadListener downloadListener;

        public DefaultDateHeaderViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.NewDate);
        }

        @Override
        public void onBind(Date date) {


            if (text != null) {
                String formattedDate = null;
                if (dateListener != null) formattedDate = dateListener.onFormatDate(date);


                text.setText(formattedDate == null ? "Unknown Date" : formattedDate);

            }
        }


        @Override
        public void applyStyle(MessagesListStyle style) {
            if (text != null) {
                text.setTextColor(style.getDateHeaderTextColor());
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getDateHeaderTextSize());
                text.setTypeface(text.getTypeface(), Typeface.NORMAL);
                text.setPadding(style.getDateHeaderPadding(), style.getDateHeaderPadding(),
                        style.getDateHeaderPadding(), style.getDateHeaderPadding());
            }
            dateFormat = style.getDateHeaderFormat();
            dateFormat = dateFormat == null ? "Unknown Date" : dateFormat;
        }
    }


    public abstract static class BaseIncomingMessageViewHolder<MESSAGE extends IMessage>
            extends BaseMessageViewHolder<MESSAGE> implements DefaultMessageViewHolder {

        protected TextView time;

        protected TextView userName;
        protected ImageView userAvatar;

        @Deprecated
        public BaseIncomingMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public BaseIncomingMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            if (time != null) {
                time.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
            }

            if (userName != null) {
                if (isGroup) {
                    userName.setText(message.getUser().getName());
                } else {
                    userName.setVisibility(View.GONE);
                }
            }

            if (userAvatar != null) {
                if (isGroup) {
                    boolean isAvatarExists = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                        isAvatarExists = imageLoader != null
                                && message.getUser().getAvatar() != null
                                && !message.getUser().getAvatar().isEmpty();
                    }

                    userAvatar.setVisibility(isAvatarExists ? View.VISIBLE : View.GONE);
                    if (isAvatarExists) {
                        imageLoader.loadImage(userAvatar, message.getUser().getAvatar(), null);
                    }
                } else {
                    userAvatar.setVisibility(View.GONE);
                }

            }
        }

        @Override
        public void applyStyle(MessagesListStyle style) {
            if (time != null) {
                time.setTextColor(style.getIncomingTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getIncomingTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }

            if (userAvatar != null) {
                userAvatar.getLayoutParams().width = style.getIncomingAvatarWidth();
                userAvatar.getLayoutParams().height = style.getIncomingAvatarHeight();
            }

        }

        private void init(View itemView) {
            time = itemView.findViewById(R.id.messageTime);
            userAvatar = itemView.findViewById(R.id.messageUserAvatar);
            userName = itemView.findViewById(R.id.userName);
        }

        public abstract void onViewRecycled();
    }


    public abstract static class BaseOutcomingMessageViewHolder<MESSAGE extends IMessage>
            extends BaseMessageViewHolder<MESSAGE> implements DefaultMessageViewHolder {

        protected TextView time;

        protected ImageView status;

        @Deprecated
        public BaseOutcomingMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        public BaseOutcomingMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            if (time != null) {
                time.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
            }


            if (status != null) {

                if (Objects.equals(message.getStatus(), "Sent")) {
                    status.setBackgroundResource(R.drawable.status___sent);
                } else if (Objects.equals(message.getStatus(), "Seen")) {

                    status.setBackgroundResource(R.drawable.status_____seen);

                } else if (Objects.equals(message.getStatus(), "Sending")) {

                    status.setBackgroundResource(R.drawable.status___sending);

                } else if (Objects.equals(message.getStatus(), "Delivered")) {

                    status.setBackgroundResource(R.drawable.status___received);

                } else {

                    status.setBackgroundResource(R.drawable.status_seen);

                }
            }
        }

        @Override
        public void applyStyle(MessagesListStyle style) {
            if (time != null) {
                time.setTextColor(style.getOutcomingTimeTextColor());
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getOutcomingTimeTextSize());
                time.setTypeface(time.getTypeface(), Typeface.NORMAL);
            }
        }

        private void init(View itemView) {
            time = itemView.findViewById(R.id.messageTime);
            status = itemView.findViewById(R.id.status);
        }

        public abstract void onViewRecycled();
    }

    private static class DefaultIncomingTextMessageViewHolder
            extends IncomingTextMessageViewHolder<IMessage> {

        public DefaultIncomingTextMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultOutcomingTextMessageViewHolder
            extends OutcomingTextMessageViewHolder<MessageContentType.Image> {

        public DefaultOutcomingTextMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultIncomingImageMessageViewHolder
            extends IncomingImageMessageViewHolder<MessageContentType.Image> {

        public DefaultIncomingImageMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultIncomingVideoMessageViewHolder
            extends IncomingVideoMessageViewHolder<MessageContentType.Image> {

        public DefaultIncomingVideoMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultOutcomingImageMessageViewHolder
            extends OutcomingImageMessageViewHolder<MessageContentType.Image> {

        public DefaultOutcomingImageMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultOutGoingVideoMessageViewHolder
            extends OutGoingVideoMessageViewHolder<MessageContentType.Image> {

        public DefaultOutGoingVideoMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultOutGoingAudioMessageViewHolder
            extends OutGoingAudioMessageViewHolder<MessageContentType.Image> {

        public DefaultOutGoingAudioMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultInComingAudioMessageViewHolder
            extends InComingAudioMessageViewHolder<MessageContentType.Image> {

        public DefaultInComingAudioMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultOutGoingDocMessageViewHolder
            extends OutGoingDocMessageViewHolder<MessageContentType.Image> {

        public DefaultOutGoingDocMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }

    private static class DefaultInComingDocMessageViewHolder
            extends InComingDocMessageViewHolder<MessageContentType.Image> {

        public DefaultInComingDocMessageViewHolder(View itemView) {
            super(itemView, null);
        }
    }


    public static class DefaultOutGoingVoiceMessageViewHolder
            extends MessageHolders.BaseOutcomingMessageViewHolder<MessageContentType.Image> {

        private View bubble;
        private ImageView playButton;
        private TextView duration;
        private TextView time;


        protected ProgressBar progressBar;

        private LinearLayout waveformContainer;

        private boolean isPlaying = false;
        private MediaPlayer mediaPlayer;
        private Handler handler = new Handler();
        private int currentDuration = 0;
        private int totalDuration = 0;
        private int currentWavePosition = 0;

        public DefaultOutGoingVoiceMessageViewHolder(View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.bubble);
            playButton = itemView.findViewById(R.id.playButton);
            duration = itemView.findViewById(R.id.duration);
            time = itemView.findViewById(R.id.time);
            //messageStatus = itemView.findViewById(R.id.messageStatus);
            waveformContainer = itemView.findViewById(R.id.waveformContainer);

            Log.d("VoiceViewHolder", "Constructor called");
        }

        @Override
        public void onBind(MessageContentType.Image message) {
            super.onBind(message);

            boolean isVoiceMessage = message.getVoiceUrl() != null && !message.getVoiceUrl().isEmpty();

            if (!isVoiceMessage) {
                isVoiceMessage = message.getVoiceDuration() > 0 ||
                        (message.getImageUrl() != null && message.getImageUrl().endsWith(".mp3")) ||
                        (message.getAudioUrl() != null && (message.getAudioUrl().contains("/vn/") || message.getAudioUrl().contains("rec_")));
            }

            if (status != null) {

                if (Objects.equals(message.getStatus(), "Sent")) {
                    status.setBackgroundResource(R.drawable.status___sent);
                } else if (Objects.equals(message.getStatus(), "Seen")) {

                    status.setBackgroundResource(R.drawable.status_____seen);

                } else if (Objects.equals(message.getStatus(), "Sending")) {

                    status.setBackgroundResource(R.drawable.status___sending);

                } else if (Objects.equals(message.getStatus(), "Delivered")) {

                    status.setBackgroundResource(R.drawable.status___received);

                } else {

                    status.setBackgroundResource(R.drawable.status_seen);

                }
            }

            if (isVoiceMessage) {
                Log.d("VoiceViewHolder", "Rendering as VOICE MESSAGE");

                if (playButton != null) playButton.setVisibility(View.VISIBLE);
                if (duration != null) duration.setVisibility(View.VISIBLE);
                if (waveformContainer != null) waveformContainer.setVisibility(View.VISIBLE);

                String audioUrl = message.getVoiceUrl();
                if (audioUrl == null || audioUrl.isEmpty()) {
                    audioUrl = message.getImageUrl();
                }
                if (audioUrl == null || audioUrl.isEmpty()) {
                    audioUrl = message.getAudioUrl();
                }

                totalDuration = message.getVoiceDuration();
                if (totalDuration <= 0) {
                    totalDuration = 3000;
                }

                // Show total duration before playing
                if (duration != null) {
                    duration.setText(formatDuration(totalDuration));
                }

                if (message.getCreatedAt() != null && time != null) {
                    time.setText(formatTime(message.getCreatedAt()));
                }

                if (waveformContainer != null) {
                    generateWaveform(totalDuration);
                }

                resetPlayState();

                final String finalAudioUrl = audioUrl;
                final MessageContentType.Image finalMessage = message;

                if (playButton != null) {
                    playButton.setOnClickListener(v -> {
                        if (finalAudioUrl != null && !finalAudioUrl.isEmpty()) {
                            if (!isPlaying) {
                                startPlaying(finalAudioUrl, finalMessage);
                            } else {
                                pausePlaying();
                            }
                        }
                    });
                }

                if (progressBar != null) {
                    setMessageStatus(message);
                }

            } else {
                if (playButton != null) playButton.setVisibility(View.GONE);
                if (duration != null) duration.setVisibility(View.GONE);
                if (waveformContainer != null) waveformContainer.setVisibility(View.GONE);
            }
        }

        private void startPlaying(String audioUrl, MessageContentType.Image message) {
            isPlaying = true;
            currentDuration = 0;
            currentWavePosition = 0;

            // Animate play button scaling
            if (playButton != null) {
                playButton.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(150)
                        .withEndAction(() -> playButton.setImageResource(R.drawable.baseline_pause_24))
                        .start();
            }

            // Notify listener to start playing
            if (audioPlayListener != null) {
                audioPlayListener.onAudioPlayClick(
                        audioUrl,
                        playButton,
                        duration,
                        null,
                        message
                );
            }

            // Update duration every 100ms
            updateDurationRunnable();
        }

        private void updateDurationRunnable() {
            if (isPlaying && duration != null) {
                currentDuration += 100;
                duration.setText(formatDuration(currentDuration));

                // Update waveform animation
                updateWaveformProgress();

                if (currentDuration <= totalDuration) {
                    handler.postDelayed(this::updateDurationRunnable, 100);
                } else {
                    finishPlaying();
                }
            }
        }

        private void updateWaveformProgress() {
            if (waveformContainer == null) return;

            float progress = (float) currentDuration / totalDuration;
            int barCount = waveformContainer.getChildCount();
            int progressBar = (int) (barCount * progress);

            for (int i = 0; i < barCount; i++) {
                View bar = waveformContainer.getChildAt(i);
                if (bar != null) {
                    if (i < progressBar) {
                        // Highlight bars that have been played
                        bar.setAlpha(1.0f);
                        bar.setScaleY(1.15f);
                    } else {
                        // Dim bars that haven't been played yet
                        bar.setAlpha(0.4f);
                        bar.setScaleY(1.0f);
                    }
                }
            }
        }

        private void pausePlaying() {
            isPlaying = false;
            handler.removeCallbacksAndMessages(null);

            if (playButton != null) {
                playButton.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .withEndAction(() -> playButton.setImageResource(R.drawable.baseline_play_arrow_24))
                        .start();
            }
        }

        private void finishPlaying() {
            isPlaying = false;
            handler.removeCallbacksAndMessages(null);
            resetPlayState();
        }

        private void resetPlayState() {
            isPlaying = false;
            currentDuration = 0;
            currentWavePosition = 0;

            if (playButton != null) {
                playButton.setScaleX(1.0f);
                playButton.setScaleY(1.0f);
                playButton.setImageResource(R.drawable.baseline_play_arrow_24);
            }

            // Reset duration to show total duration again
            if (duration != null) {
                duration.setText(formatDuration(totalDuration));
            }

            // Reset waveform
            if (waveformContainer != null) {
                for (int i = 0; i < waveformContainer.getChildCount(); i++) {
                    View bar = waveformContainer.getChildAt(i);
                    if (bar != null) {
                        bar.setAlpha(0.8f);
                        bar.setScaleY(1.0f);
                    }
                }
            }
        }

        private void setMessageStatus(MessageContentType.Image message) {
            if (progressBar != null) {
                progressBar = itemView.findViewById(R.id.fileSendProgress);
            }
        }

        // Updated generateWaveform for DefaultOutGoingVoiceMessageViewHolder
        private void generateWaveform(int durationMillis) {
            waveformContainer.removeAllViews();

            int seconds = Math.max(durationMillis / 1000, 1);
            // More bars for WhatsApp-like density
            int barCount = Math.min(Math.max(seconds * 8, 50), 90);

            // WhatsApp-style thin bars
            int barWidth = dpToPx(2);
            int barSpacing = dpToPx(2);
            int maxHeight = dpToPx(20);
            int minHeight = dpToPx(4);

            for (int i = 0; i < barCount; i++) {
                View bar = new View(waveformContainer.getContext());

                // Create more natural waveform pattern
                double progress = (double) i / barCount;
                double wave1 = Math.sin(progress * Math.PI * 3) * 0.4;
                double wave2 = Math.sin(progress * Math.PI * 7) * 0.3;
                double randomness = Math.random() * 0.3;
                double combinedWave = Math.abs(wave1 + wave2 + randomness);

                int height = minHeight + (int)((maxHeight - minHeight) * combinedWave);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(barWidth, height);
                params.setMarginEnd(barSpacing);

                bar.setLayoutParams(params);
                bar.setBackgroundColor(Color.WHITE);
                bar.setAlpha(0.8f);

                // Rounded corners for bars
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setColor(Color.WHITE);
                shape.setCornerRadius(dpToPx(1));
                bar.setBackground(shape);
                bar.setAlpha(0.8f);

                waveformContainer.addView(bar);
            }
        }


        private int dpToPx(int dp) {
            float density = waveformContainer.getContext().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }

        private String formatDuration(int millis) {
            int seconds = millis / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }

        private String formatTime(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(date);
        }

        @Override
        public void onViewRecycled() {
            if (isPlaying) {
                pausePlaying();
            }
            handler.removeCallbacksAndMessages(null);
        }
    }

    public static class DefaultInComingVoiceMessageViewHolder
            extends MessageHolders.BaseIncomingMessageViewHolder<MessageContentType.Image> {

        private View bubble;
        private ImageView playButton;
        private TextView duration;
        private TextView time;
        private LinearLayout waveformContainer;

        private boolean isPlaying = false;
        private Handler handler = new Handler();
        private int currentDuration = 0;
        private int totalDuration = 0;

        public DefaultInComingVoiceMessageViewHolder(View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.bubble);
            playButton = itemView.findViewById(R.id.playButton);
            duration = itemView.findViewById(R.id.duration);
            time = itemView.findViewById(R.id.time);
            waveformContainer = itemView.findViewById(R.id.waveformContainer);

            Log.d("VoiceViewHolder", "INCOMING Constructor called");
        }

        @Override
        public void onBind(MessageContentType.Image message) {
            super.onBind(message);

            boolean isVoiceMessage = message.getVoiceUrl() != null && !message.getVoiceUrl().isEmpty();

            if (!isVoiceMessage) {
                isVoiceMessage = message.getVoiceDuration() > 0 ||
                        (message.getImageUrl() != null && message.getImageUrl().endsWith(".mp3")) ||
                        (message.getAudioUrl() != null && (message.getAudioUrl().contains("/vn/") || message.getAudioUrl().contains("rec_")));
            }

            if (isVoiceMessage) {
                Log.d("VoiceViewHolder", "✅ Rendering as VOICE MESSAGE");

                if (playButton != null) playButton.setVisibility(View.VISIBLE);
                if (duration != null) duration.setVisibility(View.VISIBLE);
                if (waveformContainer != null) waveformContainer.setVisibility(View.VISIBLE);

                String audioUrl = message.getVoiceUrl();
                if (audioUrl == null || audioUrl.isEmpty()) {
                    audioUrl = message.getImageUrl();
                }
                if (audioUrl == null || audioUrl.isEmpty()) {
                    audioUrl = message.getAudioUrl();
                }

                totalDuration = message.getVoiceDuration();
                if (totalDuration <= 0) {
                    totalDuration = 3000;
                }

                // Show total duration before playing
                if (duration != null) {
                    duration.setText(formatDuration(totalDuration));
                }

                if (message.getCreatedAt() != null && time != null) {
                    time.setText(formatTime(message.getCreatedAt()));
                }

                if (waveformContainer != null) {
                    generateWaveform(totalDuration);
                }

                resetPlayState();

                final String finalAudioUrl = audioUrl;
                final MessageContentType.Image finalMessage = message;

                if (playButton != null) {
                    playButton.setOnClickListener(v -> {
                        if (finalAudioUrl != null && !finalAudioUrl.isEmpty()) {
                            if (!isPlaying) {
                                startPlaying(finalAudioUrl, finalMessage);
                            } else {
                                pausePlaying();
                            }
                        }
                    });
                }

            } else {
                if (playButton != null) playButton.setVisibility(View.GONE);
                if (duration != null) duration.setVisibility(View.GONE);
                if (waveformContainer != null) waveformContainer.setVisibility(View.GONE);
            }
        }

        private void startPlaying(String audioUrl, MessageContentType.Image message) {
            isPlaying = true;
            currentDuration = 0;

            if (playButton != null) {
                playButton.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(150)
                        .withEndAction(() -> playButton.setImageResource(R.drawable.baseline_pause_24))
                        .start();
            }

            if (audioPlayListener != null) {
                audioPlayListener.onAudioPlayClick(
                        audioUrl,
                        playButton,
                        duration,
                        null,
                        message
                );
            }

            updateDurationRunnable();
        }

        private void updateDurationRunnable() {
            if (isPlaying && duration != null) {
                currentDuration += 100;
                duration.setText(formatDuration(currentDuration));

                updateWaveformProgress();

                if (currentDuration <= totalDuration) {
                    handler.postDelayed(this::updateDurationRunnable, 100);
                } else {
                    finishPlaying();
                }
            }
        }

        private void updateWaveformProgress() {
            if (waveformContainer == null) return;

            float progress = (float) currentDuration / totalDuration;
            int barCount = waveformContainer.getChildCount();
            int progressBar = (int) (barCount * progress);

            for (int i = 0; i < barCount; i++) {
                View bar = waveformContainer.getChildAt(i);
                if (bar != null) {
                    if (i < progressBar) {
                        bar.setAlpha(1.0f);
                        bar.setScaleY(1.15f);
                    } else {
                        bar.setAlpha(0.4f);
                        bar.setScaleY(1.0f);
                    }
                }
            }
        }

        private void pausePlaying() {
            isPlaying = false;
            handler.removeCallbacksAndMessages(null);

            if (playButton != null) {
                playButton.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .withEndAction(() -> playButton.setImageResource(R.drawable.baseline_play_arrow_24))
                        .start();
            }
        }

        private void finishPlaying() {
            isPlaying = false;
            handler.removeCallbacksAndMessages(null);
            resetPlayState();
        }

        private void resetPlayState() {
            isPlaying = false;
            currentDuration = 0;

            if (playButton != null) {
                playButton.setScaleX(1.0f);
                playButton.setScaleY(1.0f);
                playButton.setImageResource(R.drawable.baseline_play_arrow_24);
            }

            // Reset duration to show total duration again
            if (duration != null) {
                duration.setText(formatDuration(totalDuration));
            }

            if (waveformContainer != null) {
                for (int i = 0; i < waveformContainer.getChildCount(); i++) {
                    View bar = waveformContainer.getChildAt(i);
                    if (bar != null) {
                        bar.setAlpha(0.7f);
                        bar.setScaleY(1.0f);
                    }
                }
            }
        }

        // Updated generateWaveform for DefaultInComingVoiceMessageViewHolder
        private void generateWaveform(int durationMillis) {
            waveformContainer.removeAllViews();

            int seconds = Math.max(durationMillis / 1000, 1);
            // More bars for WhatsApp-like density
            int barCount = Math.min(Math.max(seconds * 8, 50), 90);

            // WhatsApp-style thin bars
            int barWidth = dpToPx(2);
            int barSpacing = dpToPx(2);
            int maxHeight = dpToPx(20);
            int minHeight = dpToPx(4);

            for (int i = 0; i < barCount; i++) {
                View bar = new View(waveformContainer.getContext());

                // Create more natural waveform pattern
                double progress = (double) i / barCount;
                double wave1 = Math.sin(progress * Math.PI * 3) * 0.4;
                double wave2 = Math.sin(progress * Math.PI * 7) * 0.3;
                double randomness = Math.random() * 0.3;
                double combinedWave = Math.abs(wave1 + wave2 + randomness);

                int height = minHeight + (int)((maxHeight - minHeight) * combinedWave);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(barWidth, height);
                params.setMarginEnd(barSpacing);

                bar.setLayoutParams(params);

                // Rounded corners for bars
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setColor(Color.parseColor("#666666"));
                shape.setCornerRadius(dpToPx(1));
                bar.setBackground(shape);
                bar.setAlpha(0.7f);

                waveformContainer.addView(bar);
            }
        }

        private int dpToPx(int dp) {
            float density = waveformContainer.getContext().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }

        private String formatDuration(int millis) {
            int seconds = millis / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }

        private String formatTime(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(date);
        }

        @Override
        public void onViewRecycled() {
            if (isPlaying) {
                pausePlaying();
            }
            handler.removeCallbacksAndMessages(null);
        }
    }





}
