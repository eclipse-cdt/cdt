/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;

/**
 * C++ adds the capability of qualifying a named type specifier w/the keyword
 * typename.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTNamedTypeSpecifier extends IASTNamedTypeSpecifier,
		ICPPASTDeclSpecifier {

	/**
	 * Was typename token consumed?
	 * 
	 * @return boolean
	 */
	public boolean isTypename();

	/**
	 * Set this value.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setIsTypename(boolean value);
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTNamedTypeSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTNamedTypeSpecifier copy(CopyStyle style);

}
