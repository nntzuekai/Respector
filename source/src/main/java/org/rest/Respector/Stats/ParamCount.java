package org.rest.Respector.Stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.EndPointRecog.FrameworkData;
import org.rest.Respector.EndPointRecog.PreprocessFramework;

import soot.*;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import soot.util.Chain;

public class ParamCount {
  public static void main(String[] args) {
    List<String> process_dir=List.of(args[0]);
    String sourceDirectory = System.getProperty("user.dir");

    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);

    Options.v().set_process_dir(process_dir);

    Options.v().set_soot_classpath(sourceDirectory);

    Options.v().set_whole_program(true);

    Options.v().set_no_bodies_for_excluded(true);
    
    Scene.v().loadNecessaryClasses();

    PreprocessFramework preprocessed=PreprocessFramework.getEndPointInfo(Scene.v(), null, null, null);

    int sum=0;
    for(EndPointMethodInfo e: preprocessed.endPointMethodData){
      System.err.printf("%s: %d\n", e.method.getName(), e.parameterInfo.size());
      sum+=e.parameterInfo.size();
    }
    double avg=1.0*sum/preprocessed.endPointMethodData.size();

    System.out.printf("Average: %f\n", avg);
  }
}
