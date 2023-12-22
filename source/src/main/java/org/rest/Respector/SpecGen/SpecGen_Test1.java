package org.rest.Respector.SpecGen;

import java.util.ArrayList;
import java.util.Arrays;

import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.rest.Respector.SpecGen.Spec.SpecObj;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XPathItemObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XPathItemObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObj;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObjSerializer;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.EndPointOperationObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.ParameterObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Examples.ExampleObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.ParamSchemaObj;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SpecGen_Test1 {
  public static void main(String[] args){
    SpecObj spec=new SpecObj();
    // spec.components.interDependencies.add(new ArrayList<>(Arrays.asList("path1.param1","path2.param2")));
    // spec.components.interDependencies.add(new ArrayList<>(Arrays.asList("path1.param1","path3.param3")));

    EndPointOperationObj ep=spec.createEndPointOperation("path1", "get", "operationId");

    ParameterObj param=ep.createParameterObj("a", paramLoction.path, true);
    ParamSchemaObj schema1=param.createSchema("string");

    schema1.addConstraint("a!=\"ddd\"");
    schema1.addMaxLength(3);

    param.addExample("example1", new ExampleObj("example1", "aaa"));

    GlobalVarInfo gInfo=new GlobalVarInfo("g1", 0, "code location");
    GlobalRefObj gr=new GlobalRefObj(gInfo);
    gr.addConstraint("g1!=\"ggg\"");
    gr.addExample("readFromJSON(\"countries.json\")");



    ep.addGlobalRefObj(gr);


    Gson gson=SpecGen.getSpecBuilder();
    System.out.println(gson.toJson(spec));
  }
}
