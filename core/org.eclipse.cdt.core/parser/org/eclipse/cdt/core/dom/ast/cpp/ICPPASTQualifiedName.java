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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This interface is a qualified name in C++.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTQualifiedName extends IASTName, IASTNameOwner {

	/**
	 * Each IASTName segment has property being <code>SEGMENT_NAME</code>.
	 */
	public static final ASTNodeProperty SEGMENT_NAME = new ASTNodeProperty(
			"ICPPASTQualifiedName.SEGMENT_NAME - An IASTName segment"); //$NON-NLS-1$

	/**
	 * Add a subname.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void addName(IASTName name);

	/**
	 * Get all subnames.
	 * 
	 * @return <code>IASTName []</code>
	 */
	public IASTName[] getNames();

	/**
	 * The last name is often semantically significant.
	 * 
	 */
	@Override
	public IASTName getLastName();
	
	/**
	 * Is this name fully qualified?
	 * 
	 * @return boolean
	 */
	public boolean isFullyQualified();

	/**
	 * Set this name to be fully qualified or not (true/false).
	 * 
	 * @param value
	 *            boolean
	 */
	public void setFullyQualified(boolean value);
	
	/**
	 * This is used to check if the ICPPASTQualifiedName's last segment is
	 * an ICPPASTConversionName or an ICPPASTOperatorName.
	 * 
	 */
	public boolean isConversionOrOperator();
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTQualifiedName copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTQualifiedName copy(CopyStyle style);
}
