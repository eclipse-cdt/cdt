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
import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IJumpNode;
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.core.model.cfg.ISingleOutgoing;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;
import org.eclipse.cdt.codan.core.test.CodanTestCase;
import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.internal.core.cfg.ControlFlowGraph;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * TODO: add description
 */
public class ControlFlowGraphTest extends CodanTestCase {
	ControlFlowGraph graph;

	void processFile(IFile file) throws CoreException, InterruptedException {
		// create translation unit and access index
		ICElement model = CoreModel.getDefault().create(file);
		if (!(model instanceof ITranslationUnit))
			return; // not a C/C++ file
		ITranslationUnit tu = (ITranslationUnit) model;
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
		// lock the index for read access
		index.acquireReadLock();
		try {
			// create index based ast
			IASTTranslationUnit ast = tu.getAST(index,
					ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			// traverse the ast using the visitor pattern.
			processAst(ast);
		} finally {
			index.releaseReadLock();
		}
	}

	/**
	 * @param ast
	 */
	private void processAst(IASTTranslationUnit ast) {
		CASTVisitor visitor = new CASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

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

	void buildCfg() {
		try {
			IResource el = cproject.getProject().findMember(
					currentFile.getName());
			processFile((IFile) el);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
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
		for (Iterator<IBasicBlock> iterator = node.getIncomingIterator(); iterator
				.hasNext();) {
			IBasicBlock b = iterator.next();
			if (!contains(node, b.getOutgoingIterator()))
				fail("Block " + node + " inconsitent prev/next " + b);
		}
		for (Iterator<IBasicBlock> iterator = node.getOutgoingIterator(); iterator
				.hasNext();) {
			IBasicBlock b = iterator.next();
			if (!contains(node, b.getIncomingIterator()))
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
	private boolean contains(IBasicBlock node, Iterator<IBasicBlock> iterator) {
		for (; iterator.hasNext();) {
			IBasicBlock b = iterator.next();
			if (b.equals(node))
				return true;
		}
		return false;
	}

	/**
	 * @param file
	 */
	protected void buildAndCheck(String file) {
		load(file);
		buildCfg();
		checkCfg();
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
		for (Iterator<IBasicBlock> iterator = des.getOutgoingIterator(); iterator
				.hasNext();) {
			IBranchNode bn = (IBranchNode) iterator.next();
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

	/*-
	 <code file="test1.c">
	 main() {
	   int a;
	   a=1; 
	 }
	 </code>
	 */
	public void test_basic() {
		buildAndCheck("test1.c");
	}

	/*-
	 <code file="test2.c">
	 main() {
	   int a=10;
	   while (a--) {
	      a=a-2;
	   }
	 }
	 </code>
	 */
	public void test_while() {
		buildAndCheck("test2.c");
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

	/*-
	 <code file="test3.c">
	 main() {
	   int a=10;
	   if (a--) {
	      a=a-2;
	   }
	 }
	 </code>
	 */
	public void test_if() {
		buildAndCheck("test3.c");
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

	/*-
	 <code file="test4.c">
	 main() {
	   int a=10;
	   if (a--) {
	      return;
	   }
	 }
	 </code>
	 */
	public void test_if_ret() {
		buildAndCheck("test4.c");
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("a--", data(des));
		IExitNode bThen = (IExitNode) branchEnd(des, IBranchNode.THEN);
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m1 = jumpEnd(bElse);
	}
	/*-
	 <code file="test5.c">
	 main() {
	   int a=10;
	   if (a--) {
	      return;
	      a++;
	   }
	 }
	 </code>
	 */
	public void test_if_dead() {
		buildAndCheck("test5.c");
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("a--", data(des));
		IExitNode bThen = (IExitNode) branchEnd(des, IBranchNode.THEN);
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m1 = jumpEnd(bElse);
		assertEquals(1,graph.getUnconnectedNodeSize());
	}
	/*-
	 <code file="test_ifif.c">
	 foo() {
	   int a=10, x=5;
	   if (a--) {
	      if (x<0)
	         a++;
	   }
	 }
	 </code>
	 */
	public void test_ifif() {
		buildAndCheck("test_ifif.c");
		IStartNode startNode = graph.getStartNode();
		IPlainNode decl = (IPlainNode) startNode.getOutgoing();
		IDecisionNode des = (IDecisionNode) decl.getOutgoing();
		assertEquals("a--", data(des));
		IDecisionNode bThen = (IDecisionNode) branchEnd(des, IBranchNode.THEN);
		assertEquals("x<0", data(bThen));
		IBasicBlock bElse = branchEnd(des, IBranchNode.ELSE);
		IBasicBlock m2 = jumpEnd(branchEnd(bThen,IBranchNode.THEN));
		IBasicBlock m1 = jumpEnd(bElse);
		IBasicBlock m3 = jumpEnd(m2);
		assertSame(m1, m3);
	}
	/*-
	 <code file="test_throw.cc">
	 int foo() {
	 	throw 5;
	 }
	 </code>
	 */
	public void test_throw() {
		buildAndCheck("test_throw.cc");
		IStartNode startNode = graph.getStartNode();
		assertEquals(1, graph.getExitNodeSize());
		Iterator<IExitNode> exitNodeIterator = graph.getExitNodeIterator();
		IExitNode exit = exitNodeIterator.next();

		assertEquals("throw 5;", data(exit));
	}
	/*-
	 <code file="test_exit.c">
	 int foo() {
	 	exit(0);
	 }
	 </code>
	 */
	public void test_exit() {
		buildAndCheck("test_exit.c");
		IStartNode startNode = graph.getStartNode();
		assertEquals(1, graph.getExitNodeSize());
		Iterator<IExitNode> exitNodeIterator = graph.getExitNodeIterator();
		IExitNode exit = exitNodeIterator.next();

		assertEquals("exit(0);", data(exit));
	}
}
