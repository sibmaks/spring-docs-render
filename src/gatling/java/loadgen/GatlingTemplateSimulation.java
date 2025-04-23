package loadgen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class GatlingTemplateSimulation extends Simulation {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    private static final int MAX_REQUESTS = 250_000;
    private static final Random rnd = new Random();
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Map<GatlingScenario, String> templateIds = new ConcurrentHashMap<>();
    private final Queue<Long> requestTimes = new ConcurrentLinkedQueue<>();

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .contentTypeHeader("application/json");

    private final ScenarioBuilder scenarioBuilder = scenario("TemplateRenderScenario")
            .asLongAs(session -> totalRequests.get() < MAX_REQUESTS)
            .on(
                    exec(session -> {
                        // pick random scenario and its templateId
                        var scenarios = GatlingScenario.values();
                        var scenario = scenarios[rnd.nextInt(scenarios.length)];
                        session = session.set("templateName", scenario.name());
                        session = session.set("templateId", templateIds.get(scenario));

                        // generate input model
                        var inputModel = scenario.modelGenerator.apply(session);
                        try {
                            session = session.set("inputModel", OBJECT_MAPPER.writeValueAsBytes(inputModel));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        return session;
                    })
                            .exec(beginTracing("startTimeRender"))
                            .exec(http("Render Template - #{templateName}")
                                    .post(session -> "/api/template/" + session.getString("templateId") + "/render")
                                    .body(ByteArrayBody(session -> session.get("inputModel")))
                                    .asJson()
                            )
                            .exec(finishTracing("startTimeRender"))
            );

    {
        setUp(
                scenarioBuilder.injectOpen(rampUsers(10).during(1))
        ).protocols(httpProtocol);
    }

    private Function<Session, Session> beginTracing(String key) {
        return session -> session.set(key, System.currentTimeMillis());
    }

    private Function<Session, Session> finishTracing(String startTimeKey) {
        return session -> {
            long duration = System.currentTimeMillis() - session.getLong(startTimeKey);
            logRequestTime(duration);
            return session;
        };
    }

    private void logRequestTime(long time) {
        totalRequests.incrementAndGet();
        requestTimes.add(time);
        int current = totalRequests.get();
        if (current % 10000 != 0) {
            return;
        }
        System.out.println("\n== Stats at " + current + " requests ==");
        var times = new ArrayList<>(requestTimes);

        double avg = times.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        double variance = times.stream()
                .mapToDouble(t -> Math.pow(t - avg, 2))
                .average().orElse(0.0);
        System.out.printf("Avg: %.2f ms, Variance: %.2f\n", avg, variance);
    }

    @Override
    public void before() {
        for (var scenario : GatlingScenario.values()) {
            try {
                var req = Map.of(
                        "code", UUID.randomUUID().toString(),
                        "template", scenario.template,
                        "engine", "JASPER"
                );
                var body = OBJECT_MAPPER.writeValueAsBytes(req);
                var request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/template/"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build();
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                templateIds.put(scenario, response.body());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to create template for scenario: " + scenario, e);
            }
        }
    }

    @Override
    public void after() {
        deleteAllTemplates();
    }

    private void deleteAllTemplates() {
        for (var id : templateIds.values()) {
            try {
                var request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/template/" + id))
                        .DELETE()
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to delete templateId " + id + ": " + e.getMessage());
            }
        }
        System.out.println("All templates deleted.");
    }
}
