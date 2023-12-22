package org.rest.Respector.Simplification;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.PathConstraint;
import org.rest.Respector.PathCondExtract.ConditionPred.ConstantComp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.BoolSort;
import com.microsoft.z3.CharSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FPSort;
import com.microsoft.z3.IntSort;
import com.microsoft.z3.SeqSort;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;
import com.microsoft.z3.UninterpretedSort;

import soot.BooleanType;
import soot.G;
import soot.IntType;
import soot.Local;
import soot.NullType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Type;
import soot.Value;
import soot.grimp.AbstractGrimpValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.EqExpr;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.MethodHandle;
import soot.jimple.MethodType;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

public class SootToZ3 {
  public Context ctx = new Context();
  public Solver solver=ctx.mkSolver();
  public ArrayList<Expr<BoolSort>> modelExprs=new ArrayList<>();
  public Status status;
  public ArrayList<ConditionPred> notConverted=new ArrayList<>();

  UninterpretedSort NullConstantSort;
  Expr<UninterpretedSort> nullConst;

  public HashMap<Value, Expr> varMap=new HashMap<>();
  public HashMap<Expr, Value> varMapRev=new HashMap<>();

  public HashMap<Type, Sort> typeMap=new HashMap<>();
  // HashMap<Sort, Type> typeMapRev=new HashMap<>();

  public HashMap<Value, Expr> strMap=new HashMap<>();
  public HashMap<Expr, Value> strMapRev=new HashMap<>();

  final SootClass stringClass=Scene.v().getSootClass("java.lang.String");

  public final boolean simplificationMode;

  protected ArrayList<Expr<BoolSort>> auxiliaryExprs=new ArrayList<>();

  public ArrayList<Expr<BoolSort>> getAndClearAuxiliaryExprs() {
    ArrayList<Expr<BoolSort>> t=this.auxiliaryExprs;
    this.auxiliaryExprs=new ArrayList<>();
    return t;
  }

  private static Logger logger=LoggerFactory.getLogger(SootToZ3.class);

  public SootToZ3(Collection<ConditionPred> conjunction) {
    this.simplificationMode=false;

    init();

    for(ConditionPred pred: conjunction){
      if(pred.inLoopHeader){
        continue;
      }
      try{
        Expr<BoolSort> expr=toZ3Expr(pred.uniCond);
        solver.add(expr);
        modelExprs.add(expr);
      }
      catch(RuntimeException e){
        logger.debug("Failed to convert to Z3: "+pred.uniCond.toString());
        notConverted.add(pred);
      }
    
    }
  }

  
  public SootToZ3() {
    this(false);
  }

  public SootToZ3(boolean simplificationMode) {
    this.simplificationMode=simplificationMode;
    init();
  }

  void init(){
    this.NullConstantSort=ctx.mkUninterpretedSort("NullSort");
    this.nullConst=ctx.mkConst("nullConst", NullConstantSort);

    typeMap.put(NullType.v(), NullConstantSort);
    varMap.put(NullConstant.v(), nullConst);
    varMapRev.put(nullConst, NullConstant.v());
  }

  public void closeCtx() {
    this.ctx.close();
  }

