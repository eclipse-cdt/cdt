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

/**
 * This interface represents a namespace alias in C++. e.g. namespace ABC { int
 * x; } namspace DEF = ABC;
 * 
 * @author jcamelon
 */
public interface ICPPASTNamespaceAlias extends IASTDeclaration {

	/**
	 * <code>ALIAS_NAME</code> represents the new namespace name being
	 * introduced.
	 */
	public static final ASTNodeProperty ALIAS_NAME = new ASTNodeProperty(
			"Alias name"); //$NON-NLS-1$

	/**
	 * <code>MAPPING_NAME</code> represents the pre-existing namespace which
	 * the new symbol aliases.
	 */
	public static final ASTNodeProperty MAPPING_NAME = new ASTNodeProperty(
			"Mapping name"); //$NON-NLS-1$

	/**
	 * Get the new alias name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getAlias();

	/**
	 * Set the new alias name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setAlias(IASTName name);

	/**
	 * Get the mapping name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getMappingName();

	/**
	 * Set the mapping name.
	 * 
	 * @param qualifiedName
	 *            <code>IASTName</code>
	 */
	public void setMappingName(IASTName qualifiedName);

}
