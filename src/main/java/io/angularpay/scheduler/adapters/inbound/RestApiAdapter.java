package io.angularpay.scheduler.adapters.inbound;

import io.angularpay.scheduler.configurations.AngularPayConfiguration;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.domain.commands.*;
import io.angularpay.scheduler.models.*;
import io.angularpay.scheduler.ports.inbound.RestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.angularpay.scheduler.helpers.Helper.fromHeaders;

@RestController
@RequestMapping("/scheduler/schedules")
@RequiredArgsConstructor
public class RestApiAdapter implements RestApiPort {

    private final CreateScheduleCommand createScheduleCommand;
    private final UpdateScheduleCommand updateScheduleCommand;
    private final RunScheduleNowCommand runScheduleNowCommand;
    private final GetScheduleTaskByReferenceCommand getScheduleTaskByReferenceCommand;
    private final GetScheduleTasksCommand getScheduleTasksCommand;

    private final AngularPayConfiguration configuration;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Override
    public CreateScheduleResponse createScheduledTask(
            @RequestBody GenericScheduleApiModel genericScheduleApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        CreateScheduleCommandRequest createScheduleCommandRequest = CreateScheduleCommandRequest.builder()
                .genericScheduleApiModel(genericScheduleApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.createScheduleCommand.execute(createScheduleCommandRequest);
    }

    @PutMapping("{scheduleReference}")
    @Override
    public void updateScheduledTask(
            @PathVariable String scheduleReference,
            @RequestBody GenericScheduleApiModel genericScheduleApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateScheduleCommandRequest updateScheduleCommandRequest = UpdateScheduleCommandRequest.builder()
                .reference(scheduleReference)
                .genericScheduleApiModel(genericScheduleApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateScheduleCommand.execute(updateScheduleCommandRequest);
    }

    @PostMapping("{scheduleReference}")
    @Override
    public void runScheduledTaskNow(
            @PathVariable String scheduleReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RunScheduleCommandRequest runScheduleCommandRequest = RunScheduleCommandRequest.builder()
                .reference(scheduleReference)
                .authenticatedUser(authenticatedUser)
                .build();
        this.runScheduleNowCommand.execute(runScheduleCommandRequest);
    }

    @GetMapping("{scheduleReference}")
    @ResponseBody
    @Override
    public ScheduledTask getScheduledTaskByReference(
            @PathVariable String scheduleReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetScheduleTaskByReferenceCommandRequest getScheduleTaskByReferenceCommandRequest = GetScheduleTaskByReferenceCommandRequest.builder()
                .reference(scheduleReference)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.getScheduleTaskByReferenceCommand.execute(getScheduleTaskByReferenceCommandRequest);
    }

    @GetMapping("/list/page/{page}")
    @ResponseBody
    @Override
    public List<ScheduledTask> getScheduledTaskList(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetScheduleTasksCommandRequest getScheduleTasksCommandRequest = GetScheduleTasksCommandRequest.builder()
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .authenticatedUser(authenticatedUser)
                .build();
        return this.getScheduleTasksCommand.execute(getScheduleTasksCommandRequest);
    }

}
