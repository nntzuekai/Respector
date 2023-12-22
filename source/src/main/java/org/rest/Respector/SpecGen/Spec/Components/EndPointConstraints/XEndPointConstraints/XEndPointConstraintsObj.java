package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints;

import java.util.TreeMap;

import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.Constraints.ConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites.GlobalWriteObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointMethodConstraint.XEndPointMethodConstraintObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.ParameterObj;

import com.google.gson.annotations.SerializedName;

/**
 * GSON serializer @see XEndPointConstraintsObjSerializer
 */
public class XEndPointConstraintsObj {
  public TreeMap<String, ConstraintsObj> parameterConstraints;

  public XEndPointMethodConstraintObj methodConstraints;

  public GlobalRefObj addGlobalRef(GlobalRefObj ref){
    if(this.methodConstraints==null){
      this.methodConstraints=new XEndPointMethodConstraintObj();
    }

    return this.methodConstraints.addGlobalRef(ref);
  }

  public GlobalWriteObj addGlobalWrite(GlobalWriteObj wr){
    if(this.methodConstraints==null){
      this.methodConstraints=new XEndPointMethodConstraintObj();
    }

    return this.methodConstraints.addGlobalWrite(wr);
  }

  public boolean addValidCond(String cond){
    if(this.methodConstraints==null){
      this.methodConstraints=new XEndPointMethodConstraintObj();
    }

    return this.methodConstraints.addValidCond(cond);
  }

  public boolean addInvalidCond(String cond){
    if(this.methodConstraints==null){
      this.methodConstraints=new XEndPointMethodConstraintObj();
    }

    return this.methodConstraints.addInvalidCond(cond);
  }

  public ConstraintsObj addOrFindParameterConstraintsObj(ParameterObj parameterObj){
    if(this.parameterConstraints==null){
      this.parameterConstraints=new TreeMap<>();
    }

    return this.parameterConstraints.computeIfAbsent(parameterObj.name, e -> new ConstraintsObj());
  }
}
