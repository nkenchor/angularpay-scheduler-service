package io.angularpay.scheduler.adapters.inbound;

import io.angularpay.scheduler.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.scheduler.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.scheduler.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static io.angularpay.scheduler.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
@Profile("!test")
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
