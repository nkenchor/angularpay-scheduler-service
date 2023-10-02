package io.angularpay.scheduler.ports.outbound;


import io.angularpay.scheduler.domain.ScheduleStatus;
import io.angularpay.scheduler.domain.ScheduledTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    ScheduledTask createScheduledTask(ScheduledTask request);
    ScheduledTask updateScheduledTask(ScheduledTask request);
    Optional<ScheduledTask> findScheduledTaskByReference(String reference);
    Page<ScheduledTask> listScheduledTasks(Pageable pageable);
    List<ScheduledTask> findScheduledTaskByStatus(ScheduleStatus status);
}
