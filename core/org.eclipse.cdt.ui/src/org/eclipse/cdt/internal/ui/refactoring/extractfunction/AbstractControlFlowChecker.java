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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public abstract class AbstractControlFlowChecker extends AbstractSelectionChecker {
	private final List<IASTNode> nodes;
	private final List<IASTNode> flowControlNodes = new ArrayList<IASTNode>();
	private final Set<IASTNode> headNodes = new HashSet<IASTNode>();
	private final Set<IASTNode> bodyNodes = new HashSet<IASTNode>();
	private final Set<IASTNode> outsideNodes = new HashSet<IASTNode>();

	AbstractControlFlowChecker(List<IASTNode> nodes) {
		this.nodes = nodes;
	}

	protected enum Location {
		HEAD, BODY, OUTSIDE
	}

	protected abstract boolean isFLowControl(IASTNode node);

	protected abstract IASTNode getAncestor(IASTNode node);

	protected abstract boolean isInsideBody(IASTNode ancestor, IASTNode node);

	@Override
	public boolean check() {
		for (IASTNode node : nodes) {
			node.accept(new ASTVisitor() {
				{
					shouldVisitStatements = true;
					shouldVisitExpressions = true;
				}

				@Override
				public int visit(IASTStatement statement) {
					if (isFLowControl(statement)) {
						flowControlNodes.add(statement);
					}
					addNode(statement);
					return PROCESS_SKIP;
				}

				@Override
				public int visit(IASTExpression expression) {
					addNode(expression);
					return PROCESS_SKIP;
				}
			});
		}

		removeAllNodesInsideSelectedFlowControlStatement(headNodes);
		removeAllNodesInsideSelectedFlowControlStatement(bodyNodes);
		removeAllNodesInsideSelectedFlowControlStatement(outsideNodes);

		if (headNodes.size() > 1) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalHeadSelection;
			return false;
		} else if (!headNodes.isEmpty() && (!bodyNodes.isEmpty() || !outsideNodes.isEmpty())) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalHeadAndSomethingElseSelection;
			return false;
		} else if (!bodyNodes.isEmpty() && !outsideNodes.isEmpty()) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalBodyAndOutsideSelection;
			return false;
		} else {
			Set<IASTNode> parents = new HashSet<IASTNode>();
			for (IASTNode node : bodyNodes) {
				parents.add(node.getParent());
			}
			if (parents.size() > 1) {
				errorMessage = Messages.ExtractFunctionRefactoring_IllegalBodySelection;
				return false;
			}
			return true;
		}
	}

	protected Location getLocation(IASTNode node) {
		if (isFLowControl(node)) {
			node = node.getParent();
		}
		IASTNode ancestor = getAncestor(node);

		if (ancestor == null) {
			return Location.OUTSIDE;
		} else if (isInsideBody(ancestor, node)) {
			return Location.BODY;
		}
		return Location.HEAD;
	}

	private void addNode(IASTNode node) {
		Location location = getLocation(node);
		switch (location) {
		case HEAD:
			headNodes.add(node);
			break;
		case BODY:
			bodyNodes.add(node);
			break;
		case OUTSIDE:
		default:
			outsideNodes.add(node);
			break;
		}
	}

	private void removeAllNodesInsideSelectedFlowControlStatement(Set<IASTNode> set) {
		for (Iterator<IASTNode> iterator = set.iterator(); iterator.hasNext();) {
			IASTNode node = iterator.next();
			for (IASTNode fcNode : flowControlNodes) {
				if (fcNode.contains(node) && fcNode != node) {
					iterator.remove();
					break;
				}
			}
		}
	}
}