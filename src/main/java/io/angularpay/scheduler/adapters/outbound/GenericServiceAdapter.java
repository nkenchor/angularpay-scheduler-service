package io.angularpay.scheduler.adapters.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.scheduler.models.GenericServiceRequest;
import io.angularpay.scheduler.ports.outbound.GenericServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static io.angularpay.scheduler.helpers.Helper.writeAsStringOrDefault;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericServiceAdapter implements GenericServicePort {

    private final WebClient webClient;
    private final ObjectMapper mapper;

    @Override
    public boolean executeTask(GenericServiceRequest request, Map<String, String> headers) {
        try {
            log.info("executing scheduled task {}", writeAsStringOrDefault(mapper, request));

            URI genericServiceUrl = UriComponentsBuilder.fromUriString(request.getActionEndpoint()).build().toUri();
            WebClient.RequestBodySpec builder = this.webClient
                    .post()
                    .uri(genericServiceUrl.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-angularpay-username", headers.get("x-angularpay-username"))
                    .header("x-angularpay-device-id", headers.get("x-angularpay-device-id"))
                    .header("x-angularpay-user-reference", headers.get("x-angularpay-user-reference"))
                    .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"));

            Boolean actionResponse;
            if (StringUtils.hasText(request.getPayload())) {
                JsonNode data = mapper.readTree(request.getPayload());
                actionResponse = builder.body(Mono.just(data), JsonNode.class)
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return Mono.just(true);
                            } else {
                                return Mono.empty();
                            }
                        }).block();
            } else {
                actionResponse = builder.exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just(true);
                    } else {
                        return Mono.empty();
                    }
                }).block();
            }
            boolean success = Objects.nonNull(actionResponse);
            if (success) {
                log.info("executed scheduled task with success status: {}",  writeAsStringOrDefault(mapper, request));
            } else {
                log.info("executed scheduled task with failure status: {}",  writeAsStringOrDefault(mapper, request));
            }
            return success;
        } catch (ResponseStatusException | JsonProcessingException exception) {
            log.error("An error occurred while executing scheduled task: {}", writeAsStringOrDefault(mapper, request), exception);
            return false;
        }
    }
}
