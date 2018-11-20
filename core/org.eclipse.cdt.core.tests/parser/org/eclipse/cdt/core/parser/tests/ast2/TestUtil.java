/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
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
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class TestUtil {
	/**
	 * Searches the AST upward from the given starting node to find the
	 * nearest IASTImplicitNameOwner and returns its first implicit name,
	 * or null if it has no implicit names.
	 */
	public static IASTName findImplicitName(IASTNode node) {
		while (node != null) {
			if (node instanceof IASTImplicitNameOwner) {
				IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) node).getImplicitNames();
				if (implicitNames != null && implicitNames.length > 0) {
					return implicitNames[0];
				}
			}
			node = node.getParent();
		}
		return null;
	}
}
