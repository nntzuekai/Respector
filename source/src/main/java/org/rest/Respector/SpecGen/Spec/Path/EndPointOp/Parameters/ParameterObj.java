package org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters;

import java.util.ArrayList;
import java.util.HashMap;

import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints.ConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints.XEndPointConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.EndPointOperationObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Examples.ExampleObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.ParamSchemaObj;

import com.google.gson.annotations.SerializedName;

import soot.Type;

public class ParameterObj {
  public String name;

  @SerializedName("in")
  public paramLoction inLiocation;

  protected boolean required;

  protected HashMap<String, ExampleObj> examples;

  public ParamSchemaObj schema;

  public transient final EndPointOperationObj parentOperation;

  public transient Boolean requiredByUser;

  // public ParameterObj(String name, String inLiocation, boolean required, HashMap<String, ExampleObj> examples,
  //     ParamSchemaObj schema) {
  //   this.name = name;
  //   this.inLiocation = inLiocation;
  //   this.required = required;
  //   this.examples = examples;
  //   this.schema = schema;
  // }

  // public ParameterObj(String name, String inLiocation, boolean required, ParamSchemaObj schema) {
  //   this.name = name;
  //   this.inLiocation = inLiocation;
  //   this.required = required;
  //   this.examples = new HashMap<>();
  //   this.schema = schema;
  // }

  public ParameterObj(String name, paramLoction inLiocation, Boolean requiredByUser, EndPointOperationObj parentOperation) {
    this.name = name;
    this.inLiocation = inLiocation;

    this.requiredByUser=requiredByUser;
    if(requiredByUser!=null){
      this.required = requiredByUser;
    }
    else{
      this.required=false;
    }

    this.parentOperation=parentOperation;
  }

  public String componentsPath() {
    return String.format("%s/x-parameter-constraints/%s", this.parentOperation.componentParentPath, this.name);
  }

  public ExampleObj addExample(String egName, ExampleObj example) {
    if(this.examples==null){
      this.examples=new HashMap<>();
    }

    return this.examples.put(egName, example);
  } 

  public ExampleObj addExample(ExampleObj example){
    if(this.examples==null){
      this.examples=new HashMap<>();
    }

    return this.examples.put("example"+this.examples.size(), example);
  }

  public boolean containsExampleName(String egName) {
    if(this.examples==null){
      return false;
    }
    else{
      return this.examples.containsKey(egName);
    }
  }

  public ParamSchemaObj createSchema(String type) {
    ParamSchemaObj paramSchemaObj=new ParamSchemaObj(type, this);
    this.schema=paramSchemaObj;

    return paramSchemaObj;
  }

  public ParamSchemaObj createSchema(Type type) {
    ParamSchemaObj paramSchemaObj=new ParamSchemaObj(type, this);
    this.schema=paramSchemaObj;

    return paramSchemaObj;
  }

  public ConstraintsObj getOrCreateXParameterConstraints(){
    return this.parentOperation.getOrCreateXEndPointConstraintObj().addOrFindParameterConstraintsObj(this);
  }

  // @Deprecated
  // public void bindXParameterConstraints(XEndPointConstraintsObj ePPConstraintsObj, String parentPath){
  //   ConstraintsObj constraintsObj = ePPConstraintsObj.addOrFindParameterConstraintsObj(this);
  //   this.schema.bindXParameterConstraints(constraintsObj, String.format("%s/parameter-constraints/%s", parentPath, this.name));
  // }
  
  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    if(this.requiredByUser==null){
      this.required = required;
    }
  }

  public Boolean getRequiredByUser() {
    return requiredByUser;
  }

  public void setRequiredByUser(Boolean requiredByUser) {
    this.requiredByUser = requiredByUser;
    if(requiredByUser!=null){
      this.required=requiredByUser;
    }
  }

  public void setDefault(String defaultValue) {
    this.schema.setDefault(defaultValue);
  }
}
