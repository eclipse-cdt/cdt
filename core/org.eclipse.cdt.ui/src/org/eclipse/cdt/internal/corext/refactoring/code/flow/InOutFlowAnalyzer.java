/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class InOutFlowAnalyzer extends FlowAnalyzer {

	public InOutFlowAnalyzer(FlowContext context) {
		super(context);
	}

	public FlowInfo perform(IASTNode[] selectedNodes) {
		FlowContext context= getFlowContext();
		GenericSequentialFlowInfo result= createSequential();
		for (int i= 0; i < selectedNodes.length; i++) {
			IASTNode node= selectedNodes[i];
			node.accept(this);
			result.merge(getFlowInfo(node), context);
		}
		return result;
	}

	@Override
	protected boolean traverseNode(IASTNode node) {
		// We are only traversing the selected nodes.
		return true;
	}

	@Override
	protected boolean createReturnFlowInfo(IASTReturnStatement node) {
		// We are only traversing selected nodes.
		return true;
	}

	@Override
	public int leave(IASTCompoundStatement node) {
		super.leave(node);
		clearAccessMode(accessFlowInfo(node), node.getStatements());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(ICPPASTCatchHandler node) {
		super.leave(node);
		clearAccessMode(accessFlowInfo(node), node.getDeclaration());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTForStatement node) {
		super.leave(node);
		clearAccessMode(accessFlowInfo(node), node.getInitializerStatement());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(ICPPASTRangeBasedForStatement node) {
		super.leave(node);
		clearAccessMode(accessFlowInfo(node), node.getDeclaration());
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTFunctionDefinition node) {
		super.leave(node);
		FlowInfo info= accessFlowInfo(node);
		IASTFunctionDeclarator declarator = node.getDeclarator();
		if (declarator instanceof IASTStandardFunctionDeclarator) {
			for (IASTParameterDeclaration param : ((IASTStandardFunctionDeclarator) declarator).getParameters()) {
				clearAccessMode(info, param.getDeclarator());
			}
		} else if (declarator instanceof ICASTKnRFunctionDeclarator) {
			for (IASTDeclaration param : ((ICASTKnRFunctionDeclarator) declarator).getParameterDeclarations()) {
				clearAccessMode(info, param);
			}
		}
		return PROCESS_SKIP;
	}

	private void clearAccessMode(FlowInfo info, IASTStatement[] statements) {
		if (statements == null || statements.length == 0 || info == null)
			return;
		for (IASTStatement statement : statements) {
			clearAccessMode(info, statement);
		}
	}

	private void clearAccessMode(FlowInfo info, IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {
			IASTDeclaration declaration = ((IASTDeclarationStatement) statement).getDeclaration();
			clearAccessMode(info, declaration);
		}
	}

	private void clearAccessMode(FlowInfo info, IASTDeclaration declaration) {
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTDeclarator[] declarators = ((IASTSimpleDeclaration) declaration).getDeclarators();
			for (IASTDeclarator declarator : declarators) {
				clearAccessMode(info, declarator);
			}
		}
	}

	private void clearAccessMode(FlowInfo info, IASTDeclarator declarator) {
		declarator = CPPVisitor.findInnermostDeclarator(declarator);
		IASTName name = declarator.getName();
		IBinding binding= name.resolveBinding();
		if (binding instanceof IVariable && !(binding instanceof IField))
			info.clearAccessMode((IVariable) binding, fFlowContext);
	}
}

