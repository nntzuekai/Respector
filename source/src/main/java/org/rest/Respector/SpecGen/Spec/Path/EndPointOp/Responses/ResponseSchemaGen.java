package org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Responses;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

public class ResponseSchemaGen {
  public static JsonObject schemaWithType(String typeString) {
    JsonObject sch=new JsonObject();
    sch.addProperty("type", typeString);
    return sch;
  }

  public static JsonElement toResponseSchema(Type type){
    return toResponseSchema(type, null);
  }

  public static HashMap<Type, JsonElement> cachedSchemas=new HashMap<>();

  public static JsonElement toResponseSchema(Type type, ArrayList<String> exlcudePackages) {
    if(cachedSchemas.containsKey(type)){
      return cachedSchemas.get(type);
    }

    TypeSwitch sw = new TypeSwitch() {

      @Override
      public void caseArrayType(ArrayType t) {
        JsonObject sch=new JsonObject();
        sch.addProperty("type", "array");
        sch.add("items", toResponseSchema(t.baseType));

        setResult(sch);
      }

      @Override
      public void caseBooleanType(BooleanType t) {
        setResult(schemaWithType("boolean"));
      }

      @Override
      public void caseByteType(ByteType t) {
        setResult(schemaWithType("integer"));
      }

      /// TODO: should it be int16 or char?
      @Override
      public void caseCharType(CharType t) {
        setResult(schemaWithType("integer"));
      }

      @Override
      public void caseDoubleType(DoubleType t) {
        setResult(schemaWithType("number"));
      }

      @Override
      public void caseFloatType(FloatType t) {
        setResult(schemaWithType("number"));
      }

      @Override
      public void caseIntType(IntType t) {
        setResult(schemaWithType("integer"));
      }

      @Override
      public void caseLongType(LongType t) {
        setResult(schemaWithType("integer"));
      }

      @Override
      public void caseRefType(RefType t) {
        String className=t.getClassName();
        if(className.equals("java.lang.String")){
          setResult(schemaWithType("string"));
        }

        else if(className.equals("java.lang.Boolean")){
          setResult(schemaWithType("boolean"));
        }

        else if(className.equals("java.lang.Byte")){
          setResult(schemaWithType("integer"));
        }

        else if(className.equals("java.lang.Character")){
          setResult(schemaWithType("integer"));
        }

        else if(className.equals("java.lang.Double")){
          setResult(schemaWithType("number"));
        }

        else if(className.equals("java.lang.Float")){
          setResult(schemaWithType("number"));
        }

        else if(className.equals("java.lang.Integer")){
          setResult(schemaWithType("integer"));
        }

        else if(className.equals("java.lang.Long")){
          setResult(schemaWithType("integer"));
        }

        else if(className.equals("java.lang.Short")){
          setResult(schemaWithType("integer"));
        }
        else if(
          className.equals("java.util.List")
          || className.equals("java.util.ArrayList")
          || className.equals("java.util.LinkedList")
          || className.equals("java.util.Set")
          || className.equals("java.util.HashSet")
          || className.equals("java.util.TreeSet")
          || className.equals("java.util.LinkedHashSet")
          || className.equals("java.util.Collection")
          || className.equals("java.util.SortedSet")
        ){
          JsonObject sch=new JsonObject();
          sch.addProperty("type", "array");
          sch.add("items", schemaWithType("object"));

          setResult(sch);
        }
        else if(
          className.equals("java.util.Map")
          || className.equals("java.util.SortedMap")
          || className.equals("java.util.TreeMap")
          || className.equals("java.util.HashMap")
        ){
          setResult(schemaWithType("object"));
        }
        else{
          JsonObject sch=schemaWithType("object");

          SootClass clazz = t.getSootClass();
          String name=clazz.getName();

          if(exlcudePackages!=null){
            for(String s: exlcudePackages){
              if(name.startsWith(s)){
                setResult(sch);
                return;
              }
            }
          }

          sch.addProperty("title", name);

          Chain<SootField> sootFields=clazz.getFields();

          if(!sootFields.isEmpty()){
            JsonObject props=new JsonObject();
            for(SootField field: sootFields){
              if(field.isStatic()){
                continue;
              }
              
              props.add(field.getName(), toResponseSchema(field.getType()));
            }
            sch.add("properties", props);
          }

          ///TODO: add fields in the superclass (getFields only returns fields defined directly in the class)

          setResult(sch);
        }
      }

      @Override
      public void caseShortType(ShortType t) {
        setResult(schemaWithType("integer"));
      }

      @Override
      public void caseVoidType(VoidType t) {
        setResult(null);
      }

      @Override
      public void caseAnySubType(AnySubType t) {
        caseRefType(t.getBase());
      }

      @Override
      public void defaultCase(Type t) {
        setResult(schemaWithType("object"));
      }

    };

    type.apply(sw);

    JsonElement res=(JsonElement) sw.getResult();

    cachedSchemas.put(type, res);

    return res;
  }
}
