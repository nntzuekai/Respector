package org.rest.Respector.PathRecord;

import soot.Value;

public class ParamInfo {
  public int idx;
  public Value rr;

  public ParamInfo(int idx, Value rr) {
    this.idx = idx;
    this.rr = rr;
  }
}
