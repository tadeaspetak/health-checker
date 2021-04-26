package com.tadeaspetak.health_checker;

import io.vertx.core.*;
import io.vertx.core.Handler;

public class Timer {
  private long id;

  final private Vertx vertx;
  final private long delay;
  final private Handler<Long> handler;

  public Timer(Vertx vertx, long delay, Handler<Long> handler) {
    this.vertx = vertx;
    this.delay = delay;
    this.handler = handler;

    this.start();
  }

  public void restart() {
    this.start();
  }

  private void start() {
    if (id != 0) this.stop();
    id = this.vertx.setPeriodic(this.delay, this.handler);
  }

  public void stop() {
    if (this.vertx.cancelTimer(id)) id = 0;
  }
}
