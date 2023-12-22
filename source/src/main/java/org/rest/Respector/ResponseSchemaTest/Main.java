package org.rest.Respector.ResponseSchemaTest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.EndPointRecog.PreprocessFramework;

import soot.*;
import soot.Scene;
import soot.options.Options;
import soot.util.Chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class Main {
  private static Logger logger = LoggerFactory.getLogger(Main.class);
  public static void main(String[] args) {
    assert args.length>=2;
    List<String> argsList=Arrays.asList(args);
    List<String> process_dir=argsList.subList(0, args.length-1);
    
    String outputFile=argsList.get(args.length-1);

    String sourceDirectory = System.getProperty("user.dir");

    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_keep_line_number(true);
    Options.v().set_exclude(List.of("jdk.*"));
    // Options.v().set_ignore_resolution_errors(true);
    // Options.v().set_output_format(Options.output_format_jimp);
    // Options.v().set_verbose(true);
    // Options.v().set_debug(true);

    Options.v().set_process_dir(process_dir);

    Options.v().set_soot_classpath(sourceDirectory);

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

    PreprocessFramework endPointInfoWithData=PreprocessFramework.getEndPointInfo(Scene.v());
    
    Options.v().set_output_format(Options.output_format_jimple);

    Transform MyApp1=new Transform("wjtp.MyApp", new MainTransform(endPointInfoWithData, outputFile));
    PackManager.v().getPack("wjtp").add(MyApp1);

    PackManager.v().runPacks();
  }
}
