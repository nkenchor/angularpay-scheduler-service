
package io.angularpay.scheduler.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericServiceRequest {

    private String actionEndpoint;
    private String payload;
}
