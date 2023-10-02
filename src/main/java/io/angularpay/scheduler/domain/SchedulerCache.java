package io.angularpay.scheduler.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

public class SchedulerCache {

    private static final Map<String, ScheduledFuture<?>> schedules = new HashMap<>();

    public static void addSchedule(String reference, ScheduledFuture<?> newScheduledFuture) {
        schedules.computeIfPresent(reference, (s, existingScheduledFuture) -> {
            existingScheduledFuture.cancel(false);
            return newScheduledFuture;
        });
        schedules.putIfAbsent(reference, newScheduledFuture);
    }

    public static void updateSchedule(String reference, ScheduledFuture<?> newScheduledFuture) {
        schedules.computeIfPresent(reference, (s, existingScheduledFuture) -> {
            existingScheduledFuture.cancel(false);
            return newScheduledFuture;
        });
        schedules.put(reference, newScheduledFuture);
    }

    public static void invalidateSchedule(String reference) {
        ScheduledFuture<?> existingScheduledFuture = schedules.get(reference);
        if (Objects.nonNull(existingScheduledFuture)) {
            existingScheduledFuture.cancel(true);
        }
        schedules.remove(reference);
    }

}
