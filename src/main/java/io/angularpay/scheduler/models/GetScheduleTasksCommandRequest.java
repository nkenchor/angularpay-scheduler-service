package io.angularpay.scheduler.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class GetScheduleTasksCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private Paging paging;

    GetScheduleTasksCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
