package com.tadeaspetak.health_checker;

import com.google.gson.Gson;
import com.tadeaspetak.health_checker.models.Service;
import com.tadeaspetak.health_checker.models.ServiceStatus;
import io.vertx.core.*;
import io.vertx.ext.web.Router;

import com.tadeaspetak.health_checker.api.Services;
import com.tadeaspetak.health_checker.db.Db;
import io.vertx.ext.web.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  final Gson gson = new Gson();

  Db db;
  Timer timer;
  WebClient webClient;

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
    Router router = Router.router(vertx);
    new Services(this.db, router);

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(config().getInteger("http.port", 8080))
      .<Void>mapEmpty()
      .onComplete(startPromise);


    webClient = WebClient.create(vertx);

    timer = new Timer(vertx, 5000, new Handler<>() {
      @Override
      public void handle(Long id) {
        db.services.getAll(servicesAr -> {
          if (servicesAr.succeeded()) {
            List<Service> services = servicesAr.result().stream().map(o -> gson.fromJson(o.toString(), Service.class)).collect(Collectors.toList());
            for (Service service : services) {
              check(service.url, result -> {
                if (result.succeeded()) {
                  ServiceStatus status = result.result() ? ServiceStatus.OK : ServiceStatus.FAIL;
                  db.services.updateStatus(service.id, status, ar -> {});
                } else {
                  db.services.updateStatus(service.id, ServiceStatus.FAIL, ar -> {});
                }
              });
            }
          } else {
            System.out.println(servicesAr.cause().getMessage());
          }
        });
      }
    });
  }

  public void check(String url, Handler<AsyncResult<Boolean>> next) {
    webClient.getAbs(url)
      .send()
      .onSuccess(response -> {
        if (response.statusCode() == 200) {
          next.handle(Future.succeededFuture(true));
        } else {
          next.handle(Future.succeededFuture(false));
        }
      })
      .onFailure(e -> {
        next.handle(Future.failedFuture(e));
      });
  }

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}
