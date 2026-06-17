package com.mts.gateway.client;

import com.mts.gateway.dto.TaskDto;
import com.mts.gateway.exception.ExternalApiException;
import com.mts.gateway.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Component
public class ExternalTasksClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);

    private final RestClient restClient;

    public ExternalTasksClient(RestClient externalTasksRestClient) {
        this.restClient = externalTasksRestClient;
    }

    public URI createTask(TaskDto taskDto) {
        return restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(taskDto)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(201))) {
                        return response.getHeaders().getLocation();
                    }
                    handleErrorStatus(response.getStatusCode(), response);
                    return null;
                });
    }

    public TaskDto getTask(String id) {
        return restClient.get()
                .uri("/tasks/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        checkContentType(response.getHeaders().getContentType(), response);
                        return response.bodyTo(TaskDto.class);
                    }
                    handleErrorStatus(response.getStatusCode(), response);
                    return null;
                });
    }

    public List<TaskDto> getTasks(Boolean completed, Integer limit) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/tasks");
                    if (completed != null) {
                        uriBuilder.queryParam("completed", completed);
                    }
                    if (limit != null) {
                        uriBuilder.queryParam("limit", limit);
                    }
                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        checkContentType(response.getHeaders().getContentType(), response);
                        return response.bodyTo(new ParameterizedTypeReference<List<TaskDto>>() {});
                    }
                    handleErrorStatus(response.getStatusCode(), response);
                    return null;
                });
    }

    public void deleteTask(String id) {
        restClient.delete()
                .uri("/tasks/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    if (response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))) {
                        throw new TaskNotFoundException("Task not found");
                    }
                    throw new ExternalApiException("Unexpected error: " + response.getStatusCode().value());
                })
                .toBodilessEntity();
    }

    private void handleErrorStatus(HttpStatusCode status, RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws java.io.IOException {
        if (status.isSameCodeAs(HttpStatusCode.valueOf(404))) {
            ProblemDetail pd = response.bodyTo(ProblemDetail.class);
            String detail = pd != null && pd.getDetail() != null ? pd.getDetail() : "Task not found";
            throw new TaskNotFoundException(detail);
        }
        if (status.is5xxServerError() || status.isSameCodeAs(HttpStatusCode.valueOf(429))) {
            throw new ExternalApiException("External API error with status: " + status.value());
        }
        throw new ExternalApiException("Unexpected status code: " + status.value());
    }

    private void checkContentType(MediaType contentType, RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws java.io.IOException {
        if (contentType != null && contentType.includes(MediaType.TEXT_HTML)) {
            String body = response.bodyTo(String.class);
            String limitedBody = body != null ? body.substring(0, Math.min(body.length(), 200)) : "empty body";
            log.error("Received unexpected HTML response: {}", limitedBody);
            throw new ExternalApiException("Unexpected Content-Type: " + contentType);
        }
    }
}
