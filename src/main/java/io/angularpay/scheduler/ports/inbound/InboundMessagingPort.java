package io.angularpay.scheduler.ports.inbound;

import io.angularpay.scheduler.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
