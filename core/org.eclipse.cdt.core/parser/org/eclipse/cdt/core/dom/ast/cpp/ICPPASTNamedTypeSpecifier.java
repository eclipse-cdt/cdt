/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;

/**
 * C++ adds the capability of qualifying a named type specifier w/the keyword
 * typename.
 * 
 * @author jcamelon
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

}
