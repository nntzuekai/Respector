package org.rest.Respector.EndPointRecog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Iterator;


import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.options.Options;
import soot.plugins.SootPhasePlugin;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import soot.util.Chain;
import soot.Type;
import soot.dava.internal.AST.ASTTryNode.container;
import soot.Local;
import soot.Body;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.jimple.*;
import soot.jimple.internal.JIfStmt;
import soot.Printer;
import soot.RefType;
import soot.BriefUnitPrinter;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.EndPointRecog.FrameworkData.FrameworkName;
import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreprocessFramework {

  public transient FrameworkData frameworkData;
  public ArrayList<EndPointMethodInfo> endPointMethodData;

  private static Logger logger = LoggerFactory.getLogger(PreprocessFramework.class);
    
  public PreprocessFramework(FrameworkData frameworkData, ArrayList<EndPointMethodInfo> endPointMethodData) {
    this.frameworkData = frameworkData;
    this.endPointMethodData = endPointMethodData;
  }

  public static FrameworkName decideFramework(Chain<SootClass> libraryClasses, Chain<SootClass> phantomClasses) {
    // libraryClasses.stream().map(c->c.getPackageName()).distinct().forEach(x->System.out.println(x));
    // phantomClasses.stream().map(c->c.getPackageName()).distinct().forEach(x->System.out.println(x));
    
    Stream<SootClass> allClasses= Stream.concat(phantomClasses.stream(), libraryClasses.stream());
    Iterator<SootClass> iter = allClasses.iterator();

    while(iter.hasNext()){
      SootClass c=iter.next();
      String p=c.getPackageName();

      for(FrameworkData d:FrameworkData.Data.values()){
        for(String s:d.packageNames){
          if(p.equals(s)){
            logger.info("The REST API uses "+d.name);
            return d.name;
          }
        }
      }
    }

    return FrameworkName.Unknown;
  }

  public static PreprocessFramework getEndPointInfo(Scene v){
    return getEndPointInfo(v, null, null, null);
  }

  public static PreprocessFramework getEndPointInfo(Scene v, String excludedMethodName, String excludedPackageName, String excludedClassName) {
    Chain<SootClass> appClasses=v.getApplicationClasses();
    Chain<SootClass> libClasses=v.getLibraryClasses();
    Chain<SootClass> phantomClasses=v.getPhantomClasses();
    
    FrameworkName framework=decideFramework(libClasses, phantomClasses);

    if(framework==FrameworkName.Unknown){
      throw new RuntimeException("Unknown Framework");
    }
    
    ArrayList<EndPointMethodInfo> rtv=new ArrayList<>();

    FrameworkData data=FrameworkData.Data.get(framework);

    HashMap<SootClass, ArrayList<EndPointMethodInfo>> resrcClassToMethods=new HashMap<>();

    ArrayList<Pair<SootClass, String>> sortedAppClasses=new ArrayList<>();
    for(SootClass c: appClasses){
      sortedAppClasses.add(Pair.of(c, c.getName()));
    }
    sortedAppClasses.sort((b,a)->a.getRight().compareTo(b.getRight()));

    for(Pair<SootClass, String> p: sortedAppClasses){
      SootClass c=p.getLeft();
      if(excludedPackageName!=null && !excludedPackageName.equals(c.getPackageName())){
        continue;
      }

      if(excludedClassName!=null && !excludedClassName.equals(c.getShortName())){
        continue;
      }

      // if(c.getShortName().equals("TracksResource")){
      //   logger.info("analyzing TracksResource");
      // }

      VisibilityAnnotationTag cTags= (VisibilityAnnotationTag) c.getTag("VisibilityAnnotationTag");

      ArrayList<String> classPath=new ArrayList<>();
      ArrayList<EndPointParamInfo> fieldPathParams=new ArrayList<>();

      if(cTags != null){
        ArrayList<AnnotationTag> classAnnos = cTags.getAnnotations();
        for(AnnotationTag t: classAnnos){
          ClassMethodAnnotation CMAnno = data.classAnnotations.get(t.getType());

          if(CMAnno != null){
            classPath.addAll(CMAnno.getPathFrom(t));
          }
        }
      }

      if(!data.fieldAnnotations.isEmpty()){
        for(SootField f: c.getFields()){
          VisibilityAnnotationTag fieldTag=(VisibilityAnnotationTag) f.getTag("VisibilityAnnotationTag");
          
          if(fieldTag==null){
            continue;
          }
  
          ArrayList<VisibilityAnnotationTag> fieldAnnos=new ArrayList<>(List.of(fieldTag));
          List<Type> fieldType=List.of(f.getType());
          
          ArrayList<EndPointParamInfo> fieldInfo=getAnnotatedParam(fieldAnnos, data.fieldAnnotations, fieldType);

          if(!fieldInfo.isEmpty()){
            fieldPathParams.add(fieldInfo.get(0));

            logger.debug(String.format("Found annotated field %s in %s", f.getName(), c.getName()));
          }
        }
      }

      for(SootMethod m:c.getMethods()){
        // if(m.getName().equals("contributorsGet")){
        //   logger.info("analyzing contributorsGet");
        // }

        if(excludedMethodName!=null && !excludedMethodName.equals(m.getName())){
          continue;
        }

        VisibilityAnnotationTag tags = (VisibilityAnnotationTag) m.getTag("VisibilityAnnotationTag");
        if(tags==null){

          continue;
        }

        ArrayList<String> requestMethod=new ArrayList<>();
        ArrayList<String> methodPath=new ArrayList<>();
        int responseStatus=200;

        boolean hasMethodAnnotationsNoSkip=false;

        boolean hasPathExplosion=false;

        for(AnnotationTag mTag: tags.getAnnotations()){
          String annoType = mTag.getType();

          if(
            // annoType.equals("Lorg/respector/SkipEndPointForProfile;")|| 
            annoType.equals("Lorg/respector/SkipEndPointForPathExplosion;")
            ){
            hasPathExplosion=true;
            continue;
          }

          ClassMethodAnnotation CMAnno = data.methodAnnotations.get(annoType);

          if(CMAnno != null){
            hasMethodAnnotationsNoSkip=true;
            
            methodPath.addAll(CMAnno.getPathFrom(mTag));

            requestMethod.addAll(CMAnno.getRequestMethodFrom(mTag));
          }

          ClassMethodAnnotation CRAnno = data.responseStatusAnnotations.get(annoType);
          if(CRAnno!=null){
            Integer rtStatus=CRAnno.getResponseStatus(mTag, data.nameToResponse);
            if(rtStatus!=null){
              responseStatus=rtStatus;
            }
          }
        }

        if(!hasMethodAnnotationsNoSkip){
          continue;
        }

        ///TODO: fix this
        if(requestMethod.isEmpty()){
          if(framework==FrameworkName.Spring){
            requestMethod.addAll(List.of("get", "post", "head", "options", "put", "patch", "delete", "trace"));
          }
        }

        VisibilityParameterAnnotationTag paramTags=(VisibilityParameterAnnotationTag) m.getTag("VisibilityParameterAnnotationTag");
        ArrayList<EndPointParamInfo> paramInfo;
        if(paramTags==null){
          paramInfo=new ArrayList<>();
        }
        else{

          ArrayList<VisibilityAnnotationTag> paramAnnos=paramTags.getVisibilityAnnotations();

          List<Type> paramTypes= m.getParameterTypes();
          
          paramInfo=getAnnotatedParam(paramAnnos, data.paramAnnotations, paramTypes);

          // DONE: no argument end point?
          // keep them
          // if(paramInfo.isEmpty()){
            // continue;
          // }
        }
        EndPointMethodInfo EPInfo=new EndPointMethodInfo(m, m.getName(), requestMethod, paramInfo, methodPath, classPath, fieldPathParams, responseStatus, hasPathExplosion);
        rtv.add(EPInfo);

        ArrayList<EndPointMethodInfo> epms = resrcClassToMethods.computeIfAbsent(c, x-> new ArrayList<>());
        epms.add(EPInfo);
      }

    }

    linkSubResources(resrcClassToMethods);

    return new PreprocessFramework(data, rtv);
  }

  public static void linkSubResources(HashMap<SootClass, ArrayList<EndPointMethodInfo>> resrcClassToMethods) {
    for(Map.Entry<SootClass, ArrayList<EndPointMethodInfo>> kv: resrcClassToMethods.entrySet()){
      SootClass c=kv.getKey();
      ArrayList<EndPointMethodInfo> endPoints=kv.getValue();
      for(EndPointMethodInfo ep: endPoints){
        SootMethod m=ep.method;

        Type rType = m.getReturnType();

        if(rType instanceof RefType){
          RefType refRtv=(RefType) rType;

          SootClass clz=refRtv.getSootClass();

          if(clz.equals(c)){
            logger.error(String.format("method %s returns its own class %s", m.getSignature(), c.getName()));
            continue;
          }

          if(resrcClassToMethods.containsKey(clz)){
            for(EndPointMethodInfo subEP: resrcClassToMethods.get(clz)){
              subEP.parentResourceMethod.add(ep);
            }
          }
        }
      }
    }
  }

  public static boolean endpointAnnoCheck(AnnotationTag tag, Map<String,ClassMethodAnnotation> methodAnnotations){   
    return methodAnnotations.containsKey(tag.getType());
  }
  

  public static ArrayList<EndPointParamInfo> getAnnotatedParam(ArrayList<VisibilityAnnotationTag> tagList, Map<String,ParameterAnnotation> paramAnnotations, List<Type> paramTypes) {
    ArrayList<EndPointParamInfo> rtv=new ArrayList<>();
    
    int len=tagList.size();
    for(int i=0;i<len;++i){
      VisibilityAnnotationTag tag=tagList.get(i);

      if(tag==null){
        continue;
      }

      String name=null;
      Boolean required=null;
      paramLoction in=null;
      String defaultValue=null;

      boolean hasParamAnnotation=false;

      for(AnnotationTag pTag: tag.getAnnotations()){
        String type1=pTag.getType();
        ParameterAnnotation PAnno = paramAnnotations.get(type1);

        if(PAnno != null){
          hasParamAnnotation=true;

          if(name==null){
            name=PAnno.getNameFrom(pTag);
          }

          if(required==null){
            required=PAnno.getRequiredFrom(pTag);
          }

          if(in==null){
            in=PAnno.getInFrom(pTag);
          }

          if(defaultValue==null){
            defaultValue=PAnno.getDefaultValueFrom(pTag);
          }

        }
      }

      if(!hasParamAnnotation){
        continue;
      }

      // if(required==null){
      //   required=true;
      // }
      if(paramLoction.path.equals(in)){
        required=true;
      }
      if(name==null){
        name="";
      }
      
      rtv.add(new EndPointParamInfo(name, i, required, in, defaultValue, paramTypes.get(i)));
      
    }

    return rtv;
  }
  
}
