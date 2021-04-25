package com.tadeaspetak.health_checker.db;

import com.tadeaspetak.health_checker.models.Service;
import com.tadeaspetak.health_checker.models.ServiceStatus;
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
  private JDBCPool pool;

  public void init(JDBCPool pool) {
    this.pool = pool;
  }

  public void add(Service service, Handler<AsyncResult<Integer>> next) {
    pool
      .preparedQuery("INSERT INTO services (name, url, user_id) VALUES (?, ?, 1) RETURNING *")
      .execute(Tuple.of(service.name, service.url))
      .onFailure(e -> {
        System.err.println(e.getMessage());
        next.handle(Future.failedFuture("Could not add the service."));
      })
      .onSuccess(rows -> {
        if (rows.size() < 1) {
          next.handle(Future.failedFuture("Could not add the service."));
        } else {
          next.handle(Future.succeededFuture(rows.iterator().next().getInteger("id")));
        }
      });
  }

  public void updateStatus(long id, ServiceStatus status, Handler<AsyncResult<Void>> next) {
    pool
      .preparedQuery("UPDATE services SET status = ? WHERE id = ?")
      .execute(Tuple.of(status, id))
      .onFailure(e -> {
        System.err.println(e.getMessage());
        next.handle(Future.failedFuture("Could not update the service."));
      })
      .onSuccess(rows -> {
        next.handle(Future.succeededFuture());

      });
  }

  public void getAll(Handler<AsyncResult<List<JsonObject>>> next) {
    pool
      .preparedQuery("SELECT * FROM services")
      .execute()
      .onFailure(e -> {
        System.err.println(e.getMessage());
        next.handle(Future.failedFuture("Could not fetch service."));
      })
      .onSuccess(rows -> {
        List<JsonObject> objects = new ArrayList<>();
        for (Row row : rows) objects.add(row.toJson());
        next.handle(Future.succeededFuture(objects));
      });
  }

  public void getOne(int id, Handler<AsyncResult<JsonObject>> next) {
    pool
      .preparedQuery("SELECT * FROM services WHERE id = ?")
      .execute(Tuple.of(id))
      .onFailure(e -> {
        System.err.println(e.getMessage());
        next.handle(Future.failedFuture("Could not fetch the service."));
      })
      .onSuccess(rows -> {
        if (rows.size() < 1) {
          next.handle(Future.failedFuture("No such service."));
        } else {
          next.handle(Future.succeededFuture(rows.iterator().next().toJson()));
        }
      });
  }

  public void delete(int id, Handler<AsyncResult<Integer>> next) {
    pool
      .preparedQuery("DELETE FROM services WHERE id = ? RETURNING *")
      .execute(Tuple.of(id))
      .onFailure(e -> {
        System.err.println(e.getMessage());
        next.handle(Future.failedFuture("Could not delete the service."));
      })
      .onSuccess(rows -> {
        if (rows.size() < 1) {
          next.handle(Future.failedFuture("No such service."));
        } else {
          next.handle(Future.succeededFuture(rows.iterator().next().getInteger("id")));
        }
      });
  }
}
