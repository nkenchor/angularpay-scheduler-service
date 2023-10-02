package io.angularpay.scheduler.ports.outbound;

import io.angularpay.scheduler.models.GenericServiceRequest;

import java.util.Map;

public interface GenericServicePort {
    boolean executeTask(GenericServiceRequest request, Map<String, String> headers);
}
