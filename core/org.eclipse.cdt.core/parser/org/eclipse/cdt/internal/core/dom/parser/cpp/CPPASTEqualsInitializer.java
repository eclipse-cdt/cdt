/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.ASTEqualsInitializer;

/**
 * Initializer with equals sign (copy initialization)
 */
public class CPPASTEqualsInitializer extends ASTEqualsInitializer {
    public CPPASTEqualsInitializer() {
	}

	public CPPASTEqualsInitializer(IASTInitializerClause arg) {
		super(arg);
	}

	@Override
	public CPPASTEqualsInitializer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTEqualsInitializer copy(CopyStyle style) {
		IASTInitializerClause arg = getInitializerClause();
		CPPASTEqualsInitializer copy = new CPPASTEqualsInitializer(arg == null ? null
				: arg.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
}
