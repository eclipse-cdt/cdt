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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This interface is a qualified name in C++.
 * 
 * @author jcamelon
 */
public interface ICPPASTQualifiedName extends IASTName, IASTNameOwner {

	/**
	 * Each IASTName segment has property being <code>SEGMENT_NAME</code>.
	 */
	public static final ASTNodeProperty SEGMENT_NAME = new ASTNodeProperty(
			"Segment"); //$NON-NLS-1$

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
}
