/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

class IllegalForSelectionChecker extends AbstractControlFlowChecker {

	IllegalForSelectionChecker(List<IASTNode> nodes) {
		super(nodes);
	}

	@Override
	protected boolean isFLowControl(IASTNode node) {
		return node instanceof IASTForStatement;
	}

	@Override
	protected IASTNode getAncestor(IASTNode node) {
		return ASTQueries.findAncestorWithType(node, IASTForStatement.class);
	}

	@Override
	protected boolean isInsideBody(IASTNode ancestor, IASTNode node) {
		IASTForStatement forStatement = (IASTForStatement) ancestor;
		return forStatement.getBody().contains(node);
	}
}
