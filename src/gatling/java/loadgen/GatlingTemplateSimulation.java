package loadgen;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class GatlingTemplateSimulation extends Simulation {
    // Shared counter for total requests
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    private static final int MAX_REQUESTS = 250_000;
    private static final Random rnd = new Random();

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

                            .exec(http("Create Template - #{scenario.templateFilePath}")
                                    .post("/api/template/")
                                    .body(RawFileBody("data/templates/#{scenario.templateFilePath}"))
                                    .asJson()
                                    .check(jsonPath("$").saveAs("templateId"))
                            )

                            .exec(http("Render Template with #{scenario.inputFilePath}")
                                    .post(session -> "/api/template/" + session.getString("templateId") + "/render")
                                    .body(RawFileBody("data/input/#{scenario.inputFilePath}"))
                                    .asJson()
                            )

                            .exec(session -> {
                                totalRequests.incrementAndGet();
                                return session;
                            })

                            .exec(http("Delete Template")
                                    .delete(session -> "/api/template/" + session.getString("templateId"))
                            )
            );

    {
        setUp(
                scn.injectOpen(rampUsers(10).during(1))
        ).protocols(httpProtocol);
    }
}
