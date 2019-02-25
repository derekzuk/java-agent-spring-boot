package derekzuk.agent.instrument.extension.httpUrlConnectionUtil;

import derekzuk.agent.instrument.extension.domain.MetricRecord;
import derekzuk.agent.instrument.extension.domain.RequestRecord;

import java.util.Map;

public interface HttpUrlConnectionUtil {

    String processMetricRecords(Map<String, MetricRecord> metricRecords);

    String processRequestRecords(Map<String, RequestRecord> requestRecords);

}
