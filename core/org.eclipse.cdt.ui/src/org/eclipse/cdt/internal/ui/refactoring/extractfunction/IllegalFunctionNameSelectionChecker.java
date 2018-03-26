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

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

class IllegalFunctionNameSelectionChecker extends AbstractSelectionChecker {
	private final List<IASTNode> nodes;

	public IllegalFunctionNameSelectionChecker(List<IASTNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public boolean check() {
		if (nodes.size() != 1) {
			return true;
		}
		IASTNode node = nodes.get(0);
		if (node instanceof IASTIdExpression && node.getParent() instanceof IASTFunctionCallExpression) {
			errorMessage = Messages.ExtractFunctionRefactoring_IllegalFunctionNameSelection;
			return false;
		}
		return true;
	}

}
