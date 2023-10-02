package io.angularpay.scheduler.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.scheduler.adapters.outbound.MongoAdapter;
import io.angularpay.scheduler.domain.Role;
import io.angularpay.scheduler.domain.ScheduleStatus;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.exceptions.CommandException;
import io.angularpay.scheduler.exceptions.ErrorCode;
import io.angularpay.scheduler.exceptions.ErrorObject;
import io.angularpay.scheduler.models.GenericServiceRequest;
import io.angularpay.scheduler.models.UpdateScheduleCommandRequest;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.angularpay.scheduler.common.Constants.ERROR_SOURCE;
import static io.angularpay.scheduler.domain.SchedulerCache.invalidateSchedule;
import static io.angularpay.scheduler.domain.SchedulerCache.updateSchedule;
import static io.angularpay.scheduler.exceptions.ErrorCode.VALIDATION_ERROR;
import static io.angularpay.scheduler.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.scheduler.helpers.Helper.savedHeadersOrDefault;

@Slf4j
@Service
public class UpdateScheduleCommand extends AbstractCommand<UpdateScheduleCommandRequest, Void> {

    private final DefaultConstraintValidator validator;
    private final MongoAdapter mongoAdapter;
    private final GenericServicePort genericServicePort;

    public UpdateScheduleCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            MongoAdapter mongoAdapter,
            GenericServicePort genericServicePort) {
        super("UpdateScheduleCommand", mapper);
        this.validator = validator;
        this.mongoAdapter = mongoAdapter;
        this.genericServicePort = genericServicePort;
    }

    @Override
    protected String getResourceOwner(UpdateScheduleCommandRequest request) {
        return "";
    }

    @Override
    protected Void handle(UpdateScheduleCommandRequest request) {
        if (Instant.parse(request.getGenericScheduleApiModel().getRunAt()).isBefore(Instant.now())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(ErrorCode.INVALID_SCHEDULE_DATE_ERROR)
                    .message(ErrorCode.INVALID_SCHEDULE_DATE_ERROR.getDefaultMessage())
                    .build();
        }

        ScheduledTask found = getRequestByReferenceOrThrow(mongoAdapter, request.getReference());

        if (found.getStatus() != ScheduleStatus.PENDING) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(ErrorCode.ILLEGAL_SCHEDULE_STATE_ERROR)
                    .message(ErrorCode.ILLEGAL_SCHEDULE_STATE_ERROR.getDefaultMessage())
                    .build();
        }

        ScheduledTask toUpdate = found.toBuilder()
                .description(request.getGenericScheduleApiModel().getDescription())
                .actionEndpoint(request.getGenericScheduleApiModel().getActionEndpoint())
                .payload(request.getGenericScheduleApiModel().getPayload())
                .runAt(request.getGenericScheduleApiModel().getRunAt())
                .build();

        ScheduledTask updated = this.mongoAdapter.updateScheduledTask(toUpdate);

        long delay = Duration.between(Instant.now(), Instant.parse(updated.getRunAt())).toMillis();
        ScheduledFuture<?> schedule = Executors.newScheduledThreadPool(1).schedule(
                () -> {
                    ScheduledTask reFound = getRequestByReferenceOrThrow(mongoAdapter, updated.getReference());

                    boolean success = genericServicePort.executeTask(GenericServiceRequest.builder()
                            .actionEndpoint(reFound.getActionEndpoint())
                            .payload(reFound.getPayload())
                            .build(), savedHeadersOrDefault(reFound.getAuthenticatedUser()));

                    reFound.setStatus(success ? ScheduleStatus.EXECUTED : ScheduleStatus.FAILED);
                    ScheduledTask reUpdated = this.mongoAdapter.updateScheduledTask(reFound);

                    invalidateSchedule(reUpdated.getReference());
                }, delay, TimeUnit.MILLISECONDS
        );
        updateSchedule(updated.getReference(), schedule);
        return null;
    }

    @Override
    protected List<ErrorObject> validate(UpdateScheduleCommandRequest request) {
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
        return Arrays.asList(Role.ROLE_SCHEDULER_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
