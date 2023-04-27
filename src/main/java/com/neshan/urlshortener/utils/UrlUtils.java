package com.neshan.urlshortener.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.net.URISyntaxException;
import java.util.Map;

@Slf4j
public class UrlUtils {
    public static String addQueryParam(String baseUrl, Map<String, String> queryParams) {
        try {
            return new URIBuilder(baseUrl)
                    .addParameters(
                            queryParams.entrySet().stream()
                                    .map(
                                            e ->
                                                    (NameValuePair)
                                                            new BasicNameValuePair(e.getKey(), e.getValue()))
                                    .toList())
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            return baseUrl;
        }
    }
}
