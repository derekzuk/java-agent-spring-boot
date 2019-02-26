package derekzuk.agent.instrument.extension;

import derekzuk.agent.instrument.extension.domain.MetricRecord;
import derekzuk.agent.instrument.extension.domain.RequestRecord;
import derekzuk.agent.instrument.extension.util.HttpUrlConnectionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MetricsCollectorTest {

    @Mock
    HttpUrlConnectionUtil httpUrlConnectionUtilMock;

    @Test
    public void testReport() {
        MetricsCollector mc = new MetricsCollector(httpUrlConnectionUtilMock);
        String testMethodName = "testReportMethodName";
        long duration = 1;
        long responseSize = 2;
        String requestUniqueID = "requestUniqueID";

        mc.report(testMethodName, duration, responseSize, requestUniqueID);

        Map<String, MetricRecord> metricRecords = mc.getMetricRecords();
        Map<String, RequestRecord> requestRecords = mc.getRequestRecords();

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
        MetricsCollector mc = new MetricsCollector(httpUrlConnectionUtilMock);

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
        Map<String, RequestRecord> requestRecords = mc.getRequestRecords();

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

    @Test
    public void testReportOverTenRequests() {
        MetricsCollector mc = new MetricsCollector(httpUrlConnectionUtilMock);

        String testMethodName = "testReportOverTenRequestsMethodName";
        long duration = 1;
        long responseSize = 2;
        String requestUniqueID = "requestUniqueID";

        // Ten requests to trigger the UrlConnectionUtil methods
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);
        mc.report(testMethodName, duration, responseSize, requestUniqueID);

        Map<String, MetricRecord> metricRecords = mc.getMetricRecords();
        Map<String, RequestRecord> requestRecords = mc.getRequestRecords();

        // The UrlConnectionUtil methods are triggered to report data to the external web app
        verify(httpUrlConnectionUtilMock, times(1)).processMetricRecords(metricRecords);
        assertTrue(metricRecords.containsKey(testMethodName));
        assertFalse(metricRecords.containsKey("nonexistantMethodName"));
        assertEquals(metricRecords.get(testMethodName).getAvgDuration(), duration);
        assertEquals(metricRecords.get(testMethodName).getMinDuration(), duration);
        assertEquals(metricRecords.get(testMethodName).getMaxDuration(), duration);
        assertEquals(metricRecords.get(testMethodName).getAvgResponseSize(), responseSize);
        assertEquals(metricRecords.get(testMethodName).getMinResponseSize(), responseSize);
        assertEquals(metricRecords.get(testMethodName).getMaxResponseSize(), responseSize);
        assertEquals(metricRecords.get(testMethodName).getCallCounts(), 11);

    }
}