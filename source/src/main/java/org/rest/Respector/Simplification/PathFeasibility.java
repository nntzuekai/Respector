package org.rest.Respector.Simplification;

import org.rest.Respector.PathCondExtract.PathConstraint;

import com.microsoft.z3.Status;

public class PathFeasibility extends SootToZ3 {
  public PathFeasibility(PathConstraint path){
    super(path.conds);


    this.status=solver.check();

    this.ctx.close();
  }

  public boolean check(){
    return status != Status.UNSATISFIABLE;
  }
}
