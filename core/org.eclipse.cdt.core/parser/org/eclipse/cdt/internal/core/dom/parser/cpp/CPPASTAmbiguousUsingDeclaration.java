/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;

public class CPPASTAmbiguousUsingDeclaration extends ASTAmbiguousNode implements IASTDeclaration {
	private final IASTDeclaration fDeclaration;

	public CPPASTAmbiguousUsingDeclaration(IASTDeclaration declaration) {
		fDeclaration = declaration;
	}

	@Override
	public IASTDeclaration copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTDeclaration copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTNode[] getNodes() {
		return new IASTNode[] { fDeclaration };
	}
}
