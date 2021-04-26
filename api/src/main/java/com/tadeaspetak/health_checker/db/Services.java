package com.tadeaspetak.health_checker.db;

import com.google.gson.Gson;
import com.tadeaspetak.health_checker.models.Service;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

public class Services {
  final private static Gson gson = new Gson();
  private JDBCPool pool;

  public void init(JDBCPool pool) {
    this.pool = pool;
  }

  public void add(Service service, Handler<AsyncResult<Service>> next) {
    pool.preparedQuery("INSERT INTO services (url) VALUES (?) RETURNING *").execute(Tuple.of(service.url))
      .onFailure(e -> {
        System.err.println(e.getMessage());
        next.handle(Future.failedFuture("Could not add the service."));
      }).onSuccess(rows -> {
      if (rows.size() < 1) {
        next.handle(Future.failedFuture("Could not add the service."));
      } else {
        Service added = gson.fromJson(rows.iterator().next().toJson().toString(), Service.class);
        next.handle(Future.succeededFuture(added));
      }
    });
  }

  public void updateStatus(long id, Service.Status status, Handler<AsyncResult<Service>> next) {
    pool.preparedQuery("UPDATE services SET status = ? WHERE id = ? RETURNING *").execute(Tuple.of(status, id)).onFailure(e -> {
      System.err.println(e.getMessage());
      next.handle(Future.failedFuture("Could not update the service."));
    }).onSuccess(rows -> {
      Row row = rows.iterator().next();
      Service updated = gson.fromJson(row.toJson().toString(), Service.class);
      System.out.println(String.format("updated row: %s, %s", updated.url, updated.status));
      next.handle(Future.succeededFuture(updated));
    });
  }

  public void updateUrl(long id, String url, Handler<AsyncResult<Service>> next) {
    pool.preparedQuery("UPDATE services SET url = ? WHERE id = ? RETURNING *").execute(Tuple.of(url, id)).onFailure(e -> {
      System.err.println(e.getMessage());
      next.handle(Future.failedFuture("Could not update the service."));
    }).onSuccess(rows -> {
      Service updated = gson.fromJson(rows.iterator().next().toJson().toString(), Service.class);
      next.handle(Future.succeededFuture(updated));
    });
  }

  public void getAll(Handler<AsyncResult<List<JsonObject>>> next) {
    pool.preparedQuery("SELECT * FROM services").execute().onFailure(e -> {
      System.err.println(e.getMessage());
      next.handle(Future.failedFuture("Could not fetch service."));
    }).onSuccess(rows -> {
      List<JsonObject> objects = new ArrayList<>();
      for (Row row : rows)
        objects.add(row.toJson());
      next.handle(Future.succeededFuture(objects));
    });
  }

  public void getOne(int id, Handler<AsyncResult<JsonObject>> next) {
    pool.preparedQuery("SELECT * FROM services WHERE id = ?").execute(Tuple.of(id)).onFailure(e -> {
      System.err.println(e.getMessage());
      next.handle(Future.failedFuture("Could not fetch the service."));
    }).onSuccess(rows -> {
      if (rows.size() < 1) {
        next.handle(Future.failedFuture("No such service."));
      } else {
        next.handle(Future.succeededFuture(rows.iterator().next().toJson()));
      }
    });
  }

  public void delete(int id, Handler<AsyncResult<Integer>> next) {
    pool.preparedQuery("DELETE FROM services WHERE id = ? RETURNING *").execute(Tuple.of(id)).onFailure(e -> {
      System.err.println(e.getMessage());
      next.handle(Future.failedFuture("Could not delete the service."));
    }).onSuccess(rows -> {
      if (rows.size() < 1) {
        next.handle(Future.failedFuture("No such service."));
      } else {
        next.handle(Future.succeededFuture(rows.iterator().next().getInteger("id")));
      }
    });
  }
}
