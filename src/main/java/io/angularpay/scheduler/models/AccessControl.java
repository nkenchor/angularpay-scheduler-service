package io.angularpay.scheduler.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
public class AccessControl {

    private AuthenticatedUser authenticatedUser;
}
