package org.mym.plog.util;

import android.os.SystemClock;

import org.mym.plog.PLog;
import org.mym.plog.logger.Logger;

import java.util.ArrayList;

/**
 * A utility class to help log timings splits throughout a method call.
 * Typical usage is:
 *
 * <pre>
 *     TimingLogger timings = new TimingLogger(TAG, "methodA");
 *     // ... do some work A ...
 *     timings.addSplit("work A");
 *     // ... do some work B ...
 *     timings.addSplit("work B");
 *     // ... do some work C ...
 *     timings.addSplit("work C");
 *     timings.dumpToLog();
 * </pre>
 *
 * <p>The dumpToLog call would add the following to the log:</p>
 *
 * <pre>
 *     D/TAG     ( 3459): methodA: begin
 *     D/TAG     ( 3459): methodA:      9 ms, work A
 *     D/TAG     ( 3459): methodA:      1 ms, work B
 *     D/TAG     ( 3459): methodA:      6 ms, work C
 *     D/TAG     ( 3459): methodA: end, 16 ms
 * </pre>
 * IMPORTANT:This class is copied from AOSP standard class but change its disable logic to PLOG's
 * controller.
 */
public class TimingLogger {

    /** Stores the time of each split. */
    private ArrayList<Long> mSplits;
    /** Stores the labels for each split. */
    private ArrayList<String> mSplitLabels;
    /**
     * The Log tag to use for checking Log.isLoggable and for
     * logging the timings.
     */
    private String mTag;
    /** A label to be included in every log. */
    private String mLabel;
    /** Used to track whether LogController#isTimingLogEnabled was enabled at reset time. */
    private boolean mDisabled;

    private Logger mLogger;

    /**
     * Create and initialize a TimingLogger object that will log using
     * the specific tag. If the Log.isLoggable is not enabled to at
     * least the Log.VERBOSE level for that tag at creation time then
     * the addSplit and dumpToLog call will do nothing.
     * @param tag the log tag to use while logging the timings
     * @param label a string to be displayed with each log
     */
    public TimingLogger(Logger logger, String tag, String label) {
        mLogger = logger;
        reset(tag, label);
    }

    public void reset(String tag, String label) {
        mTag = tag;
        mLabel = label;
        reset();
    }

    public void setLogger(Logger logger) {
        this.mLogger = logger;
    }

    public void reset() {
//        mDisabled = !Log.isLoggable(mTag, Log.VERBOSE);
        mDisabled = !PLog.getCurrentConfig().getController().isTimingLogEnabled();
        if (mDisabled) return;
        if (mSplits == null) {
            mSplits = new ArrayList<>();
            mSplitLabels = new ArrayList<>();
        } else {
            mSplits.clear();
            mSplitLabels.clear();
        }
        addSplit(null);
    }

    public void addSplit(String splitLabel) {
        if (mDisabled) return;
        long now = SystemClock.elapsedRealtime();
        mSplits.add(now);
        mSplitLabels.add(splitLabel);
    }

    public void dumpToLog() {
        if (mDisabled) return;
        StringBuilder sb = new StringBuilder();
//        callLogger(mTag, mLabel + ": begin");
        sb.append(mLabel).append(":begin").append("\n");
        final long first = mSplits.get(0);
        long now = first;
        for (int i = 1; i < mSplits.size(); i++) {
            now = mSplits.get(i);
            final String splitLabel = mSplitLabels.get(i);
            final long prev = mSplits.get(i - 1);

//            callLogger(mTag, mLabel + ":      " + (now - prev) + " ms, " + splitLabel);
            sb.append(mLabel).append(":      ").append(now - prev).append(" ms, ")
                    .append(splitLabel)
                    .append("\n");
        }
//        callLogger(mTag, mLabel + ": end, " + (now - first) + " ms");
        sb.append(mLabel).append(": end, ").append(now - first).append(" ms")
                .append("\n");
        callLogger(mTag, sb.toString());
    }

    private void callLogger(String tag, String msg){
//        PLog.logWithStackOffset(Log.DEBUG, 3, tag, msg);
        mLogger.d(tag, msg);
    }
}
