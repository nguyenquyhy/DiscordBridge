package com.nguyenquyhy.discordbridge.models;

/**
 * Created by Hy on 12/11/2016.
 */
public interface IConfigInheritable<T> {
    void inherit(T parent);
}
