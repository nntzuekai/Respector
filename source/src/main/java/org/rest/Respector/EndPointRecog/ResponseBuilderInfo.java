package org.rest.Respector.EndPointRecog;

import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Responses.ResponseSchemaGen;

import com.google.gson.JsonElement;

import soot.SootField;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticFieldRef;

public class ResponseBuilderInfo {
  public String subSignature;
  public Integer arg;
  public Integer returnStatus; 
  public Integer entityArg;

  public Integer extractStatusCode(InvokeExpr inv, FrameworkData frameworkData) {
    if(this.arg!=null){
      Value argV=inv.getArg(this.arg);

      if(argV instanceof StaticFieldRef){
        SootField ref=((StaticFieldRef) argV).getField();
        for(StaticResponseInfo res: frameworkData.enumResponses){
          if(ref.getDeclaringClass().getName().equals(res.declaringClass)
            && ref.getName().equals(res.name)
          ){
            return res.statusCode;
          }
        }
      }
      else if(argV instanceof IntConstant){
        int intV= ((IntConstant) argV).value;

        return intV;

      }
    }
    else{
      assert this.returnStatus!=null;

      return this.returnStatus;
    }

    return null;
  }

  public JsonElement extractSchema(InvokeExpr inv, FrameworkData frameworkData) {
    if(this.entityArg==null){
      return null;
    }

    Value argV=inv.getArg(this.entityArg);
    
    return ResponseSchemaGen.toResponseSchema(argV.getType());
  }
}
