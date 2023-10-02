package io.angularpay.scheduler.ports.inbound;

import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.models.CreateScheduleResponse;
import io.angularpay.scheduler.models.GenericScheduleApiModel;

import java.util.List;
import java.util.Map;

public interface RestApiPort {
    CreateScheduleResponse createScheduledTask(GenericScheduleApiModel genericScheduleApiModel, Map<String, String> headers);
    void updateScheduledTask(String scheduleReference, GenericScheduleApiModel genericScheduleApiModel, Map<String, String> headers);
    void runScheduledTaskNow(String scheduleReference, Map<String, String> headers);
    ScheduledTask getScheduledTaskByReference(String scheduleReference, Map<String, String> headers);
    List<ScheduledTask> getScheduledTaskList(int page, Map<String, String> headers);
}
