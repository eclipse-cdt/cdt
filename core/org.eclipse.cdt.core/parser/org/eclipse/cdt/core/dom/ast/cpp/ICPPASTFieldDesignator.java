/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * Specific designator that represents a field reference.
 * @since 6.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTFieldDesignator extends ICPPASTDesignator {
	/** The name of the field being initialized. */
	public static final ASTNodeProperty FIELD_NAME = new ASTNodeProperty(
			"ICPPASTFieldDesignator.FIELD_NAME - field name"); //$NON-NLS-1$

	/**
	 * Returns the field name.
	 */
	public IASTName getName();

	/**
	 * Sets the field name.
	 */
	public void setName(IASTName name);

	@Override
	public ICPPASTFieldDesignator copy();

	@Override
	public ICPPASTFieldDesignator copy(CopyStyle style);
}
