package org.rest.Respector.PrintAllPaths;

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

import java.io.File;
import java.io.PrintWriter;

import soot.*;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.plugins.SootPhasePlugin;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import soot.util.Chain;
import soot.Type;
import soot.JastAddJ.Opt;
import soot.Local;
import soot.Body;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.jimple.*;


public class PrintAllPathsMain {
  public static void main(String[] args) {
    List<String> process_dir=List.of(args[0]);

    String methodName=null;
    if(args.length>1){
      methodName=args[1];
    }

    String packageName=null;
    if(args.length>2){
      packageName=args[2];
    }

    String className=null;
    if(args.length>3){
      className=args[3];
    }

    boolean printBackEdge=false;
    if(args.length>4 && args[4].equals("-b")){
      printBackEdge=true;
    }

    String userClassPath="";
    // if(args.length>5){
    //   userClassPath=args[5];
    // }

    // userClassPath="/home/rkh/fall2022/guava-19.0.jar";

    String sourceDirectory = System.getProperty("user.dir");

    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_keep_line_number(true);
    // Options.v().set_output_format(Options.output_format_jimp);
    // Options.v().set_verbose(true);
    // Options.v().set_debug(true);

    Options.v().set_process_dir(process_dir);

    Options.v().set_soot_classpath(String.join(File.pathSeparator, sourceDirectory, userClassPath));

    Options.v().set_omit_excepting_unit_edges(true);

    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().setPhaseOption("jb", "preserve-source-annotations:true");

    // Options.v().setPhaseOption("jb.cp", "enabled");

    Options.v().set_write_local_annotations(true);

    Options.v().set_whole_program(true);
    // Call-graph options
    Options.v().setPhaseOption("cg", "library:any-subtype");
    // Options.v().setPhaseOption("cg", "safe-newinstance:true");
    Options.v().setPhaseOption("cg", "all-reachable");
    // Options.v().setPhaseOption("cg", "resolve-all-abstract-invokes");

    // Enable CHA call-graph construction
    // Options.v().setPhaseOption("cg.cha","enabled:true");
    Options.v().setPhaseOption("cg.cha","enabled:false");

    // Enable SPARK call-graph construction
    Options.v().setPhaseOption("cg.spark","enabled:true");
    // Options.v().setPhaseOption("cg.spark","verbose:true");
    // Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
    // Options.v().setPhaseOption("cg.spark", "field-based:true");
    // Options.v().setPhaseOption("cg.spark", "types-for-sites:true");

    // Enable BDD call-graph construction
    // Options.v().setPhaseOption("cg.paddle", "enabled");

    Options.v().set_no_bodies_for_excluded(true);
    // Options.v().set_app(true);
    Scene.v().loadNecessaryClasses();

    PreprocessFramework endPointInfoWithData=PreprocessFramework.getEndPointInfo(Scene.v(), methodName, packageName, className);
    
    Options.v().set_output_format(Options.output_format_jimple);

    Transform MyApp1=new Transform("wjtp.MyApp", new PrintAllPaths(endPointInfoWithData, printBackEdge));
    PackManager.v().getPack("wjtp").add(MyApp1);

    PackManager.v().runPacks();
  }
}

