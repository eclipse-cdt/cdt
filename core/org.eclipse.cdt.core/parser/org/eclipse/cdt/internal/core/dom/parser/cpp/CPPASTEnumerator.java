/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.ASTEnumerator;

/**
 * C++-specific enumerator.
 */
public class CPPASTEnumerator extends ASTEnumerator {

	public CPPASTEnumerator() {
		super();
	}

	public CPPASTEnumerator(IASTName name, IASTExpression value) {
		super(name, value);
	}

	@Override
	public CPPASTEnumerator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTEnumerator copy(CopyStyle style) {
		return copy(new CPPASTEnumerator(), style);
	}
}
