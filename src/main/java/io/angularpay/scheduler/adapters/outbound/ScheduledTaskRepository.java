package io.angularpay.scheduler.adapters.outbound;

import io.angularpay.scheduler.domain.ScheduleStatus;
import io.angularpay.scheduler.domain.ScheduledTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduledTaskRepository extends MongoRepository<ScheduledTask, String> {

    Optional<ScheduledTask> findByReference(String reference);
    Page<ScheduledTask> findAll(Pageable pageable);
    List<ScheduledTask> findByStatus(ScheduleStatus status);
}
