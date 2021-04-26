package com.tadeaspetak.health_checker.models;

import com.google.gson.annotations.SerializedName;

public class Service {
  public enum Status {
    ok,
    fail,
    invalid
  }


  public int id;
  public String url;
  public Status status;
}
