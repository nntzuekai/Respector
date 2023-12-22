package org.rest.Respector.PathCondExtract;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import soot.ValueBox;
import soot.grimp.Grimp;
import soot.grimp.internal.ExprBox;
import soot.grimp.internal.ObjExprBox;
import soot.Immediate;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ConcreteRef;
import soot.jimple.Expr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JArrayRef;

public class RhsRewrite {
  public static ExprBox rewriteExpr(Expr src, ImmutableMap<Value, ExprBox> symStore) {
    Expr newExpr=(Expr)Grimp.v().newExpr(src);
    boolean modified=false;
    for(ValueBox vb: newExpr.getUseBoxes()){
      Value v=vb.getValue();
      if(symStore.containsKey(v))
      {
        Value newV=symStore.get(v).getValue();

        modified=true;
        vb.setValue(newV);

        // if(!(vb instanceof ObjExprBox && newV instanceof NewExpr)
        //   // && !(vb instanceof ExprBox && newV instanceof ParameterRef)
        // ){
        //   modified=true;
        //   vb.setValue(newV);
        // }
      }
    }
    if(modified){
      return new ExprBox(newExpr);
    }
    else{
      return new ExprBox(src);
    }
  }

  public static ExprBox rewriteImm(Immediate src, ImmutableMap<Value, ExprBox> symStore) {
    if(symStore.containsKey(src)){
      return symStore.get(src);
    }
    else{
      return new ExprBox(src);
    }
  }

  public static ExprBox rewriteRHS(Value src, ImmutableMap<Value, ExprBox> symStore) {
    if(src instanceof Immediate){
      return rewriteImm((Immediate)src, symStore);
    }
    else if(src instanceof Expr){
      if(src instanceof VirtualInvokeExpr){
        ExprBox rtv=rewritePrimitiveWrapperCall((VirtualInvokeExpr)src, symStore);
        if(rtv!=null){
          return rtv;
        }
      }

      return rewriteExpr((Expr)src, symStore);
    }
    else{
      assert src instanceof ConcreteRef;
      
      ConcreteRef src1=(ConcreteRef)src;

      return new ExprBox(src1);
    }
  }

  public static ExprBox rewritePrimitiveWrapperCall(VirtualInvokeExpr src, ImmutableMap<Value, ExprBox> symStore) {
    Value base0=src.getBase();
    Value base1=rewriteRHS(base0, symStore).getValue();

    if(base1 instanceof EndPointParameter){
      EndPointParameter base2=(EndPointParameter)base1;
      SootMethod m = src.getMethod();
      String mName=m.getName();
      String className=m.getDeclaringClass().getName();

      /// TODO: check OpenAPI datatypes
      if( 
          (className.equals("java.lang.Integer") && mName.equals("intValue"))
          ||(className.equals("java.lang.Double") && mName.equals("doubleValue"))
          ||(className.equals("java.lang.Float") && mName.equals("floatValue"))
          ||(className.equals("java.lang.Long") && mName.equals("longValue"))
          ||(className.equals("java.lang.Byte") && mName.equals("byteValue"))
          // ||m.getSignature().equals("<java.lang.Integer: int intValue()>")
          ){
        EndPointParameter epp1=new EndPointParameter(base2.getName(), base2.isEPP,base2.idx, m.getReturnType());

        return new ExprBox(epp1);
      }
    }

    return null;
  }
}
