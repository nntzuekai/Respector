package org.rest.Respector.archive;


public class MyAppUtil {
  public void reportError(String variableName,
                          int lineNumber,
                          String targetState,
                          String sourceState) {
    System.out.println("Error on " + variableName + " at " + lineNumber + "\n"
                       + sourceState + " -> " + targetState + "\n");
  }
}

