package com.tadeaspetak.health_checker;

import com.tadeaspetak.health_checker.db.Db;
import com.tadeaspetak.health_checker.models.Service;
import io.vertx.core.*;
import io.vertx.ext.web.client.WebClient;

import java.util.List;

public class Checker {
  final private Db db;
  final private WebClient webClient;

  public Checker(Vertx vertx, Db db) {
    this.db = db;

    webClient = WebClient.create(vertx);
  }

  public void checkAndUpdate(List<Service> services) {
    for (Service service : services) {
      checkAndUpdate(service, ar -> {
      });
    }
  }

  public void checkAndUpdate(Service service, Handler<AsyncResult<Service>> next) {
    try {
      checkUrl(service.url, statusCheck -> {
        if (statusCheck.succeeded()) {
          Service.Status status = statusCheck.result() ? Service.Status.ok : Service.Status.fail;
          db.services.updateStatus(service.id, status, ar -> {
            System.out.println(ar.result().status);
            if (ar.succeeded()) next.handle(Future.succeededFuture(ar.result()));
          });
        } else {
          System.err.println(statusCheck.cause().getMessage());
          db.services.updateStatus(service.id, Service.Status.fail, ar -> {
            if (ar.succeeded()) next.handle(Future.succeededFuture(ar.result()));
          });
        }
      });
    } catch (VertxException e) {
      db.services.updateStatus(service.id, Service.Status.invalid, ar -> {
        if (ar.succeeded()) next.handle(Future.succeededFuture(ar.result()));
      });
    }
  }

  private void checkUrl(String url, Handler<AsyncResult<Boolean>> next) {
    webClient.getAbs(url)
      .send()
      .onSuccess(response -> {
        if (response.statusCode() == 200) {
          next.handle(Future.succeededFuture(true));
        } else {
          next.handle(Future.succeededFuture(false));
        }
      })
      .onFailure(e -> next.handle(Future.failedFuture(e)));
  }
}
