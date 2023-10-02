package io.angularpay.scheduler.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class GetScheduleTaskByReferenceCommandRequest extends AccessControl {

    @NotEmpty
    private String reference;

    GetScheduleTaskByReferenceCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
