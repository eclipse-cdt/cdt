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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.ASTEqualsInitializer;

/**
 * Initializer with equals sign (copy initialization)
 */
public class CASTEqualsInitializer extends ASTEqualsInitializer {
    public CASTEqualsInitializer() {
	}

	public CASTEqualsInitializer(IASTInitializerClause arg) {
		super(arg);
	}

	@Override
	public CASTEqualsInitializer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTEqualsInitializer copy(CopyStyle style) {
		IASTInitializerClause arg = getInitializerClause();
		CASTEqualsInitializer copy = new CASTEqualsInitializer(arg == null ? null : arg.copy(style));
		return copy(copy, style);
	}
}
