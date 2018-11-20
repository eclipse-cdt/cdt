/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;

/**
 * @deprecated Replaced by {@link CPPASTSimpleDeclSpecifier}
 */
@Deprecated
public class GPPASTSimpleDeclSpecifier extends CPPASTSimpleDeclSpecifier implements IGPPASTSimpleDeclSpecifier {

	public GPPASTSimpleDeclSpecifier() {
	}

	public GPPASTSimpleDeclSpecifier(IASTExpression typeofExpression) {
		super();
		setDeclTypeExpression(typeofExpression);
	}

	@Override
	public GPPASTSimpleDeclSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public GPPASTSimpleDeclSpecifier copy(CopyStyle style) {
		return copy(new GPPASTSimpleDeclSpecifier(), style);
	}

	@Override
	public void setTypeofExpression(IASTExpression typeofExpression) {
		setDeclTypeExpression(typeofExpression);
	}

	@Override
	public IASTExpression getTypeofExpression() {
		return getDeclTypeExpression();
	}
}
