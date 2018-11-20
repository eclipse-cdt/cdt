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
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;

/**
 * @deprecated
 */
@Deprecated
public class GPPASTPointer extends CPPASTPointer implements IGPPASTPointer {

	@Override
	public GPPASTPointer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public GPPASTPointer copy(CopyStyle style) {
		GPPASTPointer copy = new GPPASTPointer();
		copy.setConst(isConst());
		copy.setVolatile(isVolatile());
		copy.setRestrict(isRestrict());
		return copy(copy, style);
	}
}
