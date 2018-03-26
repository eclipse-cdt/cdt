/*******************************************************************************
 * Copyright (c) 2008, 2017 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;

public class SelectionChecker extends AbstractSelectionChecker {
	private NodeContainer nodeContainer;
	private IASTNode firstNode;
	private IASTFunctionDefinition functionDefinition;
	private List<AbstractSelectionChecker> checkers;
	private SpecialNodeFinder specialNodeFinder;

	public SelectionChecker(NodeContainer container) {
		nodeContainer = container;
		firstNode = nodeContainer.getNodesToWrite().get(0);
		functionDefinition = ASTQueries.findAncestorWithType(firstNode, IASTFunctionDefinition.class);
		checkers = new ArrayList<AbstractSelectionChecker>();
		checkers.add(new NonExtractableStatementChecker(nodeContainer.getNodesToWrite()));
		checkers.add(new IllegalFunctionNameSelectionChecker(nodeContainer.getNodesToWrite()));
		checkers.add(new IllegalIfSelectionChecker(nodeContainer.getNodesToWrite()));
		checkers.add(new IllegalForSelectionChecker(nodeContainer.getNodesToWrite()));
		checkers.add(new IllegalDoSelectionChecker(nodeContainer.getNodesToWrite()));
		checkers.add(new IllegalRangeBasedForSelectionChecker(nodeContainer.getNodesToWrite()));
		checkers.add(new IllegalWhileSelectionChecker(nodeContainer.getNodesToWrite()));
		checkers.add(new IllegalGotoSelectionChecker(nodeContainer.getNodesToWrite(), functionDefinition));
		checkers.add(new IllegalReturnSelectionChecker(nodeContainer.getNodesToWrite()));
		checkers.add(
				new IllegalLocalTypeSelectionChecker(nodeContainer.getNodesToWrite(), functionDefinition)); //
		specialNodeFinder = new SpecialNodeFinder(nodeContainer.getNodesToWrite());
	}

	@Override
	public boolean check() {
		for (AbstractSelectionChecker checker : checkers) {
			if (!checker.check()) {
				errorMessage = checker.getErrorMessage();
				return false;
			}
		}
		specialNodeFinder.findNodes();
		return true;
	}

	public boolean hasReturnStatements() {
		return specialNodeFinder.hasReturnStatement();
	}

	public boolean isConstExpr() {
		return specialNodeFinder.isConstExpr();
	}

	public boolean isLvalue() {
		return specialNodeFinder.isLvalue();
	}

	public boolean isUnkownType() {
		return specialNodeFinder.isUnknownType();
	}

	public boolean isUnOp() {
		return specialNodeFinder.isUnOp();
	}

	public boolean isLambda() {
		return specialNodeFinder.isLambda();
	}

}