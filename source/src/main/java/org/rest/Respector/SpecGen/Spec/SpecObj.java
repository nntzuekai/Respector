package org.rest.Respector.SpecGen.Spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.rest.Respector.SpecGen.Spec.Components.SpecComponentsObj;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.EndPointOperationObj;

import com.google.gson.annotations.SerializedName;

public class SpecObj {
  @SerializedName("openapi")
  public final String OpenAPIVersion="3.0.0";

  public final List<ServerObj> servers=Arrays.asList(new ServerObj());

  public SpecInfoObj info=new SpecInfoObj();

  public TreeMap<String, PathItemObj> paths;

  public SpecComponentsObj components=new SpecComponentsObj();

  public SpecObj() {
    this.paths = new TreeMap<>();
  }

  public PathItemObj createOrFindPathItemObj(String path){
    return this.paths.computeIfAbsent(path, e -> new PathItemObj(path, this));
  }

  public EndPointOperationObj createEndPointOperation(String path, String HttpOp, String operationId) {
    PathItemObj pathItem=createOrFindPathItemObj(path);
    return pathItem.createEndPointOperation(HttpOp, operationId);
  }

  public boolean insertEndPointOperationTo(String path, String HttpOp, EndPointOperationObj epp) {
    PathItemObj pathItem=createOrFindPathItemObj(path);
    return pathItem.insertEndPointOperationTo(HttpOp, epp);
  }

  // @Deprecated
  // public EndPointOperationObj addEndPointOperation(EndPointOperationObj endPoint){
  //   PathItemObj pathItem=this.paths.computeIfAbsent(endPoint.path, e -> new PathItemObj(endPoint.path, this));
  //   return pathItem.addEndPointOperation(endPoint);
  // }
}
