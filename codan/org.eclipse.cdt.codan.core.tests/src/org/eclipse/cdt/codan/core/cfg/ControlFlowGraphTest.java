/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cfg;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.cxx.internal.model.cfg.ControlFlowGraphBuilder;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IJumpNode;
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.core.model.cfg.ISingleOutgoing;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;
import org.eclipse.cdt.codan.core.tests.CodanFastCxxAstTestCase;
import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.internal.core.cfg.ControlFlowGraph;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * Tests for ControlFlowGraph
 */
public class ControlFlowGraphTest extends CodanFastCxxAstTestCase {
	ControlFlowGraph graph;

	/**
	 * @param ast
	 */
	private void processAst(IASTTranslationUnit ast) {
		CASTVisitor visitor = new CASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration decl) {
				if (decl instanceof IASTFunctionDefinition) {
					graph = new ControlFlowGraphBuilder().build((IASTFunctionDefinition) decl);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		};
		ast.accept(visitor);
	}

	private void checkCfg() {
		checkCfg(true);
	}

	/**
	 *
	 */
	private void checkCfg(boolean decision) {
		assertNotNull(graph);
		assertNotNull(graph.getStartNode());
		Collection<IBasicBlock> nodes = graph.getNodes();
		for (Iterator<IBasicBlock> iterator = nodes.iterator(); iterator.hasNext();) {
			IBasicBlock node = iterator.next();
			checkNode(node, decision);
		}
	}

	/**
	 * @param node
	 */
	private void checkNode(IBasicBlock node, boolean decision) {
		IBasicBlock[] incomingNodes = node.getIncomingNodes();
		nodes: for (int i = 0; i < incomingNodes.length; i++) {
			IBasicBlock b = incomingNodes[i];
			if (b == null) {
				if (node instanceof IBranchNode)
					continue nodes;
				// check if dead node
				Iterator<IBasicBlock> iterator = graph.getUnconnectedNodeIterator();
				boolean dead = false;
				for (; iterator.hasNext();) {
					IBasicBlock d = iterator.next();
					if (node == d) {
						dead = true;
						break;
					}
				}
				if (!dead)
					fail("Block " + node + " prev is null");
			} else if (!contains(node, b.getOutgoingNodes()))
				fail("Block " + node + " inconsitent prev/next " + b);
		}
		IBasicBlock[] outgoingNodes = node.getOutgoingNodes();
		for (int i = 0; i < outgoingNodes.length; i++) {
			IBasicBlock b = outgoingNodes[i];
			if (b == null)
				fail("Block " + node + " next is null");
			if (!contains(node, b.getIncomingNodes()))
				fail("Block " + node + " inconsitent next/prev " + b);
		}
		if (node instanceof IDecisionNode && decision) {
			assertTrue("decision node outgoing size " + node.getOutgoingSize(), node.getOutgoingSize() >= 1);
			assertNotNull(((IDecisionNode) node).getMergeNode());
		}
	}

	/**
	 * @param node
	 * @param outgoingIterator
	 * @return
	 */
	private boolean contains(IBasicBlock node, IBasicBlock[] blocks) {
		for (int i = 0; i < blocks.length; i++) {
			IBasicBlock b = blocks[i];
			if (b.equals(node))
				return true;
		}
		return false;
	}

	protected void buildAndCheck(String code) {
		buildAndCheck(code, false);
	}

	protected void buildAndCheck_cpp(String code) {
		buildAndCheck(code, true);
	}

	/**
	 * @param file
	 */
	protected void buildAndCheck(String code, boolean cpp) {
		buildCfg(code, cpp);
		checkCfg();
	}

	/**
	 * @param code
	 * @param cpp
	 */
	private void buildCfg(String code, boolean cpp) {
		parse(code, cpp ? ParserLanguage.CPP : ParserLanguage.C, true);
		processAst(tu);
	}

	private void buildCfg_C(String code) {
		parse(code, ParserLanguage.C, true);
		processAst(tu);
	}

