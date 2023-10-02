package io.angularpay.scheduler.domain.commands;

import io.angularpay.scheduler.adapters.outbound.MongoAdapter;
import io.angularpay.scheduler.configurations.AngularPayConfiguration;
import io.angularpay.scheduler.domain.ScheduleStatus;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.models.GenericServiceRequest;
import io.angularpay.scheduler.ports.outbound.GenericServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.angularpay.scheduler.domain.SchedulerCache.addSchedule;
import static io.angularpay.scheduler.domain.SchedulerCache.invalidateSchedule;
import static io.angularpay.scheduler.helpers.Helper.savedHeadersOrDefault;

@Slf4j
@Service
@Profile("!test")
public class StartUpCommand {

    private final MongoAdapter mongoAdapter;
    private final GenericServicePort genericServicePort;
    private final AngularPayConfiguration configuration;

    public StartUpCommand(
            MongoAdapter mongoAdapter,
            GenericServicePort genericServicePort,
            AngularPayConfiguration configuration) {
        this.mongoAdapter = mongoAdapter;
        this.genericServicePort = genericServicePort;
        this.configuration = configuration;
        this.execute();
    }

    private void execute() {
        if (!this.configuration.isExecutePastPending()) {
            return;
        }
        this.mongoAdapter.findScheduledTaskByStatus(ScheduleStatus.PENDING).stream().parallel().forEach(found -> {
            if (Instant.parse(found.getRunAt()).isAfter(Instant.now())) {
                long delay = Duration.between(Instant.now(), Instant.parse(found.getRunAt())).toMillis();
                ScheduledFuture<?> schedule = Executors.newScheduledThreadPool(1).schedule(
                        () -> {
                            boolean success = genericServicePort.executeTask(GenericServiceRequest.builder()
                                    .actionEndpoint(found.getActionEndpoint())
                                    .payload(found.getPayload())
                                    .build(), savedHeadersOrDefault(found.getAuthenticatedUser()));

                            found.setStatus(success ? ScheduleStatus.EXECUTED : ScheduleStatus.FAILED);
                            ScheduledTask reUpdated = this.mongoAdapter.updateScheduledTask(found);

                            invalidateSchedule(reUpdated.getReference());
                        }, delay, TimeUnit.MILLISECONDS
                );
                addSchedule(found.getReference(), schedule);
            } else {
                boolean success = genericServicePort.executeTask(GenericServiceRequest.builder()
                        .actionEndpoint(found.getActionEndpoint())
                        .payload(found.getPayload())
                        .build(), savedHeadersOrDefault(found.getAuthenticatedUser()));

                found.setStatus(success ? ScheduleStatus.EXECUTED : ScheduleStatus.FAILED);
                this.mongoAdapter.updateScheduledTask(found);
            }
        });
    }

}
