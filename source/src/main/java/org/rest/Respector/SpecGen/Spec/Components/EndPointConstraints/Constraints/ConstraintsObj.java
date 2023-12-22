package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints;

import java.util.ArrayList;

public class ConstraintsObj {
  public ArrayList<String> constraints;

  public boolean addCond(String cond){
    if(this.constraints==null){
      this.constraints=new ArrayList<>();
    }

    return constraints.add(cond);
  }
}
