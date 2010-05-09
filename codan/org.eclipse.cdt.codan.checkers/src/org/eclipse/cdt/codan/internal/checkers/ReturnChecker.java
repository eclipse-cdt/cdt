/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.Iterator;

import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.cxx.model.CxxModelsCache;
import org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.internal.core.cfg.ICfgData;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;

/**
 * The checker suppose to find issue related to mismatched return value/function
 * declaration<br>
 * <li>Function declared as returning non-void returns void
 * <li>Function declared as returning void has non-void return
 * <li>Function declared as returning non-void has no return (requires control
 * flow graph)
 */
public class ReturnChecker extends AbstractAstFunctionChecker {
	public final String RET_NO_VALUE_ID = "org.eclipse.cdt.codan.checkers.noreturn"; //$NON-NLS-1$
	public final String RET_ERR_VALUE_ID = "org.eclipse.cdt.codan.checkers.errreturnvalue"; //$NON-NLS-1$
	public final String RET_NORET_ID = "org.eclipse.cdt.codan.checkers.errnoreturn"; //$NON-NLS-1$

	class ReturnStmpVisitor extends ASTVisitor {
		private IASTFunctionDefinition func;
		boolean hasret;

		ReturnStmpVisitor(IASTFunctionDefinition func) {
			shouldVisitStatements = true;
			this.func = func;
			this.hasret = false;
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTReturnStatement) {
				IASTReturnStatement ret = (IASTReturnStatement) stmt;
				if (!isVoid(func)) {
					if (isExplicitReturn(func)) {
						if (ret.getReturnValue() == null)
							reportProblem(RET_NO_VALUE_ID, ret);
					}
				} else {
					if (ret.getReturnValue() != null)
						reportProblem(RET_ERR_VALUE_ID, ret.getReturnValue());
				}
				hasret = true;
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker#
	 * processFunction(org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition)
	 */
	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		ReturnStmpVisitor visitor = new ReturnStmpVisitor(func);
		func.accept(visitor);
		if (!visitor.hasret) {
			// no return at all
			if (!isVoid(func) && isExplicitReturn(func)) {
				if (endsWithNoExitNode(func))
					reportProblem(RET_NORET_ID, func.getDeclSpecifier());
			}
		}
	}

	/**
	 * @param func
	 * @return
	 */
	@SuppressWarnings("restriction")
	protected boolean endsWithNoExitNode(IASTFunctionDefinition func) {
		IControlFlowGraph graph = CxxModelsCache.getInstance()
				.getControlFlowGraph(func);
		Iterator<IExitNode> exitNodeIterator = graph
				.getExitNodeIterator();
		boolean noexitop = false;
		for (; exitNodeIterator.hasNext();) {
			IExitNode node = exitNodeIterator.next();
			if (((ICfgData) node).getData() == null) {
				// if it real exit node such as return, exit or throw data
				// will be an ast node, it is null it is fake node added by the
				// graph builder
				noexitop = true;
				break;
			}
		}
		return noexitop;
	}

	/**
	 * @param func
	 * @return
	 */
	protected boolean isExplicitReturn(IASTFunctionDefinition func) {
		return getDeclSpecType(func) != ICASTSimpleDeclSpecifier.t_unspecified;
	}

	/**
	 * @param func
	 * @return
	 */
	public boolean isVoid(IASTFunctionDefinition func) {
		int type = getDeclSpecType(func);
		if (type == IASTSimpleDeclSpecifier.t_void) {
			IASTFunctionDeclarator declarator = func.getDeclarator();
			if (declarator.getPointerOperators().length == 0)
				return true;
		}
		return false;
	}

	/**
	 * @param func
	 * @return
	 */
	protected int getDeclSpecType(IASTFunctionDefinition func) {
		IASTDeclSpecifier declSpecifier = func.getDeclSpecifier();
		int type = -1;
		if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			type = ((IASTSimpleDeclSpecifier) declSpecifier).getType();
		}
		return type;
	}
}
