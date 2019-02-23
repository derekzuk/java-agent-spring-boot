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

    private static final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
    private static final double alpha = 0.015;

    /**
     * Report method call metrics.
     */
    public static void report(final String methodName,
                              final long duration,
                              final long responseSize,
                              final String requestUniqueID) {

        entries.compute(methodName,
                (final String key,
                 final Entry curr) -> {
                    // If no record exists for the given method name, create a new record
                    if (curr == null) {
                        return new Entry(1L, duration, responseSize);
                    }

                    // Determine min duration
                    long curMinDuration = curr.getMinDuration();
                    long minDuration;
                    if (duration < curMinDuration) {
                        minDuration = duration;
                    } else {
                        minDuration = curMinDuration;
                    }

                    // Determine max duration
                    long currMaxDuration = curr.getMaxDuration();
                    long maxDuration;
                    if (duration > currMaxDuration) {
                        maxDuration = duration;
                    } else {
                        maxDuration = currMaxDuration;
                    }

                    // Determine average duration
                    final long newAvgDuration = Math.round(
                            curr.getAvgDuration() * (1 - alpha) + duration * alpha
                    );

                    // Deterimine min response size
                    long currMinResponseSize = curr.getMinResponseSize();
                    long minResponseSize;
                    minResponseSize = responseSize < currMinResponseSize ? responseSize : currMinResponseSize;

                    // Deterimine max response size
                    long currMaxResponseSize = curr.getMaxResponseSize();
                    long maxResponseSize;
                    maxResponseSize = responseSize > currMaxResponseSize ? responseSize : currMaxResponseSize;

                    // Determine average response size
                    final long newAvgResponseSize = Math.round(
                            curr.getAvgResponseSize() * (1 - alpha) + responseSize * alpha
                    );

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


                    return new Entry(curr.getCallCounts() + 1,
                            newAvgDuration,
                            minDuration,
                            maxDuration,
                            newAvgResponseSize,
                            minResponseSize,
                            maxResponseSize);
                });

    }

    public static String executePost(String targetURL) {
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

    public static class Entry {
        private final long callCounts;
        private final long avgDuration;
        private final long minDuration;
        private final long maxDuration;
        private final long avgResponseSize;
        private final long minResponseSize;
        private final long maxResponseSize;

        private Entry(final long callCounts,
                      final long avgDuration,
                      final long minDuration,
                      final long maxDuration,
                      final long avgResponseSize,
                      final long minResponseSize,
                      final long maxResponseSize) {
            this.callCounts = callCounts;
            this.avgDuration = avgDuration;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.avgResponseSize = avgResponseSize;
            this.minResponseSize = minResponseSize;
            this.maxResponseSize = maxResponseSize;
        }

        public Entry(long callCounts, long duration, long responseSize) {
            this.callCounts = callCounts;
            this.avgDuration = duration;
            this.minDuration = duration;
            this.maxDuration = duration;
            this.avgResponseSize = responseSize;
            this.minResponseSize = responseSize;
            this.maxResponseSize = responseSize;
        }

        public long getCallCounts() {
            return callCounts;
        }

        public long getAvgDuration() {
            return avgDuration;
        }

        public long getMinDuration() {
            return minDuration;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public long getAvgResponseSize() {
            return avgResponseSize;
        }

        public long getMinResponseSize() {
            return minResponseSize;
        }

        public long getMaxResponseSize() {
            return maxResponseSize;
        }
    }

    public static Map<String, Entry> getEntries() {
        return Collections.unmodifiableMap(entries);
    }
}