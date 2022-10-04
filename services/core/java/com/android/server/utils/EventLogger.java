/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.utils;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.util.Log;

import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;

/**
 * Logs human-readable events for debugging purposes.
 */
public class EventLogger {

    /** Identifies the source of events. */
    private final String mTag;

    /** Stores the events using a ring buffer. */
    private final ArrayDeque<Event> mEvents;

    /**
     * The maximum number of events to keep in {@code mEvents}.
     *
     * <p>Calling {@link #log} when the size of {@link #mEvents} matches the threshold will
     * cause the oldest event to be evicted.
     */
    private final int mMemSize;

    /**
     * Constructor for logger.
     * @param size the maximum number of events to keep in log
     * @param tag the string displayed before the recorded log
     */
    public EventLogger(int size, String tag) {
        mEvents = new ArrayDeque<>(size);
        mMemSize = size;
        mTag = tag;
    }

    /** Enqueues {@code event} to be logged. */
    public synchronized void log(Event event) {
        if (mEvents.size() >= mMemSize) {
            mEvents.removeLast();
        }

        mEvents.addFirst(event);
    }

    /**
     * Add a string-based event to the log, and print it to logcat as info.
     * @param msg the message for the logs
     * @param tag the logcat tag to use
     */
    public synchronized void loglogi(String msg, String tag) {
        final Event event = new StringEvent(msg);
        log(event.printLog(tag));
    }

    /**
     * Same as {@link #loglogi(String, String)} but specifying the logcat type
     * @param msg the message for the logs
     * @param logType the type of logcat entry
     * @param tag the logcat tag to use
     */
    public synchronized void loglog(String msg, @Event.LogType int logType, String tag) {
        final Event event = new StringEvent(msg);
        log(event.printLog(logType, tag));
    }

    /** Dumps events using {@link PrintWriter}. */
    public synchronized void dump(PrintWriter pw) {
        dump(pw, "" /* prefix */);
    }

    /** Dumps events using {@link PrintWriter} with a certain indent. */
    public synchronized void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "Events log: " + mTag);
        String indent = prefix + "  ";
        for (Event evt : mEvents) {
            pw.println(indent + evt.toString());
        }
    }

    public abstract static class Event {

        /** Timestamps formatter. */
        private static final SimpleDateFormat sFormat =
                new SimpleDateFormat("MM-dd HH:mm:ss:SSS", Locale.US);

        private final long mTimestamp;

        public Event() {
            mTimestamp = System.currentTimeMillis();
        }

        public String toString() {
            return (new StringBuilder(sFormat.format(new Date(mTimestamp))))
                    .append(" ").append(eventToString()).toString();
        }

        /**
         * Causes the string message for the event to appear in the logcat.
         * Here is an example of how to create a new event (a StringEvent), adding it to the logger
         * (an instance of AudioEventLogger) while also making it show in the logcat:
         * <pre>
         *     myLogger.log(
         *         (new StringEvent("something for logcat and logger")).printLog(MyClass.TAG) );
         * </pre>
         * @param tag the tag for the android.util.Log.v
         * @return the same instance of the event
         */
        public Event printLog(String tag) {
            return printLog(ALOGI, tag);
        }

        /** @hide */
        @IntDef(flag = false, value = {
                ALOGI,
                ALOGE,
                ALOGW,
                ALOGV }
        )
        @Retention(RetentionPolicy.SOURCE)
        public @interface LogType {}

        public static final int ALOGI = 0;
        public static final int ALOGE = 1;
        public static final int ALOGW = 2;
        public static final int ALOGV = 3;

        /**
         * Same as {@link #printLog(String)} with a log type
         * @param type one of {@link #ALOGI}, {@link #ALOGE}, {@link #ALOGV}
         * @param tag
         * @return
         */
        public Event printLog(@LogType int type, String tag) {
            switch (type) {
                case ALOGI:
                    Log.i(tag, eventToString());
                    break;
                case ALOGE:
                    Log.e(tag, eventToString());
                    break;
                case ALOGW:
                    Log.w(tag, eventToString());
                    break;
                case ALOGV:
                default:
                    Log.v(tag, eventToString());
                    break;
            }
            return this;
        }

        /**
         * Convert event to String.
         * This method is only called when the logger history is about to the dumped,
         * so this method is where expensive String conversions should be made, not when the Event
         * subclass is created.
         * Timestamp information will be automatically added, do not include it.
         * @return a string representation of the event that occurred.
         */
        public abstract String eventToString();
    }

    public static class StringEvent extends Event {

        @Nullable
        private final String mSource;

        private final String mDescription;

        /** Creates event from {@code source} and formatted {@code description} with {@code args} */
        public static StringEvent from(@NonNull String source,
                @NonNull String description, Object... args) {
            return new StringEvent(source, String.format(Locale.US, description, args));
        }

        public StringEvent(String description) {
            this(null /* source */, description);
        }

        public StringEvent(String source, String description) {
            mSource = source;
            mDescription = description;
        }

        @Override
        public String eventToString() {
            if (mSource == null) {
                return mDescription;
            }

            // [source ] optional description
            return String.format("[%-40s] %s",
                    mSource,
                    (mDescription == null ? "" : mDescription));
        }
    }
}
