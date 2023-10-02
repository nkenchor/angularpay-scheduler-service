
package io.angularpay.scheduler.models.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformTTLConfiguration {

    @JsonProperty("time_unit")
    private String timeUnit;
    private Long value;

}
