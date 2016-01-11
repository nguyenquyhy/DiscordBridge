package com.nguyenquyhy.spongediscord.discord.handle;

/**
 * Created by Hy on 1/11/2016.
 */
public interface IDispatcher {
    /**
     * Unregisters a listener, so the listener will no longer receive events.
     *
     * @param listener Listener to unregister
     */
    void unregisterListener(Object listener);

    /**
     * Registers an IListener to receive events.
     * @param listener Listener to register
     */
    void registerListener(Object listener);

    /**
     * Sends an IEvent to all listeners that listen for that specific event.
     * @param IEvent Event to dispatch.
     */
    void dispatch(IEvent IEvent);
}
