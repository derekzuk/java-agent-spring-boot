package derekzuk.agent.instrument.extension;

import derekzuk.agent.instrument.extension.domain.MetricRecord;
import derekzuk.agent.instrument.extension.domain.RequestRecord;
import derekzuk.agent.instrument.extension.util.HttpUrlConnectionImpl;
import derekzuk.agent.instrument.extension.util.HttpUrlConnectionUtil;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Collects metrics on HTTP requests
 *
 * Establishes the following metrics for each HTTP request:
 * Average duration (exponential moving average)
 * Minimum duration
 * Maximum duration
 * Average response size (bytes)
 * Minimum response size
 * Maximum response size
 * Count of times the HTTP request has been executed
 */
public class MetricsCollector {

    private static final ConcurrentHashMap<String, MetricRecord> metricRecords = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, RequestRecord> requestRecordsByUUID = new ConcurrentHashMap<>();
    private static AtomicInteger counter = new AtomicInteger(0);
    private static HttpUrlConnectionUtil httpUrlConnectionUtil;
    public static final double alpha = 0.5;

    MetricsCollector(HttpUrlConnectionUtil httpUrlConnectionUtil) {
        MetricsCollector.httpUrlConnectionUtil = httpUrlConnectionUtil;
    }

    /**
     * Report method call metrics.
     *
     * Reports are occasionally sent to the external monitoring website.
     * We only report occasionally in order to reduce the impact the agent has on the application.
     */
    public static void report(final String methodName,
                              final long duration,
                              final long responseSize,
                              final String UUID) {

        // Establish metrics for the given method
        metricRecords.compute(methodName,
                (final String key,
                 final MetricRecord curr) -> {
                    // If no record exists for the given method name, create a new record
                    if (curr == null) {
                        return new MetricRecord(1L, duration, responseSize);
                    }

                    // Establish metrics
                    long minDuration = determineMinDuration(curr, duration);
                    long maxDuration = determineMaxDuration(curr, duration);
                    final long newAvgDuration = calculateEMA(true, curr.getAvgDuration(), duration);
                    long minResponseSize = determineMinResponseSize(curr, responseSize);
                    long maxResponseSize = determineMaxResponseSize(curr, responseSize);
                    final long newAvgResponseSize = calculateEMA(false, curr.getAvgResponseSize(), responseSize);

                    return new MetricRecord(curr.getCallCounts() + 1,
                            newAvgDuration,
                            minDuration,
                            maxDuration,
                            newAvgResponseSize,
                            minResponseSize,
                            maxResponseSize);
                });

        // Add request to Map to be retrieved later by requestUniqueID (UUID)
        RequestRecord requestRecord = new RequestRecord(UUID, methodName, duration, responseSize);

        requestRecordsByUUID.put(UUID, requestRecord);

        // Occasionally report to standalone web app
        if (counter.incrementAndGet() > 10) {
            if (httpUrlConnectionUtil == null) {
                httpUrlConnectionUtil = new HttpUrlConnectionImpl();
            }
            httpUrlConnectionUtil.processMetricRecords(getMetricRecords());
            httpUrlConnectionUtil.processRequestRecords(getRequestRecords());

            requestRecordsByUUID = new ConcurrentHashMap<>();
            counter.set(0);
        }
    }

    public static long calculateEMA(boolean isDuration, long currentVal, long newVal) {
        if (isDuration) {
            return Math.round(
                    currentVal * (1 - alpha) + newVal * alpha
            );
        } else {
            return Math.round(
                    currentVal * (1 - alpha) + newVal * alpha
            );
        }
    }

    private static long determineMaxResponseSize(MetricRecord curr, long responseSize) {
        long currMaxResponseSize = curr.getMaxResponseSize();
        long maxResponseSize;
        return responseSize > currMaxResponseSize ? responseSize : currMaxResponseSize;
    }

    private static long determineMinResponseSize(MetricRecord curr, long responseSize) {
        long currMinResponseSize = curr.getMinResponseSize();
        long minResponseSize;
        return responseSize < currMinResponseSize ? responseSize : currMinResponseSize;
    }

    private static long determineMaxDuration(MetricRecord curr, long duration) {
        long currMaxDuration = curr.getMaxDuration();
        long maxDuration;
        if (duration > currMaxDuration) {
            maxDuration = duration;
        } else {
            maxDuration = currMaxDuration;
        }
        return maxDuration;
    }

    private static long determineMinDuration(MetricRecord curr, long duration) {
        long curMinDuration = curr.getMinDuration();
        long minDuration;
        if (duration < curMinDuration) {
            minDuration = duration;
        } else {
            minDuration = curMinDuration;
        }
        return minDuration;
    }

    public static Map<String, MetricRecord> getMetricRecords() {
        return Collections.unmodifiableMap(metricRecords);
    }

    public static Map<String, RequestRecord> getRequestRecords() {
        return Collections.unmodifiableMap(requestRecordsByUUID);
    }
}