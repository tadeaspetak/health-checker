package com.tadeaspetak.health_checker.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;

public class Db {
  final public JDBCPool pool;
  final public Services services = new Services();

  public Db(Vertx vertx, String dbPath, Handler<AsyncResult<Void>> next) {
    pool = JDBCPool.pool(vertx, new JsonObject()
      .put("url", "jdbc:sqlite:" + dbPath)
      .put("max_pool_size", 30)
    );
    pool.query("SELECT * FROM services;")
      .execute()
      .onFailure(e -> initDb(initialized -> {
        if (initialized.succeeded()) {
          initCollections();
          next.handle(Future.succeededFuture());
        } else {
          next.handle(Future.failedFuture(e));
        }
      }))
      .onSuccess(rows -> {
        initCollections();
        next.handle(Future.succeededFuture());
      });
  }

  private void initDb(Handler<AsyncResult<Void>> next) {
    pool
      .query("CREATE TABLE services (id INTEGER PRIMARY KEY, name TEXT, url TEXT, user_id INTEGER, status TEXT)")
      .execute()
      .onFailure(e -> next.handle(Future.failedFuture((e.getCause()))))
      .onSuccess(rows -> next.handle(Future.succeededFuture()));
  }

  private void initCollections() {
    this.services.init(this.pool);
  }
}
