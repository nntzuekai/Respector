package org.rest.Respector.SpecGen.Spec.Path.EndPointOp;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.rest.Respector.EndPointRecog.EndPointParamInfo;
import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.rest.Respector.SpecGen.Spec.ReferenceObj;
import org.rest.Respector.SpecGen.Spec.SpecObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites.GlobalWriteObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints.XEndPointConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.ParameterObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.ParamSchemaObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.RequestBody.RequestBodyObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Responses.ResponseObj;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class EndPointOperationObj {
  public transient String path;
  public transient String HttpOp;
  // public transient SpecObj enclosingSpec;
  public transient String componentParentPath;
  // public transient boolean onlyInvalid;

  public final transient PathItemObj parentPathItem;
  public transient XEndPointConstraintsObj xEPPEntry;

  public final String description="";
  public String operationId;

  public ArrayList<ParameterObj> parameters;
  public TreeMap<String, ResponseObj> responses;

  protected RequestBodyObj requestBody=null;

  @SerializedName("x-endpoint-constraints")
  public ReferenceObj xConstraints;

  @SerializedName("x-raw-valid")
  public ArrayList<ArrayList<String>> rawValid=null;
  @SerializedName("x-raw-invalid")
  public ArrayList<ArrayList<String>> rawInvalid=null;

  public JsonElement _bodyParams=null;

  
  // only copy parameters
  public EndPointOperationObj(EndPointOperationObj rhs) {
    this.operationId=rhs.operationId;

    this.path=rhs.path;
    this.HttpOp=rhs.HttpOp;
    this.componentParentPath=rhs.componentParentPath;
    this.parentPathItem=rhs.parentPathItem;
    this.xEPPEntry=rhs.xEPPEntry;

    this.parameters=new ArrayList<>(rhs.parameters);
    this.responses=rhs.responses;

    this.requestBody=rhs.requestBody;
    this.xConstraints=rhs.xConstraints;
    this.rawValid=rhs.rawValid;
    this.rawInvalid=rhs.rawInvalid;
  }

  public EndPointOperationObj(String path, String httpOp, PathItemObj parentPathItem, ArrayList<ParameterObj> parameters,
      TreeMap<String, ResponseObj> responses, ReferenceObj xConstraints, ArrayList<ArrayList<String>> rawValid,
      ArrayList<ArrayList<String>> rawInvalid, String operationId) {
    this.operationId=operationId;
    this.path = path;
    this.HttpOp = httpOp;
    this.parentPathItem=parentPathItem;
    this.parameters = parameters;
    this.responses = responses;
    this.xConstraints = xConstraints;
    this.rawValid = rawValid;
    this.rawInvalid = rawInvalid;
  }

  public EndPointOperationObj(String path, String HttpOp, PathItemObj parentPathItem, String operationId) {
    this.operationId=operationId;

    this.path = path;
    this.HttpOp = HttpOp;
    this.parentPathItem=parentPathItem;

    this.parameters=new ArrayList<>();
    this.responses=new TreeMap<>();

    // addResponse(new ResponseObj("200"));
    addResponse(new ResponseObj("default", "others"));
  }

  public RequestBodyObj getRequestBody() {
    if(requestBody==null){
      requestBody=new RequestBodyObj();
    }
    return requestBody;
  }

  protected static final List<String> opsAllowingRequestBody=List.of("post", "put", "patch");

  ///TODO: Reconsider this. We should allow all ops to have the request body as it is not forbidden as per RFCs.
  boolean canHaveRequestBody(){
    return EndPointOperationObj.opsAllowingRequestBody.contains(this.HttpOp);
  }

  // @Deprecated
  // public boolean addParameterObj(ParameterObj parameterObj){
  //   parameterObj.bindXParameterConstraints(this.getOrCreateXEndPointConstraintObj(), this.componentParentPath);
  //   return this.parameters.add(parameterObj);
  // }

  public ParameterObj createParameterObj(EndPointParamInfo EPPInfo){
    ParameterObj parameterObj=new ParameterObj(EPPInfo.name, EPPInfo.in, EPPInfo.required, this);

    this.parameters.add(parameterObj);
    return parameterObj;
  }

  public ParamSchemaObj createRequestBodyParamObj(EndPointParamInfo EPPInfo){
    if(this.canHaveRequestBody()){
      RequestBodyObj reqBody=getRequestBody();

      return reqBody.createRequestBodyParam(EPPInfo);
    }
    else{
      ParameterObj parameterObj= createParameterObj(EPPInfo.name, paramLoction.query, false);
      ParamSchemaObj schemaObj=parameterObj.createSchema("string");

      return schemaObj;
    }
  }

  public ParamSchemaObj createRequestBodyParamObj(String name, String type){
    if(this.canHaveRequestBody()){
      RequestBodyObj reqBody=getRequestBody();

      return reqBody.createRequestBodyParam(name, type);
    }
    else{
      ParameterObj parameterObj= createParameterObj(name, paramLoction.query, false);
      ParamSchemaObj schemaObj=parameterObj.createSchema(type);

      return schemaObj;
    }
  }

  @Deprecated
  public ParameterObj createParameterObj(String name, paramLoction inType, boolean required){
    ParameterObj parameterObj=new ParameterObj(name, inType, required, this);

    this.parameters.add(parameterObj);
    return parameterObj;
  }

  public XEndPointConstraintsObj getOrCreateXEndPointConstraintObj(){
    if(this.xEPPEntry==null){
      this.xEPPEntry = this.parentPathItem.parentSpec.components.addOrFindEndPoint(this);

      String escapedPath=path.replace("/", "~1")
        // .replace("{", "%7B").replaceFirst("}", "%7D")
      ;
      this.componentParentPath=String.format("#/components/x-endpoint-constraints/%s/%s", escapedPath, this.HttpOp);
      // String ptrStr=String.format("%s/end-point-constraints", this.componentParentPath);

      this.xConstraints=new ReferenceObj(this.componentParentPath, xEPPEntry);
    }

    return this.xEPPEntry;
  }

  public GlobalRefObj addGlobalRefObj(GlobalRefObj globalRefObj){
    XEndPointConstraintsObj xEPPEntry = this.getOrCreateXEndPointConstraintObj();

    return xEPPEntry.addGlobalRef(globalRefObj);
  }

  public GlobalWriteObj addGlobalWriteObj(GlobalWriteObj globalWriteObj){
    XEndPointConstraintsObj xEPPEntry = this.getOrCreateXEndPointConstraintObj();

    return xEPPEntry.addGlobalWrite(globalWriteObj);
  }

  public boolean addValidCond(String cond){
    XEndPointConstraintsObj xEPPEntry = this.getOrCreateXEndPointConstraintObj();

    return xEPPEntry.addValidCond(cond);
  }

  public boolean addInvalidCond(String cond){
    XEndPointConstraintsObj xEPPEntry = this.getOrCreateXEndPointConstraintObj();

    return xEPPEntry.addInvalidCond(cond);
  }

  public boolean addRawValid(ArrayList<String> conds){
    if(this.rawValid==null){
      this.rawValid=new ArrayList<>();
    }
    return this.rawValid.add(conds);
  }

  public boolean addRawInvalid(ArrayList<String> conds){
    if(this.rawInvalid==null){
      this.rawInvalid=new ArrayList<>();
    }
    return this.rawInvalid.add(conds);
  }

  public ResponseObj addResponse(ResponseObj response){
    return this.responses.put(response.HTTPStatusCode, response);
  }
}
