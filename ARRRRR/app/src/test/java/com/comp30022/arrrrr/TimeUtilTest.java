package com.comp30022.arrrrr;

import com.comp30022.arrrrr.utils.TimeUtil;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests for {@link TimeUtil}
 *
 * @author Dafu Ai
 */

public class TimeUtilTest {

    @Test
    public void testGetFriendlyTime() {
        long today = System.currentTimeMillis();

        long twoSecAgo = today - 1000*2;
        long twoMinAgo = today - 1000*60*2;
        long twoHourAgo = today - 1000*60*60*2;
        long twoDaysAgo = today - 1000*60*60*24*2;
        long oneDayAgo = today - 1000*60*60*24;

        Assert.assertEquals("2 days ago", TimeUtil.getFriendlyTime(twoDaysAgo));
        Assert.assertEquals("Yesterday", TimeUtil.getFriendlyTime(oneDayAgo));
        Assert.assertEquals("2 hours ago", TimeUtil.getFriendlyTime(twoHourAgo));
        Assert.assertEquals("2 minutes ago", TimeUtil.getFriendlyTime(twoMinAgo));
        Assert.assertEquals("Just now", TimeUtil.getFriendlyTime(twoSecAgo));
    }

    @Test
    public void testIsTimeCloseToNow() {
        long now = System.currentTimeMillis();
        long twoSecAgo = now - 1000*2;
        long twoMinAgo = now - 1000*60*2;

        Assert.assertTrue(TimeUtil.isTimeCloseToNow(now));
        Assert.assertTrue(TimeUtil.isTimeCloseToNow(twoSecAgo));
        Assert.assertTrue(TimeUtil.isTimeCloseToNow(twoMinAgo));
    }
}
