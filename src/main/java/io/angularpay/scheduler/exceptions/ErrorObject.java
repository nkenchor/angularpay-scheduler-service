
package io.angularpay.scheduler.exceptions;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorObject {

    private ErrorCode code;
    private String message;
    private String source;
}
