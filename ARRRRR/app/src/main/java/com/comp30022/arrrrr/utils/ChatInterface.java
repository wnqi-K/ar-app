package com.comp30022.arrrrr.utils;

import com.comp30022.arrrrr.models.Chat;

/**
 * Interfaces used to listen to the event of sending/getting
 * messages that succeeds or fails
 *
 * @author Zijie Shen
 */

public class ChatInterface {

    public interface Listener {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);

        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);
    }
}
