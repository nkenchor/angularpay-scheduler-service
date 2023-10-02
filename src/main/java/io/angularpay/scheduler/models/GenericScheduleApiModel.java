
package io.angularpay.scheduler.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;

@Data
public class GenericScheduleApiModel {

    @NotEmpty
    private String description;

    @NotEmpty
    @URL(regexp = "^(http|https).*")
    @JsonProperty("action_endpoint")
    private String actionEndpoint;

    private String payload;

    @JsonProperty("run_at")
    private String runAt;
}
