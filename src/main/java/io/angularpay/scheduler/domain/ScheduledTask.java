
package io.angularpay.scheduler.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.angularpay.scheduler.models.AuthenticatedUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("scheduled_tasks")
public class ScheduledTask {

    @Id
    private String id;
    @Version
    private int version;
    private String reference;
    @JsonProperty("created_on")
    private String createdOn;
    @JsonProperty("last_modified")
    private String lastModified;
    private String description;
    @JsonProperty("action_endpoint")
    private String actionEndpoint;
    private String payload;
    @JsonProperty("run_at")
    private String runAt;
    private ScheduleStatus status;
    private AuthenticatedUser authenticatedUser;
}
