/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * This interface represents a simple type template parameter.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTSimpleTypeTemplateParameter extends ICPPASTTemplateParameter, IASTNameOwner {
	/**
	 * Relation between template parameter and its name.
	 */
	public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty(
			"ICPPASTSimpleTypeTemplateParameter.PARAMETER_NAME - The Parameter's Name"); //$NON-NLS-1$

	/**
	 * Relation between template parameter and its default type.
	 */
	public static final ASTNodeProperty DEFAULT_TYPE = new ASTNodeProperty(
			"ICPPASTSimpleTypeTemplateParameter.DEFAULT_TYPE - Optional default TypeId value"); //$NON-NLS-1$

	/**
	 * <code>st_class</code> represents a class.
	 */
	public static final int st_class = 1;

	/**
	 * <code>st_typename</code> represents a typename.
	 */
	public static final int st_typename = 2;

	/**
	 * Get the type of the type parameter (either {@link #st_class} or {@link #st_typename}).
	 */
	public int getParameterType();

	/**
	 * Returns the template parameter name.
	 */
	public IASTName getName();

	/**
	 * Returns the default value (a type id) for this template parameter, or <code>null</code>.
	 */
	public IASTTypeId getDefaultType();

	/**
	 * Set the type of the type parameter.
	 *
	 * @param value The type of the type parameter (either {@link #st_class} or {@link #st_typename})
	 */
	public void setParameterType(int value);

	/**
	 * Set whether this is a parameter pack.
	 * @since 5.2
	 */
	public void setIsParameterPack(boolean val);

	/**
	 * Sets the template parameter name.
	 */
	public void setName(IASTName name);

	/**
	 * Sets the default value (a type id) for this template parameter.
	 */
	public void setDefaultType(IASTTypeId typeId);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTSimpleTypeTemplateParameter copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTSimpleTypeTemplateParameter copy(CopyStyle style);
}
