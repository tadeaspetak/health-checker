package com.tadeaspetak.health_checker.models;

public enum ServiceStatus {
  OK("ok"), FAIL("fail");

  private final String text;

  ServiceStatus(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
