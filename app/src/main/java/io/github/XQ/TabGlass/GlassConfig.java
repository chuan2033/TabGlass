package io.github.XQ.TabGlass;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

/**
 * The geometry the bar exposes: how wide, how tall, how far off the bottom, and
 * how much air goes around a column and around the droplet.
 *
 * <p>Read from two places, in this order. The companion settings app is the
 * normal one; it publishes the current values through a read-only provider,
 * because module code runs inside WeChat's and QQ's processes and cannot open
 * another app's preferences. The host's own preference file is still read
 * first so hand-written setups keep working, and the provider then wins over
 * it — the UI is the source of truth once it exists. Neither being present
 * leaves the defaults below, which match the settings app's own defaults.
 *
 * <p>All five are geometry only: they move our own pill and the space between
 * the app's tabs, never the tabs themselves, so icons, labels and hit targets
 * stay exactly as the host laid them out.
 */
final class GlassConfig {

    /** Named before QQ was a target; kept so existing WeChat setups still read. */
    private static final String PREFS = "wx_liquid_glass_cfg";

    /** Read-only channel published by the companion settings app. */
    private static final Uri COMPANION = Uri.parse(
            "content://io.github.XQ.TabGlass.prefs");

    /** Distance between the bottom of the glass pill and the screen edge, dp. */
    static volatile int barOffsetDp = 12;

    /**
     * Breathing room around each tab column, dp on every side of its content.
     * Only a floor: the pill's width comes from {@link #pillWidthPercent}, and
     * this guarantees a column can never be narrower than its content plus this
     * much air. Upstream KernelSU reads 32.
     */
    static volatile int columnPadDp = 50;

    /**
     * The pill's width as a percentage of the screen, columns split evenly.
     *
     * <p>WeChat's four tabs and QQ's three hug their labels to very different
     * totals — 962px vs 665px on the same screen — which reads as two different
     * bars. Deriving the width from the screen instead makes both hosts the
     * same size, with the extra room spread evenly between the columns. 0
     * restores the upstream behaviour of hugging the labels.
     */
    static volatile int pillWidthPercent = 80;

    /**
     * Horizontal padding on the tab row, dp. Upstream: 4.
     *
     * <p>This is also the droplet's inset on all four sides: the droplet spans
     * exactly one tab column, so the space left of the first column *is* this
     * value. Keeping the vertical inset at the same number is what keeps the
     * highlight centred in the capsule — a wider rowPad with a 4dp vertical
     * inset reads as visibly lopsided on the first and last tab.
     */
    static volatile int rowPadDp = 4;

    /**
     * The pill's own height, dp — absolute, not relative to the host's bar.
     *
     * <p>WeChat and QQ dock their bars at different heights (152px vs 141px on
     * the same screen), so "the app's height plus a few dp" gives the two hosts
     * two different pills. Pinning the pill makes both identical, and each
     * app's tab row is simply centred inside it. A host whose own content is
     * taller than this still wins: the pill never shrinks below its tabs.
     */
    static volatile int pillHeightDp = 69;

    /** Whether the companion app answered; logged once per process. */
    private static boolean sCompanionSeen;

    private GlassConfig() {
    }

    static void load(Context ctx) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, 0);
            barOffsetDp = p.getInt("barOffsetDp", barOffsetDp);
            columnPadDp = p.getInt("columnPadDp", columnPadDp);
            rowPadDp = p.getInt("rowPadDp", rowPadDp);
            pillHeightDp = p.getInt("pillHeightDp", pillHeightDp);
            pillWidthPercent = p.getInt("pillWidthPercent", pillWidthPercent);
        } catch (Throwable t) {
            LiquidGlassModule.logErr("config load failed", t);
        }
        readCompanion(ctx);
        LiquidGlassModule.log(android.util.Log.INFO,
                "config: offset=" + barOffsetDp + "dp columnPad=" + columnPadDp
                        + "dp rowPad=" + rowPadDp + "dp pillH=" + pillHeightDp
                        + "dp pillW=" + pillWidthPercent + "%"
                        + (sCompanionSeen ? " (companion)" : " (host prefs)"));
    }

    /**
     * Reads the settings app's current values. Absent app, disabled provider and
     * a process that cannot reach it all land on the same path: keep whatever
     * the host preference file and the defaults already produced.
     */
    private static void readCompanion(Context ctx) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(COMPANION, null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }
            barOffsetDp = intOr(cursor, "barOffsetDp", barOffsetDp);
            columnPadDp = intOr(cursor, "columnPadDp", columnPadDp);
            rowPadDp = intOr(cursor, "rowPadDp", rowPadDp);
            pillHeightDp = intOr(cursor, "pillHeightDp", pillHeightDp);
            pillWidthPercent = intOr(cursor, "pillWidthPercent", pillWidthPercent);
            sCompanionSeen = true;
            rememberLocally(ctx);
        } catch (Throwable t) {
            // A missing or disabled settings app is a normal state, not an error.
            sCompanionSeen = false;
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static int intOr(Cursor cursor, String column, int fallback) {
        int index = cursor.getColumnIndex(column);
        if (index < 0 || cursor.isNull(index)) {
            return fallback;
        }
        return cursor.getInt(index);
    }

    /**
     * Keeps the values the settings app just supplied in the host's own
     * preference file.
     *
     * <p>That file is read first on the next load, so a settings app that has
     * been force-stopped — after which nothing may start it, provider included —
     * no longer drops the bar back to the built-in defaults. Only a real change
     * is written.
     */
    private static void rememberLocally(Context ctx) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, 0);
            boolean same = p.getInt("barOffsetDp", Integer.MIN_VALUE) == barOffsetDp
                    && p.getInt("columnPadDp", Integer.MIN_VALUE) == columnPadDp
                    && p.getInt("rowPadDp", Integer.MIN_VALUE) == rowPadDp
                    && p.getInt("pillHeightDp", Integer.MIN_VALUE) == pillHeightDp
                    && p.getInt("pillWidthPercent", Integer.MIN_VALUE) == pillWidthPercent;
            if (same) {
                return;
            }
            p.edit()
                    .putInt("barOffsetDp", barOffsetDp)
                    .putInt("columnPadDp", columnPadDp)
                    .putInt("rowPadDp", rowPadDp)
                    .putInt("pillHeightDp", pillHeightDp)
                    .putInt("pillWidthPercent", pillWidthPercent)
                    .apply();
        } catch (Throwable t) {
            LiquidGlassModule.logErr("could not cache the configuration locally", t);
        }
    }

    /**
     * Fingerprint of the current values. The installer stores the one it built
     * the live pill from, so a change can be applied without waiting for the
     * host process to restart.
     */
    static int signature() {
        int hash = barOffsetDp;
        hash = hash * 31 + columnPadDp;
        hash = hash * 31 + rowPadDp;
        hash = hash * 31 + pillHeightDp;
        hash = hash * 31 + pillWidthPercent;
        return hash;
    }
}
