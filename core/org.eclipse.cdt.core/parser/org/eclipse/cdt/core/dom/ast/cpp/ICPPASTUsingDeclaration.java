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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This interface represents a using declaration.
 * 
 * @author jcamelon
 */
public interface ICPPASTUsingDeclaration extends IASTDeclaration, IASTNameOwner {

	/**
	 * <code>NAME</code> is the qualified name brought into scope.
	 */
	public static final ASTNodeProperty NAME = new ASTNodeProperty("Name"); //$NON-NLS-1$

	/**
	 * Was the typename keyword used?
	 * 
	 * @param value
	 *            boolean
	 */
	public void setIsTypename(boolean value);

	/**
	 * Set that the typename keyword was/wasn't used.
	 * 
	 * @return boolean
	 */
	public boolean isTypename();

	/**
	 * Get the name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set the name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

}
