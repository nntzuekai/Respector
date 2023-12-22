package org.rest.Respector.SpecGen.Spec.Components.InterDependence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.Interdependence.GlobalVarInterdependency;
import org.rest.Respector.SpecGen.Spec.ReferenceObj;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;

/**
 * GSON serializer @see GlobalInterDepSerializer
 */
public class GlobalInterDepObj {
  public transient TreeMap<Integer, GlobalInterDepItemObj> interdependency;

  public GlobalInterDepObj() {
    this.interdependency=new TreeMap<>();
  }
}
