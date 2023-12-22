package org.rest.Respector.SpecGen.Spec.Path;

import java.util.TreeMap;

import org.rest.Respector.SpecGen.Spec.SpecObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.EndPointOperationObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathItemObj {
  public String path;
  public final transient SpecObj parentSpec;

  public TreeMap<String, EndPointOperationObj> operations;

  private static Logger logger = LoggerFactory.getLogger(PathItemObj.class);

  // public PathItemObj() {
  //   this.operations=new TreeMap<>();
  // }

  public PathItemObj(String path, SpecObj enclosingSpec) {
    this.path=path;
    this.parentSpec=enclosingSpec;
    this.operations=new TreeMap<>();
  }

  // public PathItemObj(EndPointOperationObj singleEndPoint) {
  //   this.operations=new HashMap<>();
  //   this.operations.put(singleEndPoint.HttpOp, singleEndPoint);
  // }

  public boolean insertEndPointOperationTo(String HttpOp, EndPointOperationObj endPointOp) {
    if(this.operations.containsKey(HttpOp)){
      // throw new RuntimeException("two end points have the same path and HTTP op?");
      logger.debug(String.format("More than one endpoints are bound to %s %s", HttpOp, this.path));

      return false;
    }

    this.operations.put(HttpOp, endPointOp);
    return true;
  }

  public EndPointOperationObj createEndPointOperation(String HttpOp, String operationId) {
    if(this.operations.containsKey(HttpOp)){
      // throw new RuntimeException("two end points have the same path and HTTP op?");
      logger.debug(String.format("More than one endpoints are bound to %s %s", HttpOp, this.path));

      ///TODO: should we overwrite?
      return null;
    }

    EndPointOperationObj epp=new EndPointOperationObj(this.path, HttpOp, this, operationId);
    this.operations.put(HttpOp, epp);

    return epp;
  }
}
