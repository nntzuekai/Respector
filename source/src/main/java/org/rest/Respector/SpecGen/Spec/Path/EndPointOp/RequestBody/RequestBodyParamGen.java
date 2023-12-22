package org.rest.Respector.SpecGen.Spec.Path.EndPointOp.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;


import soot.AnySubType;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.RefType;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.Type;
import soot.TypeSwitch;
import soot.VoidType;
import soot.util.Chain;

import org.apache.commons.lang3.tuple.Pair;

public class RequestBodyParamGen {

  public static HashMap<Type, HashMap<String, String>> cachedSchemas=new HashMap<>();

  public static HashMap<String, String> toReqParamNameAndTypes(Type type, ArrayList<String> exlcudePackages) {
    if(cachedSchemas.containsKey(type)){
      return cachedSchemas.get(type);
    }

    if(!(type instanceof RefType)){
      return null;
    }

    RefType t=(RefType)type;
    SootClass clazz=t.getSootClass();

    if(clazz.isPhantom()){
      return null;
    }

    if(!clazz.isConcrete()){
      return null;
    }

    String name=clazz.getName();
    if(name.startsWith("java.util.") || name.startsWith("java.lang.")){
      return null;
    }

    if(exlcudePackages!=null){
      for(String s: exlcudePackages){
        if(name.startsWith(s)){
          return null;
        }
      }
    }

    HashMap<String, String> params=new HashMap<>();

    TypeSwitch sw = new TypeSwitch() {

      @Override
      public void caseBooleanType(BooleanType t) {
        setResult("boolean");
      }

      @Override
      public void caseByteType(ByteType t) {
        setResult("integer");
      }

      /// TODO: should it be int16 or char?
      @Override
      public void caseCharType(CharType t) {
        setResult("integer");
      }

      @Override
      public void caseDoubleType(DoubleType t) {
        setResult("number");
      }

      @Override
      public void caseFloatType(FloatType t) {
        setResult("number");
      }

      @Override
      public void caseIntType(IntType t) {
        setResult("integer");
      }

      @Override
      public void caseLongType(LongType t) {
        setResult("integer");
      }

      @Override
      public void caseRefType(RefType t) {
        String className=t.getClassName();
        if(className.equals("java.lang.String")){
          setResult("string");
        }

        else if(className.equals("java.lang.Boolean")){
          setResult("boolean");
        }

        else if(className.equals("java.lang.Byte")){
          setResult("integer");
        }

        else if(className.equals("java.lang.Character")){
          setResult("integer");
        }

        else if(className.equals("java.lang.Double")){
          setResult("number");
        }

        else if(className.equals("java.lang.Float")){
          setResult("number");
        }

        else if(className.equals("java.lang.Integer")){
          setResult("integer");
        }

        else if(className.equals("java.lang.Long")){
          setResult("integer");
        }

        else if(className.equals("java.lang.Short")){
          setResult("integer");
        }
        
        else{
          SootClass clazz = t.getSootClass();
          String name=clazz.getName();

          if(exlcudePackages!=null){
            for(String s: exlcudePackages){
              if(name.startsWith(s)){
                setResult(null);
                return;
              }
            }
          }
          
          defaultCase(type);
        }
      }

      @Override
      public void caseShortType(ShortType t) {
        setResult("integer");
      }

      @Override
      public void caseAnySubType(AnySubType t) {
        caseRefType(t.getBase());
      }

      @Override
      public void defaultCase(Type t) {
        setResult("string");
      }

    };

    do {
      Chain<SootField> sootFields=clazz.getFields();

      for(SootField field: sootFields){
        if(field.isStatic()){
          continue;
        }

        String fName=field.getName();
        Type fType=field.getType();

        if(fType instanceof RefType){
          RefType t1=(RefType)fType;
          String typeName=t1.getClassName();
          if(exlcudePackages!=null){
            if(exlcudePackages.stream().anyMatch(s->typeName.startsWith(s))){
              continue;
            }
          }
        }

        

        fType.apply(sw);
        String typeName=(String)sw.getResult();

        if(typeName==null){
          continue;
        }

        params.putIfAbsent(fName, typeName);

      }

      if(!clazz.hasSuperclass()){
        break;
      }
      
      clazz=clazz.getSuperclass();
    } while (true);

    return params;
  }
}
