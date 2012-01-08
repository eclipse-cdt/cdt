/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * Specific Designator that represents a field reference.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTFieldDesignator extends ICASTDesignator {

	/**
	 * <code>FIELD_NAME</code> represent the relationship between an
	 * <code>ICASTFieldDesignator</code> and an <code>IASTName</code>.
	 */
	public static final ASTNodeProperty FIELD_NAME = new ASTNodeProperty(
			"ICASTFieldDesignator.FIELD_NAME - ICASTFieldDesignator Field Name"); //$NON-NLS-1$

	/**
	 * Get the field name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set the field name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICASTFieldDesignator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTFieldDesignator copy(CopyStyle style);
}
