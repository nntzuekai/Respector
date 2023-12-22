package org.rest.Respector.SpecGen.Spec;

import org.apache.commons.lang3.tuple.Pair;

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
import soot.Type;
import soot.TypeSwitch;

public class TypeTranslation {
  public static Pair<String, String> typeToJSONTypeAndFormat(Type type) {
    TypeSwitch sw = new TypeSwitch() {

      @Override
      public void caseArrayType(ArrayType t) {
        setResult(Pair.of("array",null));
      }

      @Override
      public void caseBooleanType(BooleanType t) {
        setResult(Pair.of("boolean", null));
      }

      @Override
      public void caseByteType(ByteType t) {
        setResult(Pair.of("integer","int8"));
      }

      /// TODO: should it be int16 or char?
      @Override
      public void caseCharType(CharType t) {
        setResult(Pair.of("integer","int16"));
      }

      @Override
      public void caseDoubleType(DoubleType t) {
        setResult(Pair.of("number","double"));
      }

      @Override
      public void caseFloatType(FloatType t) {
        setResult(Pair.of("number","float"));
      }

      @Override
      public void caseIntType(IntType t) {
        setResult(Pair.of("integer","int32"));
      }

      @Override
      public void caseLongType(LongType t) {
        setResult(Pair.of("integer","int64"));
      }

      @Override
      public void caseRefType(RefType t) {
        String className=t.getClassName();
        if(className.equals("java.lang.String")){
          setResult(Pair.of("string",null));
        }

        else if(className.equals("java.lang.Boolean")){
          setResult(Pair.of("boolean", null));
        }

        else if(className.equals("java.lang.Byte")){
          setResult(Pair.of("integer","int8"));
        }

        else if(className.equals("java.lang.Character")){
          setResult(Pair.of("integer","int16"));
        }

        else if(className.equals("java.lang.Double")){
          setResult(Pair.of("number","double"));
        }

        else if(className.equals("java.lang.Float")){
          setResult(Pair.of("number","float"));
        }

        else if(className.equals("java.lang.Integer")){
          setResult(Pair.of("integer","int32"));
        }

        else if(className.equals("java.lang.Long")){
          setResult(Pair.of("integer","int64"));
        }

        else if(className.equals("java.lang.Short")){
          setResult(Pair.of("integer","int16"));
        }
        else if(
          className.equals("java.util.List")
          || className.equals("java.util.ArrayList")
          || className.equals("java.util.LinkedList")
          || className.equals("java.util.Set")
          || className.equals("java.util.HashSet")
          || className.equals("java.util.TreeSet")
          || className.equals("java.util.LinkedHashSet")
        ){
          setResult(Pair.of("array", null));
        }

        else{
          setResult(Pair.of("string",null));
        }
      }

      @Override
      public void caseShortType(ShortType t) {
        setResult(Pair.of("integer","int16"));
      }

      @Override
      public void defaultCase(Type t) {
        setResult(Pair.of("string",null));
      }

    };

    type.apply(sw);

    @SuppressWarnings("unchecked") 
    Pair<String, String> res=(Pair<String, String>) sw.getResult();
    return res;
  }
}
