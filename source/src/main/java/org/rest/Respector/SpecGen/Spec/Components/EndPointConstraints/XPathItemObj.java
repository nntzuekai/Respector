package org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints;

import java.util.TreeMap;

import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XEndPointConstraints.XEndPointConstraintsObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.EndPointOperationObj;

public class XPathItemObj {
  public TreeMap<String, XEndPointConstraintsObj> xEndPointConstraints=new TreeMap<>();

  public XEndPointConstraintsObj addOrFindEndPoint(EndPointOperationObj epp){
    XEndPointConstraintsObj xEPP= this.xEndPointConstraints.computeIfAbsent(epp.HttpOp, e -> new XEndPointConstraintsObj());

    return xEPP;
  }
}
