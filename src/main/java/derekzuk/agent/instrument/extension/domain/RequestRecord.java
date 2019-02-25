package derekzuk.agent.instrument.extension.domain;

public class RequestRecord {
    private final String UUID;
    private final String methodName;
    private final long duration;
    private final long responseSize;

    public RequestRecord(String UUID, String methodName, long duration, long responseSize) {
        this.UUID = UUID;
        this.methodName = methodName;
        this.duration = duration;
        this.responseSize = responseSize;
    }

    public String getUUID() {
        return UUID;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getDuration() {
        return duration;
    }

    public long getResponseSize() {
        return responseSize;
    }
}
