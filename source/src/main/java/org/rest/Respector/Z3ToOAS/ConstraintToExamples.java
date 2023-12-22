package org.rest.Respector.Z3ToOAS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.ParameterObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Examples.ExampleObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.BooleanType;
import soot.SootField;
import soot.Value;
import soot.jimple.AbstractConstantSwitch;
import soot.jimple.ConditionExpr;
import soot.jimple.Constant;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.StringConstant;

public class ConstraintToExamples {
  static class NumericAndStringConstant extends AbstractConstantSwitch{
    public void caseDoubleConstant(DoubleConstant v) {
      setResult(v.value);
    }
  
    public void caseFloatConstant(FloatConstant v) {
      setResult(v.value);
    }
  
    public void caseIntConstant(IntConstant v) {
      setResult(v.value);
    }
  
    public void caseLongConstant(LongConstant v) {
      setResult(v.value);
    }
  
    public void caseStringConstant(StringConstant v) {
      setResult(v.value);
    }

    public void defaultCase(Object v) {
      setResult(null);
    }
  }

  private static Logger logger = LoggerFactory.getLogger(ConstraintToExamples.class);

  public static HashSet<Object> extractExamplesFromCepp(Collection<ArrayList<ConditionPred>> disjunction, EndPointParameter epp) {
    HashSet<Object> examples=new HashSet<>();

    for(ArrayList<ConditionPred> conjuction: disjunction){
      for(ConditionPred pred: conjuction){
        if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
          continue;
        }

        if(pred.uniCond instanceof EqExpr){
          EqExpr expr=(EqExpr) pred.uniCond;
    
          Value op1=expr.getOp1();
          Value op2=expr.getOp2();
    
          if(op1 instanceof EndPointParameter && op2 instanceof Constant){
            // EndPointParameter lhs=(EndPointParameter)op1;
            assert op1.equivTo(epp);

            NumericAndStringConstant sw=new NumericAndStringConstant();
            op2.apply(sw);
            Object res=sw.getResult();

            if(res!=null){
              if(epp.getType() instanceof BooleanType){
                if(res instanceof Integer){
                  int v=(Integer)res;
                  examples.add(v!=0);
                }
              }
              else{
                examples.add(res);
              }
            }
          }
          else if(op2 instanceof EndPointParameter){
            logger.error("EPP on RHS?");
          }
        }
      }
    }

    return examples;
  }

  public static HashSet<Object> extractExamplesFromCg(Collection<ArrayList<ConditionPred>> disjunction, SootField g) {
    HashSet<Object> examples=new HashSet<>();

    for(ArrayList<ConditionPred> conjuction: disjunction){
      for(ConditionPred pred: conjuction){
        if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
          continue;
        }

        if(pred.uniCond instanceof EqExpr){
          EqExpr expr=(EqExpr) pred.uniCond;
    
          Value op1=expr.getOp1();
          Value op2=expr.getOp2();
    
          if(op1 instanceof StaticFieldRef && op2 instanceof Constant){
            StaticFieldRef lhs=(StaticFieldRef)op1;
            assert g.equals(lhs.getField());

            NumericAndStringConstant sw=new NumericAndStringConstant();
            op2.apply(sw);
            Object res=sw.getResult();

            if(res!=null){
              if(g.getType() instanceof BooleanType){
                if(res instanceof Integer){
                  int v=(Integer)res;
                  examples.add(v!=0);
                }
              }
              else{
                examples.add(res);
              }
            }
          }
          else if(op1 instanceof Constant && op2 instanceof StaticFieldRef){
            // it happens
            // assert false: "global on RHS?";

            StaticFieldRef rhs=(StaticFieldRef)op2;
            assert g.equals(rhs.getField());

            NumericAndStringConstant sw=new NumericAndStringConstant();
            op1.apply(sw);
            Object res=sw.getResult();

            if(res!=null){
              if(g.getType() instanceof BooleanType){
                if(res instanceof Integer){
                  int v=(Integer)res;
                  examples.add(v!=0);
                }
              }
              else{
                examples.add(res);
              }
            }
          }
        }
      }
    }
    
    return examples;
  }
}
