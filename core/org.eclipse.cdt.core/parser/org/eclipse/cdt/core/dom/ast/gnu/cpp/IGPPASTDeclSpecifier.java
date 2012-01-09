/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * G++ allows for restrict to be a modifier for the decl spec.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated Replaced by {@link IASTDeclSpecifier}.
 */
@Deprecated
public interface IGPPASTDeclSpecifier extends IASTDeclSpecifier {

	/**
	 * @since 5.1
	 */
	@Override
	public IGPPASTDeclSpecifier copy();
}
