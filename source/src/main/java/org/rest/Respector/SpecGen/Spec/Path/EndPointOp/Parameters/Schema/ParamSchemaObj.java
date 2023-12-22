package org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.SpecGen.Spec.ReferenceObj;
import org.rest.Respector.SpecGen.Spec.TypeTranslation;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints.ConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints.XEndPointConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.ParameterObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.Items.ParamSchemaItemsObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

import soot.ArrayType;
import soot.Type;

public class ParamSchemaObj {
  public String type;
  public String format;
  public String title;
  @SerializedName("default")
  public Object defaultValue;
  public Integer maxLength;
  public Integer minLength;

  public Number maximum;
  public Boolean exclusiveMaximum;

  public Number minimum;
  public Boolean exclusiveMinimum;

  public Integer multipleOf;

  public ArrayList<String> pattern;

  public HashSet<Integer> maxItems;
  public HashSet<Integer> minItems;
  
  public Integer minProperties;
  public Integer maxProperties;

  @SerializedName("x-parameter-constraints")
  public ReferenceObj xConstraints;

  public transient ConstraintsObj paramConstraintEntry;

  public ParamSchemaItemsObj items;

  public transient final ParameterObj parentParam;

  // public ParamSchemaObj(String type, HashSet<Integer> maxLength, HashSet<Integer> minLength,
  //     ArrayList<String> constraints) {
  //   this.type = type;
  //   this.maxLength = maxLength;
  //   this.minLength = minLength;
  //   this.constraints = constraints;
  // }

  private static Logger logger=LoggerFactory.getLogger(ParamSchemaObj.class);
  
  public ParamSchemaObj(String type, ParameterObj parentParam) {
    this.type = type;
    this.parentParam=parentParam;
  }

  public ParamSchemaObj(Type type, ParameterObj parentParam) {
    this.parentParam=parentParam;

    if(type==null){
      this.type="string";
      return;
    }

    Pair<String, String> jsonType=TypeTranslation.typeToJSONTypeAndFormat(type);
    this.type=jsonType.getLeft();
    this.format=jsonType.getRight();

    if(this.type.equals("array")){
      if(type instanceof ArrayType){
        ArrayType arrType=(ArrayType)type;
  
        this.items=new ParamSchemaItemsObj(arrType.getElementType());
      }
      else{
        this.items=new ParamSchemaItemsObj("string", null);
      }
    }
    
  }

  // @Deprecated
  // public void bindXParameterConstraints(ConstraintsObj ePPConstraintsObj, String parentPath){
  //   this.paramConstraintEntry=ePPConstraintsObj;
  //   this.xConstraints=new ReferenceObj(parentPath, ePPConstraintsObj);
  // }

  public ConstraintsObj getOrCreateParamterConstraints(){
    if(this.paramConstraintEntry!=null){
      return this.paramConstraintEntry;
    }

    ConstraintsObj constraintsObj=this.parentParam.getOrCreateXParameterConstraints();
    this.paramConstraintEntry=constraintsObj;

    this.xConstraints=new ReferenceObj(this.parentParam.componentsPath(), constraintsObj);

    return constraintsObj;
  }

  public boolean addConstraint(String constraint){
    ConstraintsObj constraintsObj=getOrCreateParamterConstraints();

    return constraintsObj.addCond(constraint);
  }

  public boolean addMaxLength(int maxLength) {
    if(this.maxLength==null || this.maxLength>maxLength){
      this.maxLength=maxLength;
      return true;
    }

    return false;
  }

  public boolean addMinLength(int minLength) {
    if(this.minLength==null || this.minLength<minLength){
      this.minLength=minLength;
      return true;
    }

    return false;
  }

  public String setTitle(String title){
    this.title=title;

    return title;
  }

  public boolean addMultipleOf(int m) {
    if(this.multipleOf==null){
      this.multipleOf=m;

      return true;
    }
    else{
      logger.debug(String.format("overwrite multipleOf: %d with %d", this.multipleOf, m));

      this.multipleOf=m;
      return false;
    }
  }

  public boolean addMaximum(int m) {
    if(this.maximum==null){
      this.maximum=m;
      return true;
    }

    if(this.maximum instanceof Integer){
      int m0=(Integer)this.maximum;

      if(m0>m){
        this.maximum=m;
        return true;
      }
      else{
        return false;
      }
    }

    if(this.maximum instanceof Double){
      double m0=(Double)this.maximum;

      if(m0>m){
        this.maximum=m;
        return true;
      }
      else{
        return false;
      }
    }

    return false;
  }
  public boolean addExclusiveMaxium(boolean e) {
    return this.exclusiveMaximum=e;
  }

  public boolean addMinimum(int m) {
    if(this.minimum==null){
      this.minimum=m;
      return true;
    }

    if(this.minimum instanceof Integer){
      int m0=(Integer)this.minimum;

      if(m0<m){
        this.minimum=m;
        return true;
      }
      else{
        return false;
      }
    }

    if(this.minimum instanceof Double){
      double m0=(Double)this.minimum;

      if(m0<m){
        this.minimum=m;
        return true;
      }
      else{
        return false;
      }
    }

    return false;
  }
  public boolean addExclusiveMinimum(boolean e) {
    return this.exclusiveMinimum=e;
  }



  public boolean addPattern(String p) {
    if(this.pattern==null){
      this.pattern=new ArrayList<>();
    }

    return this.pattern.add(p);
  }

  public boolean addMaxItems(int m) {
    if(this.maxItems==null){
      this.maxItems=new HashSet<>();
    }

    return this.maxItems.add(m);
  }

  public boolean addMinItems(int m) {
    if(this.minItems==null){
      this.minItems=new HashSet<>();
    }

    return this.minItems.add(m);
  }

  public boolean addMinProperties(int m){
    if(m<0){
      return false;
    }

    if(this.minProperties==null){
      this.minProperties=m;
      return true;
    }

    if(this.minProperties<m){
      this.minProperties=m;
      return true;
    }
    
    return false;
  }

  public boolean addMaxProperties(int m){
    if(m<0){
      return false;
    }

    if(this.maxProperties==null){
      this.maxProperties=m;
      return true;
    }

    if(this.maxProperties>m){
      this.maxProperties=m;
      return true;
    }
    
    return false;
  }

  public void setDefault(String defaultValue) {
    try {
      if(this.type.equals("integer")){
        this.defaultValue=Integer.valueOf(defaultValue);
      }
      else if(this.type.equals("number")){
        this.defaultValue=Double.valueOf(defaultValue);
      }
      else if(this.type.equals("boolean") && (defaultValue.equalsIgnoreCase("true") || defaultValue.equalsIgnoreCase("false"))){
        this.defaultValue=Boolean.valueOf(defaultValue);
      }
      else{
        this.defaultValue=defaultValue;
      }
    } catch (NumberFormatException e) {
      this.defaultValue=defaultValue;
    }

    this.parentParam.setRequiredByUser(false);
  }
}