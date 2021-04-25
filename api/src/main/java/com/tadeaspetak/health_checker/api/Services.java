package com.tadeaspetak.health_checker.api;

import com.google.gson.Gson;
import com.tadeaspetak.health_checker.models.Service;
import com.tadeaspetak.health_checker.db.Db;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;

public class Services {
  private static final  String basePath = "/services";

  Db db;
  final private Gson gson = new Gson();

  public Services(Db db, Router router) {
    this.db = db;

    router.get(String.format("%s", basePath)).handler(this::getAll);
    router.post(String.format("%s", basePath)).handler(BodyHandler.create()).handler(this::add);
    router.get(String.format("%s/:id", basePath)).handler(this::getOne);
    router.delete(String.format("%s/:id", basePath)).handler(this::delete);
  }

  private void getOne(RoutingContext ctx) {
    Optional<Integer> id = Helpers.parseIdOrFail(ctx);
    if (id.isEmpty()) return;

    db.services.getOne(id.get(), service -> {
      if (service.failed()) {
        Helpers.respond(ctx, 404, String.format("Service %s not found.", id.get()));
      } else {
        ctx.json(service.result());
      }
    });
  }

  private void delete(RoutingContext ctx) {
    Optional<Integer> id = Helpers.parseIdOrFail(ctx);
    if (id.isEmpty()) return;

    db.services.delete(id.get(), deleted -> {
      if (deleted.failed()) {
        Helpers.respond(ctx, 500, deleted.cause().getMessage());
      } else {
        Helpers.respond(ctx, 200, new JsonObject().put("message",String.format("Deleted service %s.", id.get())).put("id", id.get()));
      }
    });
  }

  private void getAll(RoutingContext ctx) {
    db.services.getAll((services -> {
      if (services.failed()) {
        Helpers.respond(ctx, 500, "Cannot fetch services.");
      } else {
        ctx.json(new JsonObject().put("services", new JsonArray(services.result())));
      }
    }));
  }

  private void add(RoutingContext ctx) {
    db.services.add(gson.fromJson(ctx.getBodyAsJson().toString(), Service.class), (id) -> {
      if (id.failed()) {
        Helpers.respond(ctx, 500, "Failed to add service.");
      } else {
        Helpers.respond(ctx, 202, new JsonObject().put("message", "Added service.").put("id", id.result()));
      }
    });
  }
}
