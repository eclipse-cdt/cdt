/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTFieldReference;

/**
 * Certain field references in C++ require the use the keyword template to
 * specify the parse.
 * 
 * @author jcamelon
 */
public interface ICPPASTFieldReference extends IASTFieldReference {

	/**
	 * Was template keyword used?
	 * 
	 * @return
	 */
	public boolean isTemplate();

	/**
	 * Set the template keyword used.
	 * 
	 * @param value
	 */
	public void setIsTemplate(boolean value);

}
