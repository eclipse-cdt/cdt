/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * Specific designator that represents a field reference.
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
	 * Returns the field name.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Sets the field name.
	 *
	 * @param name <code>IASTName</code>
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
