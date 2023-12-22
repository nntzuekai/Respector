package org.rest.Respector.Interdependence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;

import soot.SootField;

public class GlobalVarInterdependency {

  public HashMap<GlobalVarInfo, Pair<ArrayList<EndPointMethodInfo>, ArrayList<EndPointMethodInfo>>> interdependency;
  

  public GlobalVarInterdependency(
    HashMap<GlobalVarInfo, Pair<ArrayList<EndPointMethodInfo>, ArrayList<EndPointMethodInfo>>> interdependency) {
    this.interdependency = interdependency;
  }


  public static GlobalVarInterdependency computeGlobalInterdepend(HashMap<GlobalVarInfo, ArrayList<EndPointMethodInfo>> globalToWriters, HashMap<GlobalVarInfo, ArrayList<EndPointMethodInfo>> globalToReaders) {
    HashMap<GlobalVarInfo, Pair<ArrayList<EndPointMethodInfo>, ArrayList<EndPointMethodInfo>>> inter=new HashMap<>();

    HashSet<GlobalVarInfo> commonKeys = new HashSet<>(globalToReaders.keySet());
    commonKeys.retainAll(globalToWriters.keySet());

    for(GlobalVarInfo varInfo: commonKeys){
      Pair<ArrayList<EndPointMethodInfo>, ArrayList<EndPointMethodInfo>> v = Pair.of(globalToWriters.get(varInfo), globalToReaders.get(varInfo));
      inter.put(varInfo, v);
    }

    return new GlobalVarInterdependency(inter);
  }
}