	private void buildCfg_CPP(String code) {
		parse(code, ParserLanguage.CPP, true);
		processAst(tu);
	}

	/**
	 * @param des
	 * @return
	 */
	private String data(IBasicBlock des) {
		return ((AbstractBasicBlock) des).toStringData();
	}

	/**
	 * Return first node after the branch
	 *
	 * @param des
	 * @return
	 */
	private IBasicBlock branchEnd(IDecisionNode des, String label) {
		IBasicBlock[] outgoingNodes = des.getOutgoingNodes();
		for (int i = 0; i < outgoingNodes.length; i++) {
			IBasicBlock iBasicBlock = outgoingNodes[i];
			IBranchNode bn = (IBranchNode) iBasicBlock;
			if (label.equals(bn.getLabel()))
				return bn.getOutgoing();
		}
		return null;
	}

	/**
	 * Return node where control jumps, following the chain until jump is hit
	 *
	 * @param a
	 * @return
	 */
	private IBasicBlock jumpEnd(IBasicBlock a) {
		if (a instanceof IJumpNode)
			return ((IJumpNode) a).getOutgoing();
		if (a instanceof ISingleOutgoing)
			return jumpEnd(((ISingleOutgoing) a).getOutgoing());
		return null;
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//	 main() {
	//	   int a;
	//	   a=1;
	//	 }
	public void test_basic() {
		buildAndCheck(getAboveComment());
	}

	//	 main() {
	//	   int a=10;
	//	   while (a--) {
	//	      a=a-2;
	//	   }
	//	 }
	public void test_while() {
		buildAndCheck(getAboveComment());
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IConnectorNode conn = (IConnectorNode) decl.getOutgoing();
		IDecisionNode des = (IDecisionNode) conn.getOutgoing();
		assertEquals("a--", data(des));
		IPlainNode bThen = (IPlainNode) branchEnd(des, IBranchNode.THEN);
		assertEquals("a=a-2;", data(bThen));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m2 = jumpEnd(bThen);
		IBasicBlock m1 = jumpEnd(bElse);
		assertSame(conn, m2);
		IExitNode ret = (IExitNode) ((IConnectorNode) m1).getOutgoing();
	}

	//	 main() {
	//	   int a=10;
	//	   if (a--) {
	//	      a=a-2;
	//	   }
	//	 }
	public void test_if() {
		buildAndCheck(getAboveComment());
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("a--", data(des));
		IPlainNode bThen = (IPlainNode) branchEnd(des, IBranchNode.THEN);
		assertEquals("a=a-2;", data(bThen));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m2 = jumpEnd(bThen);
		IBasicBlock m1 = jumpEnd(bElse);
		assertSame(m1, m2);
	}

	//	 main() {
	//	   int a=10;
	//	   if (a--) {
	//	      return;
	//	   }
	//	 }
	public void test_if_ret() {
		buildAndCheck(getAboveComment());
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("a--", data(des));
		IExitNode bThen = (IExitNode) branchEnd(des, IBranchNode.THEN);
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m1 = jumpEnd(bElse);
	}

	//	 main() {
	//	      return;
	//	      a++;
	//	 }
	public void test_dead() {
		buildCfg(getAboveComment(), false);
		IStartNode startNode = graph.getStartNode();
		IExitNode ret = (IExitNode) startNode.getOutgoing();
		assertEquals(1, graph.getUnconnectedNodeSize());
	}

	//	 main() {
	//	   int a=10;
	//	   if (a--) {
	//	      return;
	//	      a++;
	//	   }
	//	 }
	public void test_if_dead() {
		buildCfg(getAboveComment(), false);
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("a--", data(des));
		IExitNode bThen = (IExitNode) branchEnd(des, IBranchNode.THEN);
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m1 = jumpEnd(bElse);
		assertEquals(1, graph.getUnconnectedNodeSize());
	}

	//	 foo(int x) {
	//	   int a=10;
	//	   if (a--) {
	//	      if (x<0)
	//	         a++;
	//	   }
	//	 }
	public void test_ifif() {
		buildAndCheck(getAboveComment());
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("a--", data(des));
		IDecisionNode bThen = (IDecisionNode) branchEnd(des, IBranchNode.THEN);
		assertEquals("x<0", data(bThen));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m2 = jumpEnd(branchEnd(bThen, IBranchNode.THEN));
		IBasicBlock m1 = jumpEnd(bElse);
		IBasicBlock m3 = jumpEnd(m2);
		assertSame(m1, m3);
	}

	//	 int foo() {
	//	 	throw 5;
	//	 }
	public void test_throw() {
		buildAndCheck_cpp(getAboveComment());
		IStartNode startNode = graph.getStartNode();
		assertEquals(1, graph.getExitNodeSize());
		Iterator<IExitNode> exitNodeIterator = graph.getExitNodeIterator();
		IExitNode exit = exitNodeIterator.next();
		assertEquals("throw 5;", data(exit));
	}

	//	 int foo() {
	//	 	exit(0);
	//	 }
	public void test_exit() {
		buildAndCheck(getAboveComment());
		IStartNode startNode = graph.getStartNode();
		assertEquals(1, graph.getExitNodeSize());
		Iterator<IExitNode> exitNodeIterator = graph.getExitNodeIterator();
		IExitNode exit = exitNodeIterator.next();
		assertEquals("exit(0);", data(exit));
	}

	@Override
	public IChecker getChecker() {
		return null;
	}

	//	 int foo() {
	// void * p;
	//	 	try {
	//          *p = 1;
	//      } catch (int e) {
	//      };
	//	 }
	public void test_try() {
		buildAndCheck_cpp(getAboveComment());
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		//assertEquals("", data(des));
		IPlainNode bThen = (IPlainNode) branchEnd(des, IBranchNode.TRY_BODY);
		assertEquals("*p = 1;", data(bThen));
		IBasicBlock bElse = null;
		IBasicBlock[] outgoingNodes = des.getOutgoingNodes();
		for (int i = 1; i < outgoingNodes.length; i++) {
			IBasicBlock iBasicBlock = outgoingNodes[i];
			IBranchNode bn = (IBranchNode) iBasicBlock;
			bElse = bn;
		}
		IBasicBlock m2 = jumpEnd(bThen);
		IBasicBlock m1 = jumpEnd(bElse);
		assertSame(m1, m2);
	}

	//	 foo() {
	//	   switch (0) {
	//     case 1: ;
	//	   }
	//	 }
	public void test_switch1() {
		buildCfg(getAboveComment(), false);
		checkCfg(false);
	}

	//	 foo() {
	//	   switch (0) {
	//     case 1: break;
	//	   }
	//	 }
	public void test_switchbreak() {
		buildCfg(getAboveComment(), false);
		checkCfg(false);
	}

	//	 foo() {
	//	   switch (0) {
	//	         a++;
	//	   }
	//	 }
	public void test_switchdead() {
		buildCfg(getAboveComment(), false);
		checkCfg(false);
		IStartNode startNode = graph.getStartNode();
		assertEquals(1, graph.getUnconnectedNodeSize());
	}

	//	 foo() {
	//	   int a=10,x=5;
	//	   if (x<0)
	//	       a++;
	//	 }
	public void test_deadbranch() {
		buildCfg(getAboveComment(), false);
		checkCfg(false);
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("x<0", data(des));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m2 = jumpEnd(branchEnd(des, IBranchNode.THEN));
		IBasicBlock m1 = jumpEnd(bElse);
		assertNull(m2);
		assertNotNull(m1);
	}

	//	int test1_f()
	//	{
	//	    while (1)
	//	    {
	//	    }
	//	}
	public void test_infiniloop() {
		buildCfg_C(getAboveComment());
		checkCfg(false);
		IStartNode startNode = graph.getStartNode();
		IConnectorNode conn = (IConnectorNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) conn.getOutgoing();
		assertEquals("1", data(des));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock bThen = branchEnd(des, IBranchNode.THEN);
		IBasicBlock m2 = jumpEnd(bThen);
		IBasicBlock m1 = jumpEnd(bElse);
		assertNotNull(m2);
		assertNull(m1);
	}

	//	void foo() {
	//    for (int i=0;i<N;i++) {
	//        bar();
	//    }
	//}
	public void test_for() {
		buildCfg_CPP(getAboveComment());
		checkCfg(false);
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IConnectorNode conn = (IConnectorNode) decl.getOutgoing();
		IDecisionNode des = (IDecisionNode) conn.getOutgoing();
		assertEquals("i<N", data(des));
		IPlainNode bThen = (IPlainNode) branchEnd(des, IBranchNode.THEN);
		assertEquals("bar();", data(bThen));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m1 = jumpEnd(bElse);
		IBasicBlock m2 = bThen.getOutgoing();
		assertNotNull(m1);
		assertSame(conn, jumpEnd(bThen));
		assertEquals("i++", data(((IConnectorNode) m2).getOutgoing()));
	}

	//	void foo() {
	//	    for (int i : arr) {
	//	        bar();
	//	    }
	//	}
	public void test_range_loop() {
		buildCfg_CPP(getAboveComment());
		checkCfg(false);
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IConnectorNode conn = (IConnectorNode) decl.getOutgoing();
		IDecisionNode des = (IDecisionNode) conn.getOutgoing();
		assertEquals("arr", data(des)); // condition
		IPlainNode bThen = (IPlainNode) branchEnd(des, IBranchNode.THEN);
		assertEquals("bar();", data(bThen));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		assertNotNull(bElse);
		IBasicBlock m2 = bThen.getOutgoing();
		IBasicBlock m1 = jumpEnd(bElse);
		assertNotNull(m1);
		assertSame(conn, jumpEnd(bThen));
		assertEquals("", data(((IConnectorNode) m2).getOutgoing())); // increment
	}

	//		 int main() {
	//		   goto b;
	//	     a:
	//	      return 2;
	//	     b:
	//	      goto a;
	//		 }
	public void test_bad_labels() {
		buildAndCheck(getAboveComment());
		assertEquals(0, graph.getUnconnectedNodeSize());
		IStartNode startNode = graph.getStartNode();
		IJumpNode gotoB = (IJumpNode) startNode.getOutgoing();
		IConnectorNode bConn = gotoB.getJumpNode();
		IJumpNode gotoA = (IJumpNode) bConn.getOutgoing();
		IConnectorNode aConn = gotoA.getJumpNode();
		IExitNode ret = (IExitNode) aConn.getOutgoing();
		assertEquals("return 2;", data(ret));
	}

	//	 int main(int a) {
	//	   goto b;
	//     if (a)   return 2;
	//  b:
	//   return 1;
	//	 }
	public void test_labels_if() {
		buildAndCheck(getAboveComment());
		assertEquals(1, graph.getUnconnectedNodeSize()); // if is dead code
	}

	//	 int main(int a) {
	//	   goto b;
	//     b:;
	//	 }
	public void test_goto() {
		buildAndCheck(getAboveComment());
		assertEquals(0, graph.getUnconnectedNodeSize());
	}

	//	 int main() {
	//	   return 1;
	//  a:
	//   return 2;
	//  b:
	//   goto a;
	//	 }
	public void test_dead_labels() {
		buildAndCheck(getAboveComment());
		assertEquals(1, graph.getUnconnectedNodeSize());
		IStartNode startNode = graph.getStartNode();
		IExitNode ret = (IExitNode) startNode.getOutgoing();
		assertEquals("return 1;", data(ret));
		IBranchNode labelB = (IBranchNode) graph.getUnconnectedNodeIterator().next(); // BranchNode: b:
		Collection<IBasicBlock> res = graph.getDeadNodes();
		assertEquals(6, res.size());

		IJumpNode gotoA = (IJumpNode) ((IConnectorNode) labelB.getOutgoing()).getOutgoing();
		IConnectorNode aConn = gotoA.getJumpNode();
		IExitNode ret2 = (IExitNode) aConn.getOutgoing();
		assertEquals("return 2;", data(ret2));
		assertTrue(res.contains(gotoA));// goto a;
		assertTrue(res.contains(aConn));
		assertTrue(res.contains(ret2)); // return 2;
		assertTrue(res.contains(ret2.getIncoming())); // Branch Node: a:
	}

	//	 int main(int a) {
	//      if (a) {
	//         return 1;
	//      } else {
	//         return 2;
	//      }
	//      a++;
	//	 }
	public void test_dead_connector() {
		buildAndCheck(getAboveComment());
		assertEquals(1, graph.getUnconnectedNodeSize());
		IConnectorNode connIf = (IConnectorNode) graph.getUnconnectedNodeIterator().next();
		assertEquals("a++;", data(connIf.getOutgoing()));
	}

	//	 int main(int a) {
	//      for (;1;a++) {
	//         return 1;
	//      }
	//      a--;
	//	 }
	public void test_dead_connector_for() {
		buildAndCheck(getAboveComment());
		assertEquals(2, graph.getUnconnectedNodeSize());
		IConnectorNode conn = (IConnectorNode) graph.getUnconnectedNodeIterator().next();
		assertEquals("a++", data(conn.getOutgoing()));
	}

	//	 int main(int a) {
	//      while (0) {
	//         return 1;
	//      }
	//
	//      a++;
	//	 }
	public void test_dead_connector_while() {
		buildAndCheck(getAboveComment());
		assertEquals(1, graph.getUnconnectedNodeSize());
		IBranchNode trueBranch = (IBranchNode) graph.getUnconnectedNodeIterator().next();
		assertEquals("return 1;", data(trueBranch.getOutgoing()));
	}

	//	int foo(int x) {
	//	    switch (x) {
	//	        case 0:
	//	            return 42;;
	//	        default:
	//	    }
	//	}
	public void test_dead_statement_in_switch() throws Exception {
		buildAndCheck(getAboveComment());
		IDecisionNode swittch = (IDecisionNode) graph.getStartNode().getOutgoing();
		Collection<IBasicBlock> deadNodes = graph.getDeadNodes();
		// Make sure the switch statement's merge node has not been marked as dead.
		assertFalse(deadNodes.contains(swittch.getMergeNode()));
	}

	//	int main(int a) {
	//		switch (a) {
	//			case 1: {
	//				break;
	//			}
	//			case 2: {
	//				break;
	//			}
	//		}
	//	}
	public void test_switch_break_in_compound_statement() {
		// Test that the target node of the jump for the 'break' in a case
		// is the connector node at the end of the switch, not the connector
		// node for the next case.
		buildAndCheck(getAboveComment());
		IDecisionNode swittch = (IDecisionNode) graph.getStartNode().getOutgoing();
		IPlainNode case1Branch = (IPlainNode) swittch.getOutgoingNodes()[0];
		IJumpNode case1Jump = (IJumpNode) case1Branch.getOutgoing();
		assertEquals(swittch.getMergeNode(), case1Jump.getJumpNode());
	}

	//	int main(int a) {
	//		switch (a) {
	//		}
	//	}
	public void test_empty_switch() {
		buildAndCheck(getAboveComment());
		// Decision node should be optimized away entirely
		assertFalse(graph.getStartNode() instanceof IDecisionNode);
	}

	//	int main(int a) {
	//		switch (a) {
	//			case 1: {
	//				break;
	//			}
	//		}
	//	}
	public void test_switch_no_explicit_default() {
		buildAndCheck(getAboveComment());
		IDecisionNode swittch = (IDecisionNode) graph.getStartNode().getOutgoing();
		assertTrue(swittch.getOutgoingSize() == 2);
	}
}
