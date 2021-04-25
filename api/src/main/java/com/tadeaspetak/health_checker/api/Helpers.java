package com.tadeaspetak.health_checker.api;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

public class Helpers {

  public static Optional<Integer> parseIdOrFail(RoutingContext ctx) {
    String unparsed = ctx.pathParam("id");
    if (unparsed == null) {
      respond(ctx, 400, "No `id`.");
      return Optional.empty();
    }

    try {
      return Optional.of(Integer.parseInt(unparsed));
    } catch (NumberFormatException e) {
      respond(ctx, 400, "`id` must be numeric.");
      return Optional.empty();
    }
  }

  public static void respond(RoutingContext ctx, int code, JsonObject json) {
    ctx.response().setStatusCode(code).putHeader("Content-Type", "application/json; charset=UTF8").end(json.encodePrettily());
  }

  public static void respond(RoutingContext ctx, int code, String msg) {
    JsonObject json = new JsonObject().put("message", msg);
    respond(ctx, code, json);
  }
}
