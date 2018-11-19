/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

class IllegalWhileSelectionChecker extends AbstractControlFlowChecker {

	IllegalWhileSelectionChecker(List<IASTNode> nodes) {
		super(nodes);
	}

	@Override
	protected boolean isFLowControl(IASTNode node) {
		return node instanceof IASTWhileStatement;
	}

	@Override
	protected IASTNode getAncestor(IASTNode node) {
		return ASTQueries.findAncestorWithType(node, IASTWhileStatement.class);
	}

	@Override
	protected boolean isInsideBody(IASTNode ancestor, IASTNode node) {
		IASTWhileStatement whileStatement = (IASTWhileStatement) ancestor;
		return whileStatement.getBody().contains(node);
	}
}
