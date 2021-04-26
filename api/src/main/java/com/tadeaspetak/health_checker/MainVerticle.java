package com.tadeaspetak.health_checker;

import com.google.gson.Gson;
import com.tadeaspetak.health_checker.api.Services;
import com.tadeaspetak.health_checker.db.Db;
import com.tadeaspetak.health_checker.models.Service;
import io.vertx.core.*;
import io.vertx.ext.web.Router;

import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  final Gson gson = new Gson();

  private Checker checker;
  private Db db;
  private Timer timer;

  @Override
  public void start(Promise<Void> startPromise) {
    this.db = new Db(vertx, config().getString("db.path", "data.db"), initialized -> {
      if (initialized.succeeded()) {
        run(startPromise);
      } else {
        System.out.println(initialized.cause().getMessage());
        System.exit(1);
      }
    });
  }

  private void run(Promise<Void> startPromise) {
    this.checker = new Checker(vertx, db);

    Router apiRouter = Router.router(vertx);
    new Services(this.db, apiRouter, checker);

    Router router = Router.router(vertx);
    router.mountSubRouter("/api", apiRouter);

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(config().getInteger("http.port", 8080))
      .<Void>mapEmpty()
      .onComplete(startPromise);


    timer = new Timer(vertx, 5000, id -> db.services.getAll(servicesAr -> {
      if (servicesAr.succeeded()) {
        checker.checkAndUpdate(servicesAr.result().stream().map(o -> gson.fromJson(o.toString(), Service.class)).collect(Collectors.toList()));
      } else {
        System.out.println(servicesAr.cause().getMessage());
      }
    }));
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    if (timer != null) timer.stop();
  }

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}
