package org.rest.Respector.PathCondExtract;

import soot.Type;
import soot.jimple.internal.JimpleLocal;

public class EndPointParameter extends JimpleLocal{
  // public String _name;
  public boolean isEPP;
  public int idx;

  public EndPointParameter(String name, boolean isEPP, int idx, Type paramType){
    // super(String.format("{%s}", name), paramType);
    super(name, paramType);
    this.isEPP=isEPP;
    this.idx=idx;
    // this._name=name;
  }

  @Override
  public String toString() {
    return String.format("{%s}", getName());
  }
}
