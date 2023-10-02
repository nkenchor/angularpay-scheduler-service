package io.angularpay.scheduler.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.scheduler.adapters.outbound.MongoAdapter;
import io.angularpay.scheduler.domain.Role;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.exceptions.ErrorObject;
import io.angularpay.scheduler.models.GetScheduleTaskByReferenceCommandRequest;
import io.angularpay.scheduler.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.scheduler.helpers.CommandHelper.getRequestByReferenceOrThrow;

@Slf4j
@Service
public class GetScheduleTaskByReferenceCommand extends AbstractCommand<GetScheduleTaskByReferenceCommandRequest, ScheduledTask> {

    private final DefaultConstraintValidator validator;
    private final MongoAdapter mongoAdapter;

    public GetScheduleTaskByReferenceCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            MongoAdapter mongoAdapter) {
        super("GetScheduleTaskByReferenceCommand", mapper);
        this.validator = validator;
        this.mongoAdapter = mongoAdapter;
    }

    @Override
    protected String getResourceOwner(GetScheduleTaskByReferenceCommandRequest request) {
        return "";
    }

    @Override
    protected ScheduledTask handle(GetScheduleTaskByReferenceCommandRequest request) {
        return getRequestByReferenceOrThrow(mongoAdapter, request.getReference());
    }

    @Override
    protected List<ErrorObject> validate(GetScheduleTaskByReferenceCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_SCHEDULER_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
