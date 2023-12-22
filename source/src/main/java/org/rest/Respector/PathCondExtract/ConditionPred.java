package org.rest.Respector.PathCondExtract;

import soot.jimple.AbstractExprSwitch;
import soot.jimple.CastExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.Constant;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.NullConstant;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JNeExpr;

import javax.management.RuntimeErrorException;

import soot.Immediate;
import soot.SootMethod;
import soot.Value;
import soot.grimp.NewInvokeExpr;
import soot.grimp.internal.GEqExpr;
import soot.grimp.internal.GGeExpr;
import soot.grimp.internal.GGtExpr;
import soot.grimp.internal.GLeExpr;
import soot.grimp.internal.GLtExpr;
import soot.grimp.internal.GNeExpr;

public class ConditionPred {
  // pred must be ConditionExpr for an IfStmt
  // pred is an immediate for a switchStmt
  public Value _pred;
  public Object evalTo;
  public String str;
  public boolean inLoopHeader;
  public ConditionExpr uniCond;

  public ConditionPred(Value pred, Boolean evalTo) {
    this._pred = pred;
    this.evalTo = evalTo;
    this.str=null;
    this.inLoopHeader=false;

    toUniCondition();
  }

  public ConditionPred(Value pred, Boolean evalTo, String str) {
    this._pred = pred;
    this.evalTo = evalTo;
    this.str=str;
    this.inLoopHeader=false;

    toUniCondition();
  }

  public ConditionPred(Value pred, Boolean evalTo, String str, boolean inLoopHeader) {
    this._pred = pred;
    this.evalTo = evalTo;
    this.str = str;
    this.inLoopHeader = inLoopHeader;

    toUniCondition();
  }

  public ConditionPred(Value pred, Integer evalTo, String str) {
    this._pred = pred;
    this.evalTo = evalTo;
    this.str = str;
    this.inLoopHeader = false;

    toUniCondition();
  }

  public ConditionPred(Value pred, Integer evalTo, String str, boolean inLoopHeader) {
    this._pred = pred;
    this.evalTo = evalTo;
    this.str = str;
    this.inLoopHeader = inLoopHeader;

    toUniCondition();
  }

  public void toUniCondition(){
    if(this._pred instanceof Immediate){
      int rhs=(Integer) this.evalTo;
      this.uniCond=new GEqExpr(this._pred, IntConstant.v(rhs));
      this.evalTo=true;
    }
    else{
      assert this._pred instanceof ConditionExpr;
      boolean rhs=(Boolean) this.evalTo;

      if(rhs){
        this.uniCond=(ConditionExpr)this._pred;
      }
      else{
        AbstractExprSwitch sw=new AbstractExprSwitch() {
          @Override
          public void caseEqExpr(EqExpr v) {
            setResult(new GNeExpr(v.getOp1(), v.getOp2()));
          }
          @Override
          public void caseNeExpr(NeExpr v) {
            setResult(new GEqExpr(v.getOp1(), v.getOp2()));
          }

          @Override
          public void caseGeExpr(GeExpr v) {
            setResult(new GLtExpr(v.getOp1(), v.getOp2()));
          }

          @Override
          public void caseGtExpr(GtExpr v) {
            setResult(new GLeExpr(v.getOp1(), v.getOp2()));
          }

          @Override
          public void caseLeExpr(LeExpr v) {
            setResult(new GGtExpr(v.getOp1(), v.getOp2()));
          }

          @Override
          public void caseLtExpr(LtExpr v) {
            setResult(new GGeExpr(v.getOp1(), v.getOp2()));
          }

          @Override
          public void defaultCase(Object obj) {
            throw new RuntimeException("Pred is not a ConditionExpr");
          }
        };

        this._pred.apply(sw);
        this.uniCond=(ConditionExpr)sw.getResult();
      }
    }
  }

  public static enum ConstantComp{
    NonConstant,
    EvalTrue,
    EvalFalse
  }

  public static ConstantComp checkConstantComparison(ConditionPred cond) {
    ConditionExpr uniCond=cond.uniCond;

    Value lhs=uniCond.getOp1();
    Value rhs=uniCond.getOp2();

    if(lhs instanceof IntConstant && rhs instanceof IntConstant){
      int v1=((IntConstant) lhs).value;
      int v2=((IntConstant) rhs).value;

      AbstractExprSwitch sw=new AbstractExprSwitch() {
        @Override
        public void caseEqExpr(EqExpr v) {
          setResult(v1==v2?ConstantComp.EvalTrue:ConstantComp.EvalFalse);
        }
        @Override
        public void caseNeExpr(NeExpr v) {
          setResult(v1!=v2?ConstantComp.EvalTrue:ConstantComp.EvalFalse);
        }

        @Override
        public void caseGeExpr(GeExpr v) {
          setResult(v1>=v2?ConstantComp.EvalTrue:ConstantComp.EvalFalse);
        }

        @Override
        public void caseGtExpr(GtExpr v) {
          setResult(v1>v2?ConstantComp.EvalTrue:ConstantComp.EvalFalse);
        }

        @Override
        public void caseLeExpr(LeExpr v) {
          setResult(v1<=v2?ConstantComp.EvalTrue:ConstantComp.EvalFalse);
        }

        @Override
        public void caseLtExpr(LtExpr v) {
          setResult(v1<v2?ConstantComp.EvalTrue:ConstantComp.EvalFalse);
        }

        @Override
        public void defaultCase(Object obj) {
          throw new RuntimeException("Pred is not a ConditionExpr");
        }
      };

      uniCond.apply(sw);
      return (ConstantComp)sw.getResult();
    }
    else if(lhs instanceof NullConstant || rhs instanceof NullConstant){
      ConstantComp lhsNull = evalToNullConstant(lhs);

      if(lhsNull==ConstantComp.NonConstant){
        return ConstantComp.NonConstant;
      }

      ConstantComp rhsNull = evalToNullConstant(rhs);

      if(rhsNull==ConstantComp.NonConstant){
        return ConstantComp.NonConstant;
      }

      if(uniCond instanceof EqExpr){
        return lhsNull==rhsNull?ConstantComp.EvalTrue:ConstantComp.EvalFalse;
      }
      else if(uniCond instanceof NeExpr){
        return lhsNull!=rhsNull?ConstantComp.EvalTrue:ConstantComp.EvalFalse;
      }
      else{
        throw new RuntimeException("NullConstant in arithmetic comparison?");
      }
    }


    return ConstantComp.NonConstant;
  }

  public static ConstantComp evalToNullConstant(Value v){
    if(v instanceof NullConstant){
      return ConstantComp.EvalTrue;
    }
    else if(v instanceof CastExpr){
      CastExpr expr=(CastExpr)v;
      return evalToNullConstant(expr.getOp());
    }
    else if(v instanceof NewInvokeExpr){
      return ConstantComp.EvalFalse;
    }
    else if(v instanceof StaticInvokeExpr){
      StaticInvokeExpr expr=(StaticInvokeExpr)v;

      SootMethod m= expr.getMethod();

      if(m.getName().equals("valueOf") &&
        m.getDeclaringClass().getName().equals("java.lang.Integer")
      ){
        return ConstantComp.EvalFalse;
      }
    }

    return ConstantComp.NonConstant;
  }

  public int hashCode(){
    return this.uniCond.equivHashCode();
  }
  public boolean equals(Object o) {
    if(o instanceof ConditionPred){
      ConditionPred c=(ConditionPred)o;
      return this.inLoopHeader==c.inLoopHeader && this.uniCond.equivTo(c.uniCond);
    }
    return false;
  }
}
