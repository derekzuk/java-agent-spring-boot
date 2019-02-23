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
        String testMethodName = "testMethodName";
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

        // Request 1
        String testMethodName = "testMethodName";
        long duration = 100;
        long responseSize = 200;
        String requestUniqueID = "requestUniqueID";
        mc.report(testMethodName, duration, responseSize, requestUniqueID);

        // Request 2
        duration = 300;
        responseSize = 400;
        requestUniqueID = "requestUniqueID2";
        mc.report(testMethodName, duration, responseSize, requestUniqueID);

        Map<String, MetricRecord> metricRecords = mc.getMetricRecords();

        assertTrue(metricRecords.containsKey(testMethodName));
        assertFalse(metricRecords.containsKey("nonexistantMethodName"));
        assertEquals(metricRecords.get(testMethodName).getAvgDuration(), 103);
        assertEquals(metricRecords.get(testMethodName).getMinDuration(), 100);
        assertEquals(metricRecords.get(testMethodName).getMaxDuration(), 300);
        assertEquals(metricRecords.get(testMethodName).getAvgResponseSize(), 203);
        assertEquals(metricRecords.get(testMethodName).getMinResponseSize(), 200);
        assertEquals(metricRecords.get(testMethodName).getMaxResponseSize(), 400);
        assertEquals(metricRecords.get(testMethodName).getCallCounts(), 2);
    }
}