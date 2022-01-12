/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;

public class CASTAmbiguousAlignmentSpecifier extends ASTAmbiguousNode implements IASTAlignmentSpecifier {
	IASTAlignmentSpecifier fExpression;
	IASTAlignmentSpecifier fTypeId;

	CASTAmbiguousAlignmentSpecifier(IASTAlignmentSpecifier expression, IASTAlignmentSpecifier typeId) {
		fExpression = expression;
		fTypeId = typeId;
	}

	@Override
	public IASTExpression getExpression() {
		return fExpression.getExpression();
	}

	@Override
	public IASTTypeId getTypeId() {
		return fTypeId.getTypeId();
	}

	@Override
	public IASTAlignmentSpecifier copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTAlignmentSpecifier copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTNode[] getNodes() {
		return new IASTNode[] { fExpression, fTypeId };
	}
}
