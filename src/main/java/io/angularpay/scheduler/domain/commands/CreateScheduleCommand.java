package io.angularpay.scheduler.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.scheduler.adapters.outbound.GenericServiceAdapter;
import io.angularpay.scheduler.adapters.outbound.MongoAdapter;
import io.angularpay.scheduler.domain.Role;
import io.angularpay.scheduler.domain.ScheduleStatus;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.exceptions.CommandException;
import io.angularpay.scheduler.exceptions.ErrorCode;
import io.angularpay.scheduler.exceptions.ErrorObject;
import io.angularpay.scheduler.models.CreateScheduleCommandRequest;
import io.angularpay.scheduler.models.CreateScheduleResponse;
import io.angularpay.scheduler.models.GenericServiceRequest;
import io.angularpay.scheduler.ports.outbound.GenericServicePort;
import io.angularpay.scheduler.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.angularpay.scheduler.common.Constants.ERROR_SOURCE;
import static io.angularpay.scheduler.domain.SchedulerCache.addSchedule;
import static io.angularpay.scheduler.domain.SchedulerCache.invalidateSchedule;
import static io.angularpay.scheduler.exceptions.ErrorCode.VALIDATION_ERROR;
import static io.angularpay.scheduler.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.scheduler.helpers.Helper.savedHeadersOrDefault;

@Slf4j
@Service
public class CreateScheduleCommand extends AbstractCommand<CreateScheduleCommandRequest, CreateScheduleResponse> {

    private final DefaultConstraintValidator validator;
    private final MongoAdapter mongoAdapter;
    private final GenericServicePort genericServicePort;

    public CreateScheduleCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            MongoAdapter mongoAdapter,
            GenericServiceAdapter genericServiceAdapter) {
        super("CreateScheduleCommand", mapper);
        this.validator = validator;
        this.mongoAdapter = mongoAdapter;
        this.genericServicePort = genericServiceAdapter;
    }

    @Override
    protected String getResourceOwner(CreateScheduleCommandRequest request) {
        return "";
    }

    @Override
    protected CreateScheduleResponse handle(CreateScheduleCommandRequest request) {
        if (Instant.parse(request.getGenericScheduleApiModel().getRunAt()).isBefore(Instant.now())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(ErrorCode.INVALID_SCHEDULE_DATE_ERROR)
                    .message(ErrorCode.INVALID_SCHEDULE_DATE_ERROR.getDefaultMessage())
                    .build();
        }

        ScheduledTask scheduledTask = ScheduledTask.builder()
                .reference(UUID.randomUUID().toString())
                .description(request.getGenericScheduleApiModel().getDescription())
                .actionEndpoint(request.getGenericScheduleApiModel().getActionEndpoint())
                .payload(request.getGenericScheduleApiModel().getPayload())
                .runAt(request.getGenericScheduleApiModel().getRunAt())
                .status(ScheduleStatus.PENDING)
                .authenticatedUser(request.getAuthenticatedUser())
                .build();

        ScheduledTask created = this.mongoAdapter.createScheduledTask(scheduledTask);

        long delay = Duration.between(Instant.now(), Instant.parse(request.getGenericScheduleApiModel().getRunAt())).toMillis();
        ScheduledFuture<?> schedule = Executors.newScheduledThreadPool(1).schedule(
                () -> {
                    ScheduledTask found = getRequestByReferenceOrThrow(mongoAdapter, created.getReference());

                    boolean success = genericServicePort.executeTask(GenericServiceRequest.builder()
                            .actionEndpoint(found.getActionEndpoint())
                            .payload(found.getPayload())
                            .build(), savedHeadersOrDefault(found.getAuthenticatedUser()));

                    found.setStatus(success ? ScheduleStatus.EXECUTED : ScheduleStatus.FAILED);
                    ScheduledTask updated = this.mongoAdapter.updateScheduledTask(found);

                    invalidateSchedule(updated.getReference());
                }, delay, TimeUnit.MILLISECONDS
        );
        addSchedule(created.getReference(), schedule);
        return new CreateScheduleResponse(created.getReference());
    }

    @Override
    protected List<ErrorObject> validate(CreateScheduleCommandRequest request) {
        List<ErrorObject> errors = new ArrayList<>();
        try {
            Instant.parse(request.getGenericScheduleApiModel().getRunAt());
        } catch (DateTimeParseException exception) {
            errors.add(ErrorObject.builder()
                    .code(VALIDATION_ERROR)
                    .message("run_at must be a valid date")
                    .source(ERROR_SOURCE)
                    .build());
        }
        errors.addAll(this.validator.validate(request));
        return errors;
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_UNVERIFIED_USER, Role.ROLE_VERIFIED_USER, Role.ROLE_SCHEDULER_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
