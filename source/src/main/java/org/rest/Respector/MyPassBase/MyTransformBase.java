package org.rest.Respector.MyPassBase;

import java.util.ArrayList;
import java.util.HashMap;

import org.rest.Respector.EndPointRecog.PreprocessFramework;
import org.rest.Respector.LoopInfo.MethodLoopInfo;
import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathRecord.MethodEntryRecord;
import org.rest.Respector.PathRecord.ParamInfo;
import org.rest.Respector.PathRecord.PathRecord;
import org.rest.Respector.PathRecord.PathRecordBase;
import org.rest.Respector.PathRecord.PathRecordBase.RecordType;

import soot.Body;
import soot.BriefUnitPrinter;
import soot.Hierarchy;
import soot.SceneTransformer;

import soot.Scene;
import soot.Value;

import soot.jimple.Stmt;

import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;

public abstract class MyTransformBase extends SceneTransformer {
  public PreprocessFramework preprocessReuslt;
  public HashMap<Body,BriefUnitPrinter> printerSet;
  public JimpleBasedInterproceduralCFG icfg;
  // public Hierarchy hierarchy;
  public boolean printBackEdge;
  public CallGraph CHA_CG;
  public HashMap<Body, MethodLoopInfo> bodyToLoopInfoCache;

  public MyTransformBase(PreprocessFramework preprocessReuslt, boolean printBackEdge) {
    this.preprocessReuslt = preprocessReuslt;
    this.printBackEdge=printBackEdge;
  }

  public void buildCHACallGraph(){
    CallGraph cg0=Scene.v().getCallGraph();

    CHATransformer.v().transform();
    CHA_CG=Scene.v().getCallGraph();

    Scene.v().setCallGraph(cg0);
  }

  public void printPath(ArrayList<PathRecordBase> path){
    printPath(path,0);
  }

  public void printPath(ArrayList<PathRecordBase> path, int indent0){
    for(PathRecordBase rec:path){
      int d=indent0+(rec.callStack.size()-1)*4;
      if(rec.type==RecordType.MethodEntry){
        MethodEntryRecord rec1=(MethodEntryRecord) rec;
        System.out.print(String.format(
          "%s%s, params: ", " ".repeat(d+4), rec1.method.getName()
        ));

        for(ParamInfo info: rec1.paramInfo){
          System.out.printf("%d, ", info.idx);
        }
        System.out.println();
      }
      else if(((PathRecord)rec).preInvoke){continue;}
      else{
        PathRecord rec1=(PathRecord) rec;
        Stmt s=rec1.stmt;
        String note=rec1.note;
        RecordType type=rec.type;

        Body b=icfg.getBodyOf(s);

        BriefUnitPrinter pt=printerSet.get(b);

        if(pt==null){
          pt=new BriefUnitPrinter(b);
          pt.noIndent();
          printerSet.put(b, pt);
        }

        s.toString(pt);
        String out=pt.toString();
        String firstLine=out.split("\n")[0];
        // System.out.print(String.format("%s%d\t%s        %s  %s\n", " ".repeat(d), s.getJavaSourceStartLineNumber(), firstLine, type.name(), note!=null?note:""));
        System.out.print(String.format("%s%d\t%s        %s\n", " ".repeat(d), s.getJavaSourceStartLineNumber(), firstLine, note!=null?note:""));

        // if (rec.type == RecordType.Invoke) {
        //   indent += 4;
        // } else if (rec.type == RecordType.Return) {
        //   indent -= 4;
        // }
      }
    }

    System.out.print("----------------------\n");
  }

  public void printConditions(ArrayList<ConditionPred> conds){
    for(ConditionPred pred:conds){
      if(pred.inLoopHeader){
        continue;
      }

      // System.out.printf("( %s ) == %s\n", pred.pred.toString(), pred.evalTo.toString());
      System.out.printf("%s\n", pred.uniCond.toString());

      // if(pred.negated){
      //   System.out.printf("( %s ) == false\n", pred.str);
      //   // System.out.printf("!(%s);\n", pred.str);
      // }
      // else{
      //   System.out.printf("( %s ) ==  true\n", pred.str);
      //   // System.out.printf("%s;\n", pred.str);
      // }
    }
    System.out.printf("----------------------\n");
  }

  public void printResponse(Value rtv){
    if(rtv==null){
      System.out.printf("return: void\n");
    }
    else{
      System.out.printf("return: %s\n", rtv.toString());
    }
    System.out.printf("----------------------\n");
  }

  public void printResponseValidity(boolean v){
    System.out.printf("%s\n", v?"valid":"invalid");
  }
}
