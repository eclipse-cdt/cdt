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
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * G++ allows for restrict to be a modifier for the decl spec.
 * 
 * @author jcamelon
 */
public interface IGPPASTDeclSpecifier extends IASTDeclSpecifier {

	/**
	 * Was restrict keyword encountered?
	 * 
	 * @return boolean
	 */
	public boolean isRestrict();

	/**
	 * Set restrict-modifier-encountered to value.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setRestrict(boolean value);

}
