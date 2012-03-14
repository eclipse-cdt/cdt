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
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.corext.refactoring.code.flow.Selection;

/**
 * Analyzer to check if a selection covers a valid set of statements of an abstract syntax
 * tree. The selection is valid iff
 * <ul>
 * 	<li>it does not start or end in the middle of a comment.</li>
 * 	<li>no extract characters except the empty statement ";" is included in the selection.</li>
 * </ul>
 */
public class StatementAnalyzer extends SelectionAnalyzer {
	protected ITranslationUnit fTranslationUnit;
	private final RefactoringStatus fStatus;

	public StatementAnalyzer(ITranslationUnit tu, Selection selection, boolean traverseSelectedNode)
			throws CoreException {
		super(selection, traverseSelectedNode);
		Assert.isNotNull(tu);
		this.fTranslationUnit= tu;
		this.fStatus= new RefactoringStatus();
	}

	protected void checkSelectedNodes() {
	}

	public RefactoringStatus getStatus() {
		return fStatus;
	}

	protected ITranslationUnit getTranslationUnit() {
		return fTranslationUnit;
	}

	@Override
	public int leave(IASTTranslationUnit node) {
		checkSelectedNodes();
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTStatement node) {
		if (node instanceof IASTDoStatement) {
			leave((IASTDoStatement) node);
		} else if (node instanceof IASTForStatement) {
			leave((IASTForStatement) node);
		} else if (node instanceof IASTSwitchStatement) {
			leave((IASTSwitchStatement) node);
		} else if (node instanceof IASTWhileStatement) {
			leave((IASTWhileStatement) node);
		} else if (node instanceof ICPPASTTryBlockStatement) {
			leave((ICPPASTTryBlockStatement) node);
		}
		return PROCESS_CONTINUE;
	}

	private void leave(IASTDoStatement node) {
		IASTNode[] selectedNodes= getSelectedNodes();
		if (doAfterValidation(node, selectedNodes)) {
			if (contains(selectedNodes, node.getBody()) && contains(selectedNodes, node.getCondition())) {
				invalidSelection(Messages.StatementAnalyzer_do_body_expression);
			}
		}
	}
	
	private void leave(IASTForStatement node) {
		IASTNode[] selectedNodes= getSelectedNodes();
		if (doAfterValidation(node, selectedNodes)) {
			boolean hasConditionPart= contains(selectedNodes, node.getConditionExpression());
			boolean hasIterationPart= contains(selectedNodes, node.getIterationExpression());
			if (contains(selectedNodes, node.getInitializerStatement()) && hasConditionPart) {
				invalidSelection(Messages.StatementAnalyzer_for_initializer_expression);
			} else if (hasConditionPart && hasIterationPart) {
				invalidSelection(Messages.StatementAnalyzer_for_expression_updater);
			} else if (hasIterationPart && contains(selectedNodes, node.getBody())) {
				invalidSelection(Messages.StatementAnalyzer_for_updater_body);
			}
		}
	}
	
	private void leave(ICPPASTTryBlockStatement node) {
		IASTNode firstSelectedNode= getFirstSelectedNode();
		if (getSelection().getEndVisitSelectionMode(node) == Selection.AFTER) {
			if (firstSelectedNode == node.getTryBody()) {
				invalidSelection(Messages.StatementAnalyzer_try_statement);
			} else {
				ICPPASTCatchHandler[] catchHandlers = node.getCatchHandlers();
				for (ICPPASTCatchHandler catchHandler :  catchHandlers) {
					if (catchHandler == firstSelectedNode || catchHandler.getCatchBody() == firstSelectedNode) {
						invalidSelection(Messages.StatementAnalyzer_try_statement);
					} else if (catchHandler.getDeclaration() == firstSelectedNode) {
						invalidSelection(Messages.StatementAnalyzer_catch_argument);
					}
				}
			}
		}
	}
	
	private void leave(IASTSwitchStatement node) {
		IASTNode[] selectedNodes= getSelectedNodes();
		IASTStatement body = node.getBody();
		IASTNode parent = body instanceof IASTCompoundStatement ? body : node;
		if (doAfterValidation(parent, selectedNodes)) {
			for (IASTNode n : selectedNodes) {
				if (n.getParent() == parent && 
						(n instanceof IASTCaseStatement || n instanceof IASTDefaultStatement)) {
					invalidSelection(Messages.StatementAnalyzer_switch_statement);
					break;
				}
			}
		}
	}
	
	private void leave(IASTWhileStatement node) {
		IASTNode[] selectedNodes= getSelectedNodes();
		if (doAfterValidation(node, selectedNodes)) {
			if (contains(selectedNodes, node.getCondition()) && contains(selectedNodes, node.getBody())) {
				invalidSelection(Messages.StatementAnalyzer_while_expression_body);
			}
		}
	}

	private boolean doAfterValidation(IASTNode node, IASTNode[] selectedNodes) {
		return selectedNodes.length > 0 && node == selectedNodes[0].getParent() && getSelection().getEndVisitSelectionMode(node) == Selection.AFTER;
	}

	protected void invalidSelection(String message) {
		fStatus.addFatalError(message);
		reset();
	}

	protected void invalidSelection(String message, RefactoringStatusContext context) {
		fStatus.addFatalError(message, context);
		reset();
	}

	protected static boolean contains(IASTNode[] nodes, IASTNode node) {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] == node)
				return true;
		}
		return false;
	}

	protected static boolean contains(IASTNode[] nodes, List<IASTExpression> list) {
		for (int i = 0; i < nodes.length; i++) {
			if (list.contains(nodes[i]))
				return true;
		}
		return false;
	}
}
