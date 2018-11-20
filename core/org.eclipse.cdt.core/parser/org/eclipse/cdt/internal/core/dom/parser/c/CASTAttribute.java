/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttribute;

/**
 * C-specific attribute.
 */
public class CASTAttribute extends ASTAttribute {

	public CASTAttribute(char[] name, IASTToken argumentClause) {
		super(name, argumentClause);
	}

	@Override
	public CASTAttribute copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTAttribute copy(CopyStyle style) {
		IASTToken argumentClause = getArgumentClause();
		if (argumentClause != null)
			argumentClause = argumentClause.copy(style);
		return copy(new CASTAttribute(getName(), argumentClause), style);
	}
}
