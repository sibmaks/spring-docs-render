package loadgen;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class GatlingTemplateSimulation extends Simulation {
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    private static final int MAX_REQUESTS = 250_000;
    private static final Random rnd = new Random();

    private static final Queue<Long> requestTimes = new ConcurrentLinkedQueue<>();
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("TemplateRenderScenario")
            .asLongAs(session -> totalRequests.get() < MAX_REQUESTS).on(
                    exec(session -> {
                        var scenarios = GatlingScenario.values();
                        var scenario = scenarios[rnd.nextInt(scenarios.length)];
                        return session.set("scenario", scenario);
                    })
                            .exec(beginTracing("startTimeCreate"))
                            .exec(http("Create Template - #{scenario.templateFilePath}")
                                    .post("/api/template/")
                                    .body(RawFileBody(Path.of("data", "templates", "#{scenario.templateFilePath}").toString()))
                                    .asJson()
                                    .check(jsonPath("$").saveAs("templateId"))
                            )
                            .exec(finishTracing("startTimeCreate"))

                            .exec(beginTracing("startTimeRender"))
                            .exec(http("Render Template with #{scenario.inputFilePath}")
                                    .post(session -> "/api/template/" + session.getString("templateId") + "/render")
                                    .body(RawFileBody(Path.of("data", "input", "#{scenario.inputFilePath}").toString()))
                                    .asJson()
                            )
                            .exec(finishTracing("startTimeRender"))

                            .exec(beginTracing("startTimeDelete"))
                            .exec(http("Delete Template")
                                    .delete(session -> "/api/template/" + session.getString("templateId"))
                            )
                            .exec(finishTracing("startTimeDelete"))
            );

    {
        setUp(
                scn.injectOpen(rampUsers(10).during(1))
        ).protocols(httpProtocol);
    }

    private static Function<Session, Session> finishTracing(String caption) {
        return session -> {
            long duration = System.currentTimeMillis() - session.getLong(caption);
            logRequestTime(duration);
            return session;
        };
    }

    private static Function<Session, Session> beginTracing(String startTimeDelete) {
        return session -> session.set(startTimeDelete, System.currentTimeMillis());
    }

    private static void logRequestTime(long time) {
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
}