  public Expr toZ3Expr(Value v){
    AbstractGrimpValueSwitch sw=new AbstractGrimpValueSwitch() {
      @Override
      public void caseLocal(Local v) {
        Type t=v.getType();
        if(t instanceof RefType){
          RefType ref=(RefType) t;

          Sort sort=typeMap.computeIfAbsent(ref, e->{ return ctx.mkUninterpretedSort(ref.getClassName()); });
          Expr ex= varMap.computeIfAbsent(v, e->ctx.mkConst(v.toString(), sort));
          varMapRev.putIfAbsent(ex, v);

          if(ref.getSootClass().equals(stringClass)){
            Expr strEx =strMap.computeIfAbsent(v, e->ctx.mkConst(v.getName(), ctx.getStringSort()));
            strMapRev.put(strEx, v);
          }

          setResult(ex);
        }
        else if(t instanceof IntType){
          Expr ex=varMap.computeIfAbsent(v, e->ctx.mkIntConst(v.toString()));
          varMapRev.putIfAbsent(ex, v);

          setResult(ex);
        }
        else if(t instanceof BooleanType){
          // Actually we still make Int Const for Boolean
          Expr ex=varMap.computeIfAbsent(v, e->ctx.mkIntConst(v.toString()));
          varMapRev.putIfAbsent(ex, v);

          setResult(ex);
        }
        else{
          defaultCase(v);
        }
      }

      @Override
      public void caseDoubleConstant(DoubleConstant v) {
        double t=v.value;

        setResult(ctx.mkFPNumeral(t, ctx.mkFPSortDouble()));
      }

      @Override
      public void caseFloatConstant(FloatConstant v) {
        float t=v.value;

        setResult(ctx.mkFPNumeral(t, ctx.mkFPSortSingle()));
      }

      @Override
      public void caseIntConstant(IntConstant v) {
        int t=v.value;

        setResult(ctx.mkInt(t));
      }

      @Override
      public void caseLongConstant(LongConstant v) {
        long t=v.value;

        setResult(ctx.mkInt(t));
      }

      @Override
      public void caseNullConstant(NullConstant v) {
        setResult(nullConst);
      }

      @Override
      public void caseStringConstant(StringConstant v) {
        String str=v.value;

        setResult(ctx.mkString(str));
      }

      @Override
      public void caseClassConstant(ClassConstant v) {
        defaultCase(v);
      }

      @Override
      public void caseMethodHandle(MethodHandle v) {
        defaultCase(v);
      }

      @Override
      public void caseMethodType(MethodType v) {
        defaultCase(v);
      }

      @Override
      public void caseArrayRef(ArrayRef v) {
        defaultCase(v);
      }

      @Override
      public void caseStaticFieldRef(StaticFieldRef v) {
        defaultCase(v);
      }

      @Override
      public void caseInstanceFieldRef(InstanceFieldRef v) {
        defaultCase(v);
      }

      @Override
      public void caseParameterRef(ParameterRef v) {
        defaultCase(v);
      }

      @Override
      public void caseCaughtExceptionRef(CaughtExceptionRef v) {
        defaultCase(v);
      }

      @Override
      public void caseThisRef(ThisRef v) {
        defaultCase(v);
      }

      @Override
      public void caseAddExpr(AddExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseAndExpr(AndExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseCmpExpr(CmpExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseCmpgExpr(CmpgExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseCmplExpr(CmplExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseDivExpr(DivExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseEqExpr(EqExpr v) {
        Value op1=v.getOp1();
        Value op2=v.getOp2();

        Expr e1=toZ3Expr(op1);
        Expr e2=toZ3Expr(op2);

        Sort sort1=e1.getSort();
        Sort sort2=e2.getSort();
        if(!sort1.equals(sort2)){
          if(sort2.equals(NullConstantSort)){
            e2=ctx.mkConst("null", e1.getSort());
          }
          else if(sort1.equals(NullConstantSort)){
            e1=ctx.mkConst("null", e2.getSort());
          }
          else if(sort1 instanceof BoolSort && sort2 instanceof IntSort){
            e2=ctx.mkNot(ctx.mkEq(e2, ctx.mkInt(0))).simplify();
          }
        }
        
        setResult(ctx.mkEq(e1, e2));
      }
    
      @Override
      public void caseNeExpr(NeExpr v) {
        Value op1=v.getOp1();
        Value op2=v.getOp2();

        Expr e1=toZ3Expr(op1);
        Expr e2=toZ3Expr(op2);

        Sort sort1=e1.getSort();
        Sort sort2=e2.getSort();
        if(!sort1.equals(sort2)){
          if(sort2.equals(NullConstantSort)){
            e2=ctx.mkConst("null", e1.getSort());
          }
          else if(sort1.equals(NullConstantSort)){
            e1=ctx.mkConst("null", e2.getSort());
          }
          else if(sort1 instanceof BoolSort && sort2 instanceof IntSort){
            e2=ctx.mkNot(ctx.mkEq(e2, ctx.mkInt(0))).simplify();
          }
        }

        setResult(ctx.mkNot(ctx.mkEq(e1, e2)));
      }
    
      @Override
      public void caseGeExpr(GeExpr v) {
        Value op1=v.getOp1();
        Value op2=v.getOp2();

        Expr e1=toZ3Expr(op1);
        Expr e2=toZ3Expr(op2);

        setResult(ctx.mkGe(e1, e2));
      }
    
      @Override
      public void caseGtExpr(GtExpr v) {
        Value op1=v.getOp1();
        Value op2=v.getOp2();

        Expr e1=toZ3Expr(op1);
        Expr e2=toZ3Expr(op2);

        setResult(ctx.mkGt(e1, e2));
      }
    
      @Override
      public void caseLeExpr(LeExpr v) {
        Value op1=v.getOp1();
        Value op2=v.getOp2();

        Expr e1=toZ3Expr(op1);
        Expr e2=toZ3Expr(op2);

        setResult(ctx.mkLe(e1, e2));
      }
    
      @Override
      public void caseLtExpr(LtExpr v) {
        Value op1=v.getOp1();
        Value op2=v.getOp2();

        Expr e1=toZ3Expr(op1);
        Expr e2=toZ3Expr(op2);

        setResult(ctx.mkLt(e1, e2));
      }
    
      @Override
      public void caseMulExpr(MulExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseOrExpr(OrExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseRemExpr(RemExpr v) {
        Value op1=v.getOp1();
        Value op2=v.getOp2();

        Expr e1=toZ3Expr(op1);
        Expr e2=toZ3Expr(op2);

        setResult(ctx.mkRem(e1, e2));
      }
    
      @Override
      public void caseShlExpr(ShlExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseShrExpr(ShrExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseUshrExpr(UshrExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseSubExpr(SubExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseXorExpr(XorExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseStaticInvokeExpr(StaticInvokeExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
        Value base=v.getBase();
        Type t=base.getType();

        if(ConditionPred.evalToNullConstant(v)==ConstantComp.EvalTrue){
          if(simplificationMode){
            auxiliaryExprs.add(ctx.mkFalse());
          }
          else{
            solver.add(ctx.mkFalse());
          }

          /// TODO: could throw an exception to indicate unsat
          defaultCase(v);
          return;
        }

        if(t instanceof RefType){
          RefType ref=(RefType) t;

          Sort sort=typeMap.computeIfAbsent(ref, e->{ return ctx.mkUninterpretedSort(ref.getClassName()); });
          Expr ex= varMap.computeIfAbsent(base, e->ctx.mkConst(base.toString(), sort));
          varMapRev.putIfAbsent(ex, v);

          BoolExpr nonNullExpr=ctx.mkNot(ctx.mkEq(ex, ctx.mkConst("null", sort)));
          if(simplificationMode){
            auxiliaryExprs.add(nonNullExpr);
          }
          else{
            solver.add(nonNullExpr);
          }

          if(base instanceof Local && ref.getSootClass().equals(stringClass)){
            Local l=(Local) base;
            Expr<SeqSort<CharSort>> strEx= strMap.computeIfAbsent(l, e->ctx.mkConst(l.getName(), ctx.getStringSort()));
            strMapRev.putIfAbsent(strEx, l);

            if(v.getMethodRef().getName().equals("length")){
              Expr lenOf=ctx.mkLength(strEx);
              setResult(lenOf);
              varMapRev.putIfAbsent(lenOf, v);

              return;
            }
            else if(v.getMethodRef().getName().equals("isEmpty")){
              BoolExpr e1=ctx.mkEq(ctx.mkLength(strEx), ctx.mkInt(0));

              Expr ite=ctx.mkITE(e1, ctx.mkInt(1), ctx.mkInt(0));

              setResult(ite);
              varMapRev.putIfAbsent(ite, v);

              return;
            }
            else if(v.getMethodRef().getName().equals("contains")){
              assert v.getArgCount()==1;

              Value arg0=v.getArg(0);
              if(arg0 instanceof StringConstant){
                StringConstant strCon=(StringConstant) arg0;
                BoolExpr strContain=ctx.mkContains(strEx, ctx.mkString(strCon.value));

                setResult(strContain);
                varMapRev.putIfAbsent(strContain, v);
                return;
              }
            }
          }

        }
        
        Sort sort=typeMap.computeIfAbsent(t, e->{ return ctx.mkUninterpretedSort(t.toString()); });
        Expr ex=varMap.computeIfAbsent(v, e->ctx.mkConst(v.toString(), sort));
        varMapRev.putIfAbsent(ex, v);

        setResult(ex);
      }
    
      @Override
      public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseCastExpr(CastExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseInstanceOfExpr(InstanceOfExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseNewArrayExpr(NewArrayExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseNewExpr(NewExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseLengthExpr(LengthExpr v) {
        defaultCase(v);
      }
    
      @Override
      public void caseNegExpr(NegExpr v) {
        Value op=v.getOp();
        Expr e=toZ3Expr(op);

        setResult(ctx.mkUnaryMinus(e));
      }
    
      @Override
      public void defaultCase(Object obj) {
        throw new RuntimeException("Not supported type");
      }
  
  

    };
    v.apply(sw);
    return (Expr)sw.getResult();
  }
}
