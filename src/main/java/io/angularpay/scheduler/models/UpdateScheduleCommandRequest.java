package io.angularpay.scheduler.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class UpdateScheduleCommandRequest extends AccessControl {

    @NotEmpty
    private String reference;

    @NotNull
    @Valid
    private GenericScheduleApiModel genericScheduleApiModel;

    UpdateScheduleCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
