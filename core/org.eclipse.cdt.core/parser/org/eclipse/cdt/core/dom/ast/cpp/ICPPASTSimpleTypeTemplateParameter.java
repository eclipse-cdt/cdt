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
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * This interface represents a simple type template parameter.
 * 
 * @author jcamelon
 */
public interface ICPPASTSimpleTypeTemplateParameter extends
		ICPPASTTemplateParameter, IASTNameOwner {

	/**
	 * <code>st_class</code> represents a class.
	 */
	public static final int st_class = 1;

	/**
	 * <code>st_typename</code> represents a typename.
	 */
	public static final int st_typename = 2;

	/**
	 * Get the parameter type.
	 * 
	 * @return int
	 */
	public int getParameterType();

	/**
	 * Set the parameter type.
	 * 
	 * @param value
	 *            int
	 */
	public void setParameterType(int value);

	/**
	 * The parameter name.
	 */
	public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty(
			"Name"); //$NON-NLS-1$

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

	/**
	 * DEFAULT_TYPE is the optional default typeId value
	 */
	public static final ASTNodeProperty DEFAULT_TYPE = new ASTNodeProperty(
			"Default Type"); //$NON-NLS-1$

	/**
	 * Get the default type.
	 * 
	 * @return <code>IASTTypeId</code>
	 */
	public IASTTypeId getDefaultType();

	/**
	 * Set the default type.
	 * 
	 * @param typeId
	 *            <code>IASTTypeId</code>
	 */
	public void setDefaultType(IASTTypeId typeId);

}
