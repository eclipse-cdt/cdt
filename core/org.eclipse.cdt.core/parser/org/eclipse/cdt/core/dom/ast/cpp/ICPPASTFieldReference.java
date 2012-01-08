/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Certain field references in C++ require the use the keyword template to
 * specify the parse.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTFieldReference extends IASTFieldReference, IASTImplicitNameOwner {

	/**
	 * Was template keyword used?
	 * 
	 */
	public boolean isTemplate();

	/**
	 * Set the template keyword used.
	 * 
	 * @param value
	 */
	public void setIsTemplate(boolean value);
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTFieldReference copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTFieldReference copy(CopyStyle style);

	/**
	 * Returns the type of the field owner.
	 * @since 5.4
	 */
	public IType getFieldOwnerType();
}
