package org.rest.Respector.SpecGen.Spec.Components;

import java.util.ArrayList;
import java.util.TreeMap;

import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XPathItemObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints.XEndPointConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Components.InterDependence.GlobalInterDepObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.EndPointOperationObj;

import com.google.gson.annotations.SerializedName;

/**
 * GSON serializer @see SpecComponentsObjSerializer
 */
public class SpecComponentsObj {
  // @SerializedName("x-endpoint-constraints")
  public TreeMap<String, XPathItemObj> xEndPointConstraints=new TreeMap<>();

  // @SerializedName("x-endpoint-interdependence")
  public GlobalInterDepObj interDependencies= new GlobalInterDepObj();

  // @SerializedName("x-global-variables-info")  
  public TreeMap<Integer, GlobalVarInfo> globalsWithStaticVarAssign= new TreeMap<>();

  public XEndPointConstraintsObj addOrFindEndPoint(EndPointOperationObj epp){
    XPathItemObj xPathItemObj=this.xEndPointConstraints.computeIfAbsent(epp.path, e-> new XPathItemObj());

    return xPathItemObj.addOrFindEndPoint(epp);
  }
}
