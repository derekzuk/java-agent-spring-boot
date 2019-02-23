package derekzuk.agent.instrument.extension;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects metrics on method calls.
 *
 * Counts the total number of method calls and
 * the average duration of method execution (with Exponential moving average
 * https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average).
 */
public class MetricsCollector {

    private static final ConcurrentHashMap<String, MetricRecord> metricRecords = new ConcurrentHashMap<>();
    public static final double alpha = 0.5;

    /**
     * Report method call metrics.
     */
    public static void report(final String methodName,
                              final long duration,
                              final long responseSize,
                              final String requestUniqueID) {

        metricRecords.compute(methodName,
                (final String key,
                 final MetricRecord curr) -> {
                    // If no record exists for the given method name, create a new record
                    if (curr == null) {
                        return new MetricRecord(1L, duration, responseSize);
                    }

                    // Determine min duration
                    long minDuration = determineMinDuration(curr, duration);
                    // Determine max duration
                    long maxDuration = determineMaxDuration(curr, duration);
                    // Determine average duration
                    final long newAvgDuration = calculateEMA(true, curr.getAvgDuration(), duration);
                    // Deterimine min response size
                    long minResponseSize = determineMinResponseSize(curr, responseSize);
                    // Deterimine max response size
                    long maxResponseSize = determineMaxResponseSize(curr, responseSize);
                    // Determine average response size
                    final long newAvgResponseSize = calculateEMA(false, curr.getAvgResponseSize(), responseSize);

                    // Report to standalone web app
                    executePost("http://localhost:8081/responseEntity/"
                            + methodName
                            + "/" + requestUniqueID
                            + "/" + duration
                            + "/" + newAvgDuration
                            + "/" + minDuration
                            + "/" + maxDuration
                            + "/" + responseSize
                            + "/" + newAvgResponseSize
                            + "/" + minResponseSize
                            + "/" + maxResponseSize);


                    return new MetricRecord(curr.getCallCounts() + 1,
                            newAvgDuration,
                            minDuration,
                            maxDuration,
                            newAvgResponseSize,
                            minResponseSize,
                            maxResponseSize);
                });

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

    private static String executePost(String targetURL) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Map<String, MetricRecord> getMetricRecords() {
        return Collections.unmodifiableMap(metricRecords);
    }
}