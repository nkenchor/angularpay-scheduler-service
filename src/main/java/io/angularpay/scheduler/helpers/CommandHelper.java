package io.angularpay.scheduler.helpers;

import io.angularpay.scheduler.adapters.outbound.MongoAdapter;
import io.angularpay.scheduler.domain.ScheduledTask;
import io.angularpay.scheduler.exceptions.CommandException;
import io.angularpay.scheduler.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandHelper {

    public static ScheduledTask getRequestByReferenceOrThrow(MongoAdapter mongoAdapter, String userReference) {
        return mongoAdapter.findScheduledTaskByReference(userReference).orElseThrow(
                () -> CommandException.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .errorCode(ErrorCode.REQUEST_NOT_FOUND)
                        .message(ErrorCode.REQUEST_NOT_FOUND.getDefaultMessage())
                        .build()
        );
    }

}
