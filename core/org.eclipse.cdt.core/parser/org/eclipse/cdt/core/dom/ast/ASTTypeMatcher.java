/*******************************************************************************
 * Copyright (c) 2008, 2012 Google, Inc and others.
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
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.parser.util.IObjectMatcher;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ASTTypeMatcher implements IObjectMatcher {
	/**
	 * Returns <code>true</code> if the two objects are equal or represent the same type.
	 */
	@Override
	public boolean isEquivalent(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 instanceof IType && o2 instanceof IType) {
			return ((IType) o1).isSameType((IType) o2);
		}
		return o1.equals(o2);
	}
}
