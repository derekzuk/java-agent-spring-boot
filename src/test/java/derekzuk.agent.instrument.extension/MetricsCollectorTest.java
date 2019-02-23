package derekzuk.agent.instrument.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;


public class MetricsCollectorTest {

    @Test
    public void testReport() {
        MetricsCollector mc = new MetricsCollector();
        String testMethodName = "testReportMethodName";
        long duration = 1;
        long responseSize = 2;
        String requestUniqueID = "requestUniqueID";

        mc.report(testMethodName, duration, responseSize, requestUniqueID);

        Map<String, MetricRecord> metricRecords = mc.getMetricRecords();

        assertTrue(metricRecords.containsKey(testMethodName));
        assertFalse(metricRecords.containsKey("nonexistantMethodName"));
        assertEquals(metricRecords.get(testMethodName).getAvgDuration(), duration);
        assertEquals(metricRecords.get(testMethodName).getMinDuration(), duration);
        assertEquals(metricRecords.get(testMethodName).getMaxDuration(), duration);
        assertEquals(metricRecords.get(testMethodName).getAvgResponseSize(), responseSize);
        assertEquals(metricRecords.get(testMethodName).getMinResponseSize(), responseSize);
        assertEquals(metricRecords.get(testMethodName).getMaxResponseSize(), responseSize);
        assertEquals(metricRecords.get(testMethodName).getCallCounts(), 1);
    }

    @Test
    public void testReportMultipleRequests() {
        MetricsCollector mc = new MetricsCollector();

        // Setup test variables
        String testMethodName = "testReportMultipleRequestsMethodName";
        long firstDuration = 100;
        long secondDuration = 300;
        long firstResponseSize = 200;
        long secondResponseSize = 400;
        String firstRequestUniqueID = "requestUniqueID";
        String secondRequestUniqueID = "secondRequestUniqueID";
        long finalAvgDuration = mc.calculateEMA(true, firstDuration, secondDuration);
        long finalAvgResponseSize = mc.calculateEMA(false, firstResponseSize, secondResponseSize);

        // Request 1
        mc.report(testMethodName, firstDuration, firstResponseSize, firstRequestUniqueID);

        // Request 2
        mc.report(testMethodName, secondDuration, secondResponseSize, secondRequestUniqueID);

        Map<String, MetricRecord> metricRecords = mc.getMetricRecords();

        assertTrue(metricRecords.containsKey(testMethodName));
        assertFalse(metricRecords.containsKey("nonexistantMethodName"));
        assertEquals(metricRecords.get(testMethodName).getAvgDuration(), finalAvgDuration);
        assertEquals(metricRecords.get(testMethodName).getMinDuration(), firstDuration);
        assertEquals(metricRecords.get(testMethodName).getMaxDuration(), secondDuration);
        assertEquals(metricRecords.get(testMethodName).getAvgResponseSize(), finalAvgResponseSize);
        assertEquals(metricRecords.get(testMethodName).getMinResponseSize(), firstResponseSize);
        assertEquals(metricRecords.get(testMethodName).getMaxResponseSize(), secondResponseSize);
        assertEquals(metricRecords.get(testMethodName).getCallCounts(), 2);
    }
}