package com.nguyenquyhy.spongediscord.discord.handle.impl;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.discord.handle.IDispatcher;
import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.IListener;
import net.jodah.typetools.TypeResolver;

import java.util.*;

/**
 * Created by Hy on 1/11/2016.
 */
public class EventDispatcher implements IDispatcher {
    // holy generics, batman!
    private Map<Class<? extends IEvent>, List<IListener>> listenerMap = new HashMap<>();

    /**
     * Unregisters a listener, so the listener will no longer receive events.
     *
     * @param listener Listener to unregister
     */
    @Override
    public void unregisterListener(Object listener) {
        if(listener instanceof IListener)
            for (Map.Entry<Class<? extends IEvent>, List<IListener>> entry : listenerMap.entrySet()) {
                entry.getValue().stream().filter(listener1 -> listener1.equals(listener)).forEach(listener1 -> entry.getValue().remove(listener1));
            }
    }

    /**
     * Registers an IListener to receive events.
     * @param listener Listener to register
     */
    @Override
    public void registerListener(Object listener) {
        if(listener instanceof IListener) {
            Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
            if (IEvent.class.isAssignableFrom(rawType)) {
                Class<? extends IEvent> eventType = (Class<? extends IEvent>) rawType;
                if (listenerMap.containsKey(eventType)) {
                    listenerMap.get(eventType).add((IListener) listener);
                } else {
                    listenerMap.put(eventType, new ArrayList<>(Collections.singletonList((IListener) listener)));
                }
                SpongeDiscord.getInstance().getLogger().debug("Registered IListener for {}. Map size now {}.", eventType.getSimpleName(), listenerMap.size());
            }
        }
    }

    /**
     * Sends an IEvent to all listeners that listen for that specific event.
     * @param event The event to dispatch.
     */
    @Override
    public void dispatch(IEvent event) {
        Class<? extends IEvent> eventType = event.getClass();
        SpongeDiscord.getInstance().getLogger().debug("Dispatching event of type {}.", eventType.getSimpleName());
        if (listenerMap.containsKey(eventType)) {
            for (IListener listener : listenerMap.get(eventType)) {
                listener.receive(event);
            }
        }
    }
}
