package com.example.lostandfounditems.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours / 24;
        long weeks   = days / 7;
        long months  = days / 30;

        if (seconds < 60)   return "Just now";
        if (minutes < 60)   return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        if (hours < 24)     return hours   + (hours   == 1 ? " hour ago"   : " hours ago");
        if (days < 7)       return days    + (days    == 1 ? " day ago"    : " days ago");
        if (weeks < 5)      return weeks   + (weeks   == 1 ? " week ago"   : " weeks ago");
        return months + (months == 1 ? " month ago" : " months ago");
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
