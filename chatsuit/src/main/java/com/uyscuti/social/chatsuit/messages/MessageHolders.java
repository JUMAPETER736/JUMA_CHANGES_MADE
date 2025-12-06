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



// ==================================================================================
// VIEW TYPE CONSTANTS & HOLDER FACTORY
// ==================================================================================

    protected ViewHolder getHolder(ViewGroup parent, int viewType, MessagesListStyle messagesListStyle) {
        switch (viewType) {
            // Date header
            case VIEW_TYPE_DATE_HEADER:
                return getHolder(parent, dateHeaderLayout, dateHeaderHolder, messagesListStyle, null);

            // Incoming message types
            case VIEW_TYPE_TEXT_MESSAGE:
                return getHolder(parent, incomingTextConfig, messagesListStyle);
            case VIEW_TYPE_IMAGE_MESSAGE:
                return getHolder(parent, incomingImageConfig, messagesListStyle);
            case VIEW_TYPE_VIDEO_MESSAGE:
                return getHolder(parent, inComingVideoConfig, messagesListStyle);
            case VIEW_TYPE_AUDIO_MESSAGE:
                return getHolder(parent, inComingAudioConfig, messagesListStyle);
            case VIEW_TYPE_VOICE_MESSAGE:
                return getHolder(parent, inComingVoiceConfig, messagesListStyle);
            case VIEW_TYPE_DOCUMENT_MESSAGE:
                return getHolder(parent, inComingDocConfig, messagesListStyle);

            // Outgoing message types (negative view types)
            case -VIEW_TYPE_TEXT_MESSAGE:
                return getHolder(parent, outcomingTextConfig, messagesListStyle);
            case -VIEW_TYPE_IMAGE_MESSAGE:
                return getHolder(parent, outcomingImageConfig, messagesListStyle);
            case -VIEW_TYPE_VIDEO_MESSAGE:
                return getHolder(parent, outGoingVideoConfig, messagesListStyle);
            case -VIEW_TYPE_AUDIO_MESSAGE:
                return getHolder(parent, outGoingAudioConfig, messagesListStyle);
            case -VIEW_TYPE_VOICE_MESSAGE:
                return getHolder(parent, outGoingVoiceConfig, messagesListStyle);
            case -VIEW_TYPE_DOCUMENT_MESSAGE:
                return getHolder(parent, outGoingDocConfig, messagesListStyle);
        }
        throw new IllegalStateException("Wrong message view type...");
    }

// ==================================================================================
// BASE MESSAGE VIEW HOLDERS
// ==================================================================================

    /**
     * Base class for all message view holders
     * Contains common properties and methods shared by incoming and outgoing messages
     */
    public static abstract class BaseMessageViewHolder<MESSAGE extends IMessage> extends ViewHolder<MESSAGE> {
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

        /**
         * Configures link behavior for TextViews to work with selection mode
         */
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

    /**
     * Base class for all incoming message view holders
     * Handles common incoming message UI elements (time, avatar, username)
     */
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
            // Set message time
            if (time != null) {
                time.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
            }

            // Set username for group chats
            if (userName != null) {
                if (isGroup) {
                    userName.setText(message.getUser().getName());
                } else {
                    userName.setVisibility(View.GONE);
                }
            }

            // Set user avatar for group chats
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

    /**
     * Base class for all outgoing message view holders
     * Handles common outgoing message UI elements (time, status indicator)
     */
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
            // Set message time
            if (time != null) {
                time.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
            }

            // Set message status icon
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

// ==================================================================================
// INCOMING MESSAGE HOLDERS
// ==================================================================================

    /**
     * Incoming Text Message Holder
     * Displays text messages received from other users
     */
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
            // Cleanup if needed
        }
    }

    /**
     * Incoming Image Message Holder
     * Displays image messages with download functionality
     */
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

            // Load image
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

            // Handle download functionality
            if (download != null) {
                String imageUrl = message.getImageUrl();
                assert imageUrl != null;

                if (imageUrl.startsWith("/storage/") || imageUrl.startsWith("file:/")) {
                    // Local file - hide download button
                    File imageFile = new File(imageUrl);
                    if (imageFile.isFile()) {
                        download.setVisibility(View.GONE);
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
                    }
                } else {
                    // Remote URL - show download button
                    download.setVisibility(View.VISIBLE);
                    down.setVisibility(View.VISIBLE);

                    getFileSizeFromUrl(imageUrl, new OnFileSizeReceivedListener() {
                        @Override
                        public void onFileSizeReceived(long fileSize) {
                            String formattedSize = formatFileSize(fileSize);
                            imgSize.post(new Runnable() {
                                @Override
                                public void run() {
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
            // Cleanup if needed
        }
    }

    /**
     * Incoming Video Message Holder
     * Displays video messages with download and play functionality
     */
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

            // Load video thumbnail
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

            // Handle download and play functionality
            if (down != null) {
                String imageUrl = message.getVideoUrl();
                assert imageUrl != null;

                if (imageUrl.startsWith("/storage/") || imageUrl.startsWith("file://")) {
                    // Local file
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
                                if (adapter.getSelectedItemsCount() == 0) {
                                    mediaClickListener.onMediaClick(message.getVideoUrl(), v, message);
                                } else {
                                    itemView.performClick();
                                }
                            }
                        });
                    }
                } else {
                    // Remote URL
                    down.setVisibility(View.VISIBLE);
                    download.setVisibility(View.VISIBLE);

                    getFileSizeFromUrl(imageUrl, new OnFileSizeReceivedListener() {
                        @Override
                        public void onFileSizeReceived(long fileSize) {
                            String formattedSize = formatFileSize(fileSize);
                            vidSize.post(new Runnable() {
                                @Override
                                public void run() {
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
            // Cleanup if needed
        }
    }

    /**
     * Incoming Audio Message Holder
     * Displays audio messages with playback controls
     */
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
                bubble.setPadding(4, 4, 4, 4);
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
            bubble = itemView.findViewById(R.id.bubble);
        }

        @Override
        public void onViewRecycled() {
            // Cleanup if needed
        }
    }

    /**
     * Incoming Voice Message Holder
     * Displays voice messages with waveform visualization
     */
  
}
