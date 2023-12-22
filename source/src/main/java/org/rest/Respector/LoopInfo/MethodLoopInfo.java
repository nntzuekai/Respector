package org.rest.Respector.LoopInfo;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.UnitGraph;
import soot.Body;
import soot.Unit;
import soot.jimple.Stmt;

public class MethodLoopInfo {
  public HashSet<Stmt> headerBlockStmts;

  Set<Loop> loops;
  BriefBlockGraph blockGraph;
  LinkedList<Block> headerBlocks;

  public static final int maxOccurForHeader=2;

  public MethodLoopInfo(Body body, UnitGraph unitGraph){
    this.headerBlockStmts=new HashSet<>();

    this.loops=(new LoopFinder()).getLoops(unitGraph);
    this.blockGraph=new BriefBlockGraph(body);
    this.headerBlocks=new LinkedList<>();

    HashSet<Stmt> loopHeaders=new HashSet<>();
    for(Loop l:this.loops){
      loopHeaders.add(l.getHead());
    }

    for(Block bb:this.blockGraph){
      if(loopHeaders.contains(bb.getHead())){
        this.headerBlocks.add(bb);

        for(Unit u: bb){
          Stmt s=(Stmt) u;
          this.headerBlockStmts.add(s);
        }
      }
    }
  }
}
