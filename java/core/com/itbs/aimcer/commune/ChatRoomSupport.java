package com.itbs.aimcer.commune;

/**
 * Methods which are required to be implemented for Group Chat support.
 *
 * @author Alex Rass
 * @since Oct 10, 2005
 */
public interface ChatRoomSupport extends Connection {
    void join(String room, String nickname, ChatRoomEventListener listener);
    void create(String room, String nickname, ChatRoomEventListener listener);
    void sendChatMessage(String message);
    boolean isJoined();
}
