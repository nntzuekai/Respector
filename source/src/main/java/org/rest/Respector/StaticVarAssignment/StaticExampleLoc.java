package org.rest.Respector.StaticVarAssignment;

import soot.Value;

public class StaticExampleLoc {
  public Value rhs;
  public String file;
  public int lineNumber;


  public StaticExampleLoc(Value rhs, String file, int lineNumber) {
    this.rhs = rhs;
    this.file = file;
    this.lineNumber = lineNumber;
  }
}
