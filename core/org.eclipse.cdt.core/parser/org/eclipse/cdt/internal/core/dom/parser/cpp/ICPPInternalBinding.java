/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;

/**
 * Non API methods for C++ bindings.
 */
public interface ICPPInternalBinding extends ICPPBinding {
	/**
	 * Returns the definition of the binding, or {@code null} if the binding doesn't have
	 * a definition.
	 */
	IASTNode getDefinition();

	/**
	 * Returns an array of declarations or {@code null} if the binding has no declarations.
	 * Implementors must keep the node with the lowest offset in declarations[0].
	 */
	IASTNode[] getDeclarations();

	void addDefinition(IASTNode node);

	void addDeclaration(IASTNode node);
}
