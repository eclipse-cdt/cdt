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
import org.eclipse.cdt.codan.core.test.CodanTestCase;
import org.eclipse.cdt.codan.internal.core.cfg.ControlFlowGraph;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionNode;
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
					graph = new ControlFlowGraphBuilder().build((IASTFunctionDefinition) decl);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		};
		ast.accept(visitor);
	
	}
	void buildCfg() {
		try {
		
			IResource el = cproject.getProject().findMember(currentFile.getName());
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
		for (Iterator<IBasicBlock> iterator = nodes.iterator(); iterator.hasNext();) {
		    IBasicBlock node = iterator.next();
			checkNode(node);
		}
		
	}
	/**
	 * @param node
	 */
	private void checkNode(IBasicBlock node) {
		for (Iterator<IBasicBlock> iterator = node.getIncomingIterator(); iterator.hasNext();) {
			IBasicBlock b = iterator.next();
			if (!contains(node, b.getOutgoingIterator()))
				fail("Block "+node+" inconsitent prev/next "+b);
		}
		for (Iterator<IBasicBlock> iterator = node.getOutgoingIterator(); iterator.hasNext();) {
			IBasicBlock b = iterator.next();
			if (!contains(node, b.getIncomingIterator()))
				fail("Block "+node+" inconsitent next/prev "+b);
		}
		if (node instanceof IDecisionNode) {
			assertTrue("decision node outgping size",node.getOutgoingSize()>1);
			assertNotNull(((IDecisionNode) node).getMergeNode());
		}
	}

	/**
	 * @param node
	 * @param outgoingIterator
	 * @return
	 */
	private boolean contains(IBasicBlock node,
			Iterator<IBasicBlock> iterator) {
		for (; iterator.hasNext();) {
			IBasicBlock b = iterator.next();
			if (b.equals(node)) return true;
		}
		return false;
	}

	/*-
	 <code file="test1.c">
	 main() {
	   int a;
	   a=1; 
	 }
	 </code>
	 */
	public void test1() {
		load("test1.c");
		buildCfg();
		checkCfg();
		
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
		load("test2.c");
		buildCfg();
		checkCfg();
		
	}

}
