package io.angularpay.scheduler.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.scheduler.adapters.outbound.MongoAdapter;
import io.angularpay.scheduler.domain.Role;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.exceptions.ErrorObject;
import io.angularpay.scheduler.models.GetScheduleTasksCommandRequest;
import io.angularpay.scheduler.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class GetScheduleTasksCommand extends AbstractCommand<GetScheduleTasksCommandRequest, List<ScheduledTask>> {

    private final DefaultConstraintValidator validator;
    private final MongoAdapter mongoAdapter;

    public GetScheduleTasksCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            MongoAdapter mongoAdapter) {
        super("GetScheduleTasksCommand", mapper);
        this.validator = validator;
        this.mongoAdapter = mongoAdapter;
    }

    @Override
    protected String getResourceOwner(GetScheduleTasksCommandRequest request) {
        return "";
    }

    @Override
    protected List<ScheduledTask> handle(GetScheduleTasksCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        return this.mongoAdapter.listScheduledTasks(pageable).getContent();
    }

    @Override
    protected List<ErrorObject> validate(GetScheduleTasksCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_SCHEDULER_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
