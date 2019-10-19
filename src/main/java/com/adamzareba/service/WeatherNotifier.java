package com.adamzareba.service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class WeatherNotifier implements Notifier {

    private static final String DESCRIPTION = "Weather Notifier displays nice messages";

    private final String owner;

    public static String repeatDescription(int count) {
        return DESCRIPTION.repeat(count);
    }

    @Override
    public String getMessage() {
        return "Weather Notifier!";
    }

    @Override
    public String getPrint(final String message) {
        return "Hi " + owner + "! This is " + message;
    }
}
