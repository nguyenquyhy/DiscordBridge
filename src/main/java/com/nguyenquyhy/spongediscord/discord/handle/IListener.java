package com.nguyenquyhy.spongediscord.discord.handle;

/**
 * Created by Hy on 1/11/2016.
 */
public interface IListener<I extends IEvent> {
    /**
     * Receives an event when it is dispatched
     *
     * @param event The event that was received
     */
    void receive(I event);
}
