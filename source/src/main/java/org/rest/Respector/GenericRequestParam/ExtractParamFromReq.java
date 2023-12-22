package org.rest.Respector.GenericRequestParam;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.EndPointRecog.EndPointParamInfo;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

import soot.MethodOrMethodContext;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

public class ExtractParamFromReq {
  public static final Map<String, Set<String>> requestTypeToGetter=loadJSON("RequestBodyAndGetter.json");

  public static ArrayList<String> extractParamFromRequest(CallGraph cg, EndPointMethodInfo EPInfo) {
    SootMethod ep = EPInfo.method;

    List<Type> paramTypes = ep.getParameterTypes();

    Set<String> getterMethod=methodForParam(paramTypes);

    if(getterMethod==null){
      return new ArrayList<>();
    }

    TreeSet<String> reqParams=new TreeSet<>();

    ArrayList<EndPointParamInfo> paramInfo = EPInfo.parameterInfo;

    TreeSet<String> paramNames=new TreeSet<>();
    for(EndPointParamInfo pI: paramInfo){
      paramNames.add(pI.name);
    }

    ReachableMethods closure=new ReachableMethods(cg, Arrays.asList(ep));

    QueueReader<MethodOrMethodContext> qReader = closure.listener();
    closure.update();

    while(qReader.hasNext()){
      MethodOrMethodContext m0=qReader.next();
      SootMethod m1=m0.method();

      if(!m1.hasActiveBody()){
        continue;
      }

      for(Unit u: m1.getActiveBody().getUnits()){
        assert u instanceof Stmt;
        
        Stmt stmt=(Stmt) u;

        if(!stmt.containsInvokeExpr()){
          continue;
        }

        InvokeExpr invokeExpr=stmt.getInvokeExpr();

        if(! (invokeExpr instanceof InstanceInvokeExpr)){
          continue;
        }

        InstanceInvokeExpr vInvokeExpr=(InstanceInvokeExpr) invokeExpr;

        String sig=vInvokeExpr.getMethodRef().getSignature();
        if(getterMethod.contains(sig)){
          Value arg0= vInvokeExpr.getArg(0);
          
          if(arg0 instanceof StringConstant){
            StringConstant strConst=(StringConstant) arg0;

            reqParams.add(strConst.value);
          }
        }

        
      }
    }

    return new ArrayList<>(reqParams);
  }

  public static Set<String> methodForParam(List<Type> paramTypes) {
    for(Type paramType: paramTypes){
      if(paramType instanceof RefType){
        RefType objType=(RefType) paramType;

        String className=objType.getClassName();

        if(requestTypeToGetter.containsKey(className)){
          return requestTypeToGetter.get(className);
        }
        else{
          return null;
        }
      }
    }

    return null;
  }

  public static Map<String, Set<String>> loadJSON(String path){
    InputStream is= ExtractParamFromReq.class.getClassLoader().getResourceAsStream(path);
    Map<String, Set<String>> rtv;

    Gson gson = new Gson();
    JsonReader reader;
    try {
      reader= new JsonReader(new InputStreamReader(is));
      rtv = gson.fromJson(reader, new TypeToken<Map<String, Set<String>>>(){}.getType());

    } catch (Exception e) {
      throw new RuntimeException("Unable to load framework data: Request Body Getters.");
    }

    return rtv;
  }
}
