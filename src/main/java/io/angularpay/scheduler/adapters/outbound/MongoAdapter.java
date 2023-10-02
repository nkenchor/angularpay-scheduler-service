package io.angularpay.scheduler.adapters.outbound;

import io.angularpay.scheduler.domain.ScheduleStatus;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final ScheduledTaskRepository scheduledTaskRepository;

    @Override
    public ScheduledTask createScheduledTask(ScheduledTask request) {
        request.setCreatedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return scheduledTaskRepository.save(request);
    }

    @Override
    public ScheduledTask updateScheduledTask(ScheduledTask request) {
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return scheduledTaskRepository.save(request);
    }

    @Override
    public Optional<ScheduledTask> findScheduledTaskByReference(String reference) {
        return scheduledTaskRepository.findByReference(reference);
    }

    @Override
    public Page<ScheduledTask> listScheduledTasks(Pageable pageable) {
        return scheduledTaskRepository.findAll(pageable);
    }

    @Override
    public List<ScheduledTask> findScheduledTaskByStatus(ScheduleStatus status) {
        return scheduledTaskRepository.findByStatus(status);
    }
}
