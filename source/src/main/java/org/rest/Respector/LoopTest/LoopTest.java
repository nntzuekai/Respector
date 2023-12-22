package org.rest.Respector.LoopTest;

import java.util.List;
import java.util.Set;

import soot.*;
import soot.options.Options;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.jimple.JimpleBody;
import soot.plugins.SootPhasePlugin;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import soot.util.Chain;

import soot.jimple.toolkits.annotation.logic.*;

public class LoopTest {
  public static void main(String[] args) {
    List<String> clsName=List.of(args[0]);

    G.reset();
    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_keep_line_number(true);
    
    Options.v().set_soot_classpath("/home/rkh/1");
    // Options.v().setPhaseOption("jb", "use-original-names:true");
    // Options.v().setPhaseOption("jb", "preserve-source-annotations:true");

    // Options.v().set_whole_program(true);
    // Options.v().setPhaseOption("cg", "library:any-subtype");

    Options.v().set_write_local_annotations(true);
    SootClass sc = Scene.v().loadClassAndSupport(clsName.get(0));
    sc.setApplicationClass();
    Scene.v().loadNecessaryClasses();


    // PackManager.v().runPacks();

 
    SootMethod sm = sc.getMethodByName("main");
    JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
    Set<Loop> loops=(new LoopFinder()).getLoops(body);
    System.out.println(loops.size());
    
    
  }
}
