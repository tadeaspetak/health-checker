package com.tadeaspetak.health_checker;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.reactiverse.junit5.web.WebClientOptionsInject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.Arrays;

import static io.reactiverse.junit5.web.TestRequest.*;

@ExtendWith({
  VertxExtension.class,
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMainVerticle {
  static int port = 8989;
  static String dbPath = "test.com.tadeaspetak.health_checker.db";

  DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject()
    .put("com.tadeaspetak.health_checker.db.path", dbPath)
    .put("http.port", port)
  );

  @WebClientOptionsInject
  public WebClientOptions opts = new WebClientOptions()
    .setDefaultPort(port)
    .setDefaultHost("localhost");

  String deploymentId;

  @BeforeAll
  static void prep() {
    File file = new File(dbPath);
    if (file.exists()) file.delete();
  }

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    testContext
      .assertComplete(vertx.deployVerticle(new MainVerticle(), options))
      .onComplete(result -> {
        deploymentId = result.result();
        testContext.completeNow();
      });
  }

  @AfterEach
  void tearDown(Vertx vertx, VertxTestContext testContext) {
    testContext
      .assertComplete(vertx.undeploy(deploymentId))
      .onComplete(ar -> testContext.completeNow());
  }

  final JsonObject newServiceInput = new JsonObject().put("name", "Amazon").put("url", "https://google.com");
  final JsonObject newServiceOutput = newServiceInput.put("id", 1).put("user_id", 1).put("state", null);

  @Test
  @Order(1)
  public void getServices(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/services"))
      .expect(
        statusCode(200),
        jsonBodyResponse(new JsonObject().put("services", new JsonArray()))
      )
      .send(testContext);
  }

  @Test
  @Order(2)
  public void addService(WebClient client, VertxTestContext testContext) {
    final Checkpoint checkpoint = testContext.checkpoint(2);

    testRequest(client.post("/services"))
      .expect(
        statusCode(202),
        jsonBodyResponse(new JsonObject().put("message", "Added service.").put("id", 1))
      )
      .sendJson(newServiceInput, testContext, checkpoint)
      .onComplete(ar ->
        testRequest(client.get("/services/1"))
          .expect(
            statusCode(200),
            jsonBodyResponse(newServiceOutput)
          )
          .send(testContext, checkpoint)
      );
  }

  @Test
  @Order(3)
  public void getServiceBadRequest(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/services/bad"))
      .expect(
        statusCode(400),
        jsonBodyResponse(new JsonObject().put("message", "`id` must be numeric."))
      )
      .send(testContext);
  }

  @Test
  @Order(4)
  public void getServiceNonExisting(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/services/2"))
      .expect(
        statusCode(404),
        jsonBodyResponse(new JsonObject().put("message", "Service 2 not found."))
      )
      .send(testContext);
  }

  @Test
  @Order(5)
  public void getServices2(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/services"))
      .expect(
        statusCode(200),
        jsonBodyResponse(new JsonObject().put("services", new JsonArray(Arrays.asList(newServiceOutput))))
      )
      .send(testContext);
  }
}
