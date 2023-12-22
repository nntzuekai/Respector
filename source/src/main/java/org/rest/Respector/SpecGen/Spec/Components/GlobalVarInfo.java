package org.rest.Respector.SpecGen.Spec.Components;

import java.util.ArrayList;

import org.rest.Respector.SpecGen.Spec.ReferenceObj;
import org.rest.Respector.StaticVarAssignment.StaticExampleLoc;

/**
 * GSON serializer @see GlobalVarInfoSerializer
 */
public class GlobalVarInfo {
  public String name;
  public int id;
  public String location;

  public ArrayList<StaticExampleLoc> locs;

  public transient ReferenceObj refToMe;

  public GlobalVarInfo(String name, int id, String location) {
    this.name = name;
    this.id = id;
    this.location = location;
    this.refToMe=new ReferenceObj(String.format("#/components/x-global-variables-info/g%d", this.id));
  }
}
