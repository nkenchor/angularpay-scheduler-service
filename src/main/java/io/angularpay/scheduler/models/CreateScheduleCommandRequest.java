package io.angularpay.scheduler.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class CreateScheduleCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private GenericScheduleApiModel genericScheduleApiModel;

    CreateScheduleCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
