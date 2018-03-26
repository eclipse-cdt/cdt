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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

class IllegalGotoSelectionChecker extends AbstractSelectionChecker {
	private final List<IASTNode> nodes;
	private final Set<String> setLabel = new HashSet<String>();
	private final Map<String, Integer> mapGotoInSelection = new HashMap<String, Integer>();
	private final Map<String, Integer> mapGotoInFunction = new HashMap<String, Integer>();
	private final IASTFunctionDefinition functionDefinition;

	public IllegalGotoSelectionChecker(List<IASTNode> nodes, IASTFunctionDefinition functionDefinition) {
		this.nodes = nodes;
		this.functionDefinition = functionDefinition;
	}

	@Override
	public boolean check() {
		for (IASTNode node : nodes) {
			node.accept(new ASTVisitor() {
				{
					shouldVisitStatements = true;
				}

				@Override
				public int visit(IASTStatement statement) {
					if (statement instanceof IASTGotoStatement) {
						IASTName name = ((IASTGotoStatement) statement).getName();
						insertIntoMap(mapGotoInSelection, name);
					} else if (statement instanceof IASTLabelStatement) {
						IASTLabelStatement labelStatement = (IASTLabelStatement) statement;
						setLabel.add(labelStatement.getName().toString());
					}
					return PROCESS_CONTINUE;
				}
			});
		}
		if (!setLabel.isEmpty() || !mapGotoInSelection.isEmpty()) {
			functionDefinition.accept(new ASTVisitor() {
				{
					shouldVisitStatements = true;
				}

				@Override
				public int visit(IASTStatement statement) {
					if (statement instanceof IASTGotoStatement) {
						IASTName name = ((IASTGotoStatement) statement).getName();
						insertIntoMap(mapGotoInFunction, name);
					}
					return PROCESS_CONTINUE;
				}
			});
			for (Map.Entry<String, Integer> entry : mapGotoInSelection.entrySet()) {
				if (!setLabel.contains(entry.getKey())) {
					errorMessage = Messages.ExtractFunctionRefactoring_IllegalGotoSelection;
					return false;
				}
			}
			for (String label : setLabel) {
				if (!mapGotoInSelection.containsKey(label)
						|| !mapGotoInSelection.get(label).equals(mapGotoInFunction.get(label))) {
					errorMessage = Messages.ExtractFunctionRefactoring_IllegalGotoSelection;
					return false;
				}
			}
		}
		return true;
	}

	private void insertIntoMap(Map<String, Integer> map, IASTName name) {
		if (map.containsKey(name.toString())) {
			int count = map.get(name.toString());
			map.put(name.toString(), count + 1);
		} else {
			map.put(name.toString(), 1);
		}
	}
}
