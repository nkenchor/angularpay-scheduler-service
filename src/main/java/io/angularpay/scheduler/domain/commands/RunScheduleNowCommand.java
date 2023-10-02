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
import io.angularpay.scheduler.models.RunScheduleCommandRequest;
import io.angularpay.scheduler.ports.outbound.GenericServicePort;
import io.angularpay.scheduler.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.scheduler.domain.SchedulerCache.invalidateSchedule;
import static io.angularpay.scheduler.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.scheduler.helpers.Helper.savedHeadersOrDefault;

@Slf4j
@Service
public class RunScheduleNowCommand extends AbstractCommand<RunScheduleCommandRequest, Void> {

    private final DefaultConstraintValidator validator;
    private final MongoAdapter mongoAdapter;
    private final GenericServicePort genericServicePort;

    public RunScheduleNowCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            MongoAdapter mongoAdapter,
            GenericServicePort genericServicePort) {
        super("RunScheduleNowCommand", mapper);
        this.validator = validator;
        this.mongoAdapter = mongoAdapter;
        this.genericServicePort = genericServicePort;
    }

    @Override
    protected String getResourceOwner(RunScheduleCommandRequest request) {
        return "";
    }

    @Override
    protected Void handle(RunScheduleCommandRequest request) {
        ScheduledTask found = getRequestByReferenceOrThrow(mongoAdapter, request.getReference());

        if (found.getStatus() != ScheduleStatus.PENDING) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(ErrorCode.ILLEGAL_SCHEDULE_STATE_ERROR)
                    .message(ErrorCode.ILLEGAL_SCHEDULE_STATE_ERROR.getDefaultMessage())
                    .build();
        }

        invalidateSchedule(found.getReference());

        boolean success = genericServicePort.executeTask(GenericServiceRequest.builder()
                .actionEndpoint(found.getActionEndpoint())
                .payload(found.getPayload())
                .build(), savedHeadersOrDefault(found.getAuthenticatedUser()));

        found.setStatus(success ? ScheduleStatus.EXECUTED : ScheduleStatus.FAILED);
        this.mongoAdapter.updateScheduledTask(found);
        return null;
    }

    @Override
    protected List<ErrorObject> validate(RunScheduleCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_SCHEDULER_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }

}
