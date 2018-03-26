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
		if (nodes.size() != 1){
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
