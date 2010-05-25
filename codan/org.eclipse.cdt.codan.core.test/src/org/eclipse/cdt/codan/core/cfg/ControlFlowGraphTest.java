/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.codan.core.test.CodanFastCxxAstTestCase;
import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.internal.core.cfg.ControlFlowGraph;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * TODO: add description
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
					graph = new ControlFlowGraphBuilder()
							.build((IASTFunctionDefinition) decl);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		};
		ast.accept(visitor);
	}

	/**
	 * 
	 */
	private void checkCfg() {
		assertNotNull(graph);
		assertNotNull(graph.getStartNode());
		Collection<IBasicBlock> nodes = graph.getNodes();
		for (Iterator<IBasicBlock> iterator = nodes.iterator(); iterator
				.hasNext();) {
			IBasicBlock node = iterator.next();
			checkNode(node);
		}
	}

	/**
	 * @param node
	 */
	private void checkNode(IBasicBlock node) {
		IBasicBlock[] incomingNodes = node.getIncomingNodes();
		for (int i = 0; i < incomingNodes.length; i++) {
			IBasicBlock b = incomingNodes[i];
			if (!contains(node, b.getOutgoingNodes()))
				fail("Block " + node + " inconsitent prev/next " + b);
		}
		IBasicBlock[] outgoingNodes = node.getOutgoingNodes();
		for (int i = 0; i < outgoingNodes.length; i++) {
			IBasicBlock b = outgoingNodes[i];
			if (!contains(node, b.getIncomingNodes()))
				fail("Block " + node + " inconsitent next/prev " + b);
		}
		if (node instanceof IDecisionNode) {
			assertTrue("decision node outgping size",
					node.getOutgoingSize() > 1);
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

	//	 foo() {
	//	   int a=10, x=5;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.test.CodanFastCxxAstTestCase#getChecker()
	 */
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
		IPlainNode bThen = (IPlainNode) branchEnd(des, IBranchNode.THEN);
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
}
