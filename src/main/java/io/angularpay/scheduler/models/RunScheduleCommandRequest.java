package io.angularpay.scheduler.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RunScheduleCommandRequest extends AccessControl {

    @NotEmpty
    private String reference;

    RunScheduleCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
