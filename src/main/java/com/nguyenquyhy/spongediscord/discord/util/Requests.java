package com.nguyenquyhy.spongediscord.discord.util;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Hy on 1/11/2016.
 */
public enum Requests {
    POST(HttpPost.class),
    GET(HttpGet.class),
    DELETE(HttpDelete.class),
    PATCH(HttpPatch.class);

    static final HttpClient CLIENT = HttpClients.createDefault();

    final Class<? extends HttpUriRequest> requestClass;

    Requests(Class<? extends HttpUriRequest> clazz) {
        this.requestClass = clazz;
    }

    public Class<? extends HttpUriRequest> getRequestClass() {
        return requestClass;
    }

    public String makeRequest(String url, BasicNameValuePair... headers) throws HttpException {
        try {
            HttpUriRequest request = this.requestClass.getConstructor(String.class).newInstance(url);
            for (BasicNameValuePair header : headers) {
                request.addHeader(header.getName(), header.getValue());
            }
            HttpResponse response = CLIENT.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 204) { //There is a no content response when deleting messages
                return null;
            }
            handleResponseCode(url, response, responseCode);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String makeRequest(String url, HttpEntity entity, BasicNameValuePair... headers) throws HttpException {
        try {
            if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(this.requestClass)) {
                HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase)
                        this.requestClass.getConstructor(String.class).newInstance(url);
                for (BasicNameValuePair header : headers) {
                    request.addHeader(header.getName(), header.getValue());
                }
                request.setEntity(entity);
                HttpResponse response = CLIENT.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 204) { //There is a no content response when deleting messages
                    return null;
                }
                handleResponseCode(url, response, responseCode);
                return EntityUtils.toString(response.getEntity());
            } else {
                SpongeDiscord.getInstance().getLogger().error("Tried to attach HTTP entity to invalid type! ({})",
                        this.requestClass.getSimpleName());
            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleResponseCode(String url, HttpResponse response, int responseCode) throws HttpException, IOException {
        if (responseCode == 404) {
            SpongeDiscord.getInstance().getLogger().error("Received 404 error, please notify the developer and include the URL ({})", url);
            throw new HttpException("Not Found", url, 404);
        } else if (responseCode == 403) {
            throw new HttpException("Forbidden", url, 403, EntityUtils.toString(response.getEntity()));
        } else if (responseCode == 400) {
            throw new HttpException("Bad Request", url, 400, EntityUtils.toString(response.getEntity()));
        } else if (responseCode >= 400) {
            throw new HttpException("HTTP Error", url, responseCode, EntityUtils.toString(response.getEntity()));
        }
    }
}
