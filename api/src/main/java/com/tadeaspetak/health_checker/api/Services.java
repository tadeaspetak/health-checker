package com.tadeaspetak.health_checker.api;

import com.google.gson.Gson;
import com.tadeaspetak.health_checker.Checker;
import com.tadeaspetak.health_checker.models.Service;
import com.tadeaspetak.health_checker.db.Db;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;

public class Services {
  final private Gson gson = new Gson();
  private static final String basePath = "/services";

  Checker checker;
  Db db;

  public Services(Db db, Router router, Checker checker) {
    this.db = db;
    this.checker = checker;

    router.get(String.format("%s", basePath)).handler(this::getAll);
    router.post(String.format("%s", basePath)).handler(BodyHandler.create()).handler(this::add);
    router.get(String.format("%s/:id", basePath)).handler(this::getOne);
    router.delete(String.format("%s/:id", basePath)).handler(this::delete);
    router.put(String.format("%s/:id", basePath)).handler(BodyHandler.create()).handler(this::updateUrl);
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
    db.services.add(gson.fromJson(ctx.getBodyAsJson().toString(), Service.class), (added) -> {
      if (added.failed()) {
        Helpers.respond(ctx, 500, "Failed to add service.");
      } else {
        Service service = added.result();
        this.checker.checkAndUpdate(service, checked -> {
          if (checked.succeeded()) {
            System.out.println("succeeded immediate check");
            Helpers.respond(ctx, 202, new JsonObject().put("message", "Added service.").put("service", checked.result()));
          } else {
            System.out.println("failed immediate check");
            Helpers.respond(ctx, 202, new JsonObject().put("message", "Added service.").put("service", service));
          }
        });

      }
    });
  }

  private void updateUrl(RoutingContext ctx) {
    Optional<Integer> id = Helpers.parseIdOrFail(ctx);
    if (id.isEmpty()) return;

    db.services.updateUrl(id.get(), ctx.getBodyAsJson().getString("url"), (updated) -> {
      if (updated.failed()) {
        Helpers.respond(ctx, 500, "Failed to update service.");
      } else {
        Helpers.respond(ctx, 202, new JsonObject().put("message", "Updated the service.").put("service", updated.result()));
      }
    });
  }
}
