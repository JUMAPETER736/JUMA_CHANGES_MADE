package com.uyscuti.social.chatsuit.messages;

/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/




import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;


import com.uyscuti.social.chatsuit.R;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.facebook.FacebookEmojiProvider;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import java.lang.reflect.Field;

/**
 * Component for input outcoming messages
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CommentsInput extends RelativeLayout


        implements View.OnClickListener, TextWatcher, View.OnFocusChangeListener {

    protected EmojiEditText messageInput;
    protected ImageButton messageSendButton;
    protected ImageButton attachmentButton;

    protected ImageButton voiceMessageButton;
    protected AppCompatButton gifButton;

    protected boolean isVnRecording;

//    protected Context con;

    MessageInputStyle style;

    protected ImageButton emojiButton;

    protected boolean emojiShowing;
    protected Space sendButtonSpace, attachmentButtonSpace;

    private CharSequence input;
    private InputListener inputListener;

    private VoiceListener voiceListener;
    private GifListener gifListener;

    private EmojiListener emojiListener;

    private AttachmentsListener attachmentsListener;
    private boolean isTyping;
    private TypingListener typingListener;
    private int delayTypingStatusMillis;
    private final Runnable typingTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTyping) {
                isTyping = false;
                if (typingListener != null) typingListener.onStopTyping();
            }
        }
    };
    private boolean lastFocus;

    public CommentsInput(Context context) {
        super(context);

//        con = context;
        init(context);
    }

    public CommentsInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CommentsInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Sets callback for 'submit' button.
     *
     * @param inputListener input callback
     */
    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    /**
     * Sets callback for 'add' button.
     *
     * @param attachmentsListener input callback
     */
    public void setAttachmentsListener(AttachmentsListener attachmentsListener) {
        this.attachmentsListener = attachmentsListener;
    }

    public void setVoiceListener(VoiceListener voiceListener) {
        this.voiceListener = voiceListener;
    }

    public void setEmojiListener(EmojiListener emojiListener) {
        this.emojiListener = emojiListener;
    }

    public void setGifListener(GifListener gifListener) {
        this.gifListener = gifListener;
    }
    /**
     * Returns EditText for messages input
     *
     * @return EditText
     */
    public EmojiEditText getInputEditText() {
        return messageInput;
    }
    public AppCompatButton getGifButton() {
        return gifButton;
    }

    public ImageButton getEmojiButton(){
        return emojiButton;
    }

    /**
     * Returns `submit` button
     *
     * @return ImageButton
     */
    public ImageButton getButton() {
        return messageSendButton;
    }

    @Override
    public void onClick(View view) {

        Log.d("MessageInput", "Clicked");
        int id = view.getId();

        Log.d("MessageInput", "onClick: " + id);
        if (id == R.id.messageSendButton) {
            boolean isSubmitted = onSubmit();
            if (isSubmitted) {
                messageInput.setText("");
            }
            removeCallbacks(typingTimerRunnable);
            post(typingTimerRunnable);
        } else if (id == R.id.attachmentButton) {
            onAddAttachments();
        } else if (id == R.id.voiceMessageButton) {
            onAddVoiceNote();
            voiceMessageButton.setImageDrawable(style.getVoiceMessageButtonIcon());


        } else if (id == R.id.messageEmoji) {
            onAddEmoji();
            if (!emojiShowing){
                emojiButton.setImageDrawable(style.getKeyBoardButtonIcon());
                emojiShowing = true;
            } else {
                emojiButton.setImageDrawable(style.getEmojiButtonIcon());
                emojiShowing = false;
            }
        }else if(id == R.id.gifButton){
            onAddGif();
        }
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start have just replaced old text that had length before
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {

        input = s;
        messageSendButton.setEnabled(input.length() > 0);
        voiceMessageButton.setEnabled(input.length() == 0);

        if (input.length() > 0) {
            voiceMessageButton.setVisibility(GONE);
            messageSendButton.setVisibility(VISIBLE);
        } else {
            messageSendButton.setVisibility(GONE);
            voiceMessageButton.setVisibility(VISIBLE);
        }
        if (s.length() > 0) {
            if (!isTyping) {
                isTyping = true;
                if (typingListener != null) typingListener.onStartTyping();
            }
            removeCallbacks(typingTimerRunnable);
            postDelayed(typingTimerRunnable, delayTypingStatusMillis);
        }
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start are about to be replaced by new text with length after.
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //do nothing
    }

    /**
     * This method is called to notify you that, somewhere within s, the text has been changed.
     */
    @Override
    public void afterTextChanged(Editable editable) {
        //do nothing
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (lastFocus && !hasFocus && typingListener != null) {
            typingListener.onStopTyping();
        }
        lastFocus = hasFocus;
    }

    private boolean onSubmit() {
        return inputListener != null && inputListener.onSubmit(input);
    }

    private void onAddAttachments() {
//        Log.d("Attachment", "Attachment Listener");
        if (attachmentsListener != null) attachmentsListener.onAddAttachments();
    }

    private void onAddVoiceNote() {
        if (voiceListener != null) voiceListener.onAddVoiceNote();
    }

    private void onAddGif() {
        if (gifListener != null) gifListener.onAddGif();
    }
    private void onAddEmoji() {
        if (emojiListener != null) emojiListener.onAddEmoji();
    }

    private void init(Context context, AttributeSet attrs) {
        init(context);
        style = MessageInputStyle.parse(context, attrs);

        this.messageInput.setMaxLines(style.getInputMaxLines());
        this.messageInput.setHint(style.getInputHint());
        this.messageInput.setText(style.getInputText());
        this.messageInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getInputTextSize());
        this.messageInput.setTextColor(style.getInputTextColor());
        this.messageInput.setHintTextColor(style.getInputHintColor());
        ViewCompat.setBackground(this.messageInput, style.getInputBackground());
        setCursor(style.getInputCursorDrawable());

        this.attachmentButton.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButton.setImageDrawable(style.getAttachmentButtonIcon());
        this.attachmentButton.getLayoutParams().width = style.getAttachmentButtonWidth();
        this.attachmentButton.getLayoutParams().height = style.getAttachmentButtonHeight();
        ViewCompat.setBackground(this.attachmentButton, style.getInputButtonBackground());

        this.attachmentButtonSpace.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButtonSpace.getLayoutParams().width = style.getAttachmentButtonMargin();

        this.voiceMessageButton.setImageDrawable(style.getVoiceMessageButtonIcon());
        this.voiceMessageButton.getLayoutParams().width = style.getInputButtonWidth();
        this.voiceMessageButton.getLayoutParams().height = style.getInputButtonHeight();
        voiceMessageButton.setEnabled(true);
        ViewCompat.setBackground(this.voiceMessageButton, style.getInputButtonBackground());


        this.gifButton.getLayoutParams().width = style.getGifButtonWidth();
        this.gifButton.getLayoutParams().height = style.getGifButtonHeight();
        ViewCompat.setBackground(this.gifButton, style.getInputButtonBackground());


        this.emojiButton.setImageDrawable(style.getEmojiButtonIcon());
        this.emojiButton.getLayoutParams().width = style.getAttachmentButtonWidth();
        this.emojiButton.getLayoutParams().height = style.getAttachmentButtonHeight();
        ViewCompat.setBackground(this.emojiButton, style.getInputButtonBackground());


//        this.messageSendButton.setVisibility(GONE);
        this.messageSendButton.setImageDrawable(style.getInputButtonIcon());
        this.messageSendButton.getLayoutParams().width = style.getInputButtonWidth();
        this.messageSendButton.getLayoutParams().height = style.getInputButtonHeight();
        ViewCompat.setBackground(messageSendButton, style.getInputButtonBackground());
        this.sendButtonSpace.getLayoutParams().width = style.getInputButtonMargin();

//        this.gifButton.setImag
        if (getPaddingLeft() == 0
                && getPaddingRight() == 0
                && getPaddingTop() == 0
                && getPaddingBottom() == 0) {
            setPadding(
                    style.getInputDefaultPaddingLeft(),
                    style.getInputDefaultPaddingTop(),
                    style.getInputDefaultPaddingRight(),
                    style.getInputDefaultPaddingBottom()
            );
        }
        this.delayTypingStatusMillis = style.getDelayTypingStatus();
    }

    private void installEmoji(){
        EmojiManager.install(new TwitterEmojiProvider());
    }

    private void init(Context context) {

        installEmoji();
        inflate(context, R.layout.view_comment_input, this);

        messageInput = findViewById(R.id.messageInput);
        messageSendButton = findViewById(R.id.messageSendButton);
        attachmentButton = findViewById(R.id.attachmentButton);
        sendButtonSpace = findViewById(R.id.sendButtonSpace);
        attachmentButtonSpace = findViewById(R.id.attachmentButtonSpace);
        emojiButton = findViewById(R.id.messageEmoji);
        emojiShowing = false;
        voiceMessageButton = findViewById(R.id.voiceMessageButton);


        gifButton = findViewById(R.id.gifButton);

        messageSendButton.setOnClickListener(this);
        attachmentButton.setOnClickListener(this);
        voiceMessageButton.setOnClickListener(this);
        emojiButton.setOnClickListener(this);
        messageInput.addTextChangedListener(this);
        messageInput.setText("");
        messageInput.setOnFocusChangeListener(this);
        gifButton.setOnClickListener(this);


    }

    private void setCursor(Drawable drawable) {
        if (drawable == null) return;

        try {
            @SuppressLint("PrivateApi") final Field drawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
            drawableResField.setAccessible(true);

            final Object drawableFieldOwner;
            final Class<?> drawableFieldClass;
            @SuppressLint("PrivateApi") final Field editorField = TextView.class.getDeclaredField("mEditor");
            editorField.setAccessible(true);
            drawableFieldOwner = editorField.get(this.messageInput);
            assert drawableFieldOwner != null;
            drawableFieldClass = drawableFieldOwner.getClass();
            final Field drawableField = drawableFieldClass.getDeclaredField("mCursorDrawable");
            drawableField.setAccessible(true);
            drawableField.set(drawableFieldOwner, new Drawable[]{drawable, drawable});
        } catch (Exception ignored) {
        }
    }

    public void setTypingListener(TypingListener typingListener) {
        this.typingListener = typingListener;
    }

    /**
     * Interface definition for a callback to be invoked when user pressed 'submit' button
     */
    public interface InputListener {

        /**
         * Fires when user presses 'send' button.
         *
         * @param input input entered by user
         * @return if input text is valid, you must return {@code true} and input will be cleared, otherwise return false.
         */
        boolean onSubmit(CharSequence input);
    }

    public interface VoiceListener {
        void onAddVoiceNote();
    }

    public interface GifListener {
        void onAddGif();
    }

    public interface EmojiListener {
        void onAddEmoji();
    }

    /**
     * Interface definition for a callback to be invoked when user presses 'add' button
     */
    public interface AttachmentsListener {

        /**
         * Fires when user presses 'add' button.
         */
        void onAddAttachments();
    }

    /**
     * Interface definition for a callback to be invoked when user typing
     */
    public interface TypingListener {

        /**
         * Fires when user presses start typing
         */
        void onStartTyping();

        /**
         * Fires when user presses stop typing
         */
        void onStopTyping();
    }
}
