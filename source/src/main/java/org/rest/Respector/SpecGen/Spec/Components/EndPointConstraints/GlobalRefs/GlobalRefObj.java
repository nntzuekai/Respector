package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs;

import java.util.ArrayList;

import org.rest.Respector.SpecGen.Spec.ReferenceObj;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;

import com.google.gson.annotations.SerializedName;

/**
 * GSON serializer @see GlobalRefObjSerializer
 */
public class GlobalRefObj {
  public final transient GlobalVarInfo varInfo;
  
  protected ArrayList<String> constraints;
  protected ArrayList<Object> examples;

  public final transient ReferenceObj ref;
  
  public GlobalRefObj(GlobalVarInfo varInfo, ArrayList<String> constraints, ArrayList<Object> examples) {
    this.varInfo = varInfo;
    this.constraints = constraints;
    this.examples = examples;
    this.ref=varInfo.refToMe;
  }

  public GlobalRefObj(GlobalVarInfo varInfo) {
    this.varInfo = varInfo;
    this.ref=varInfo.refToMe;
  }

  public boolean addConstraint(String constraint) {
    if(this.constraints==null){
      this.constraints=new ArrayList<>();
    }

    return this.constraints.add(constraint);
  }

  public boolean addExample(Object example) {
    if(this.examples==null){
      this.examples=new ArrayList<>();
    }

    return this.examples.add(example);
  }
}
