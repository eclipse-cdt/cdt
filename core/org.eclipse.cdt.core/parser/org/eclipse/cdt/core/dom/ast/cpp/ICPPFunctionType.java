/*******************************************************************************
 * Copyright (c) 2005-2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 * 	   Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPFunctionType extends IFunctionType {
	/**
	 * Returns <code>true</code> for a constant method
	 */
	public boolean isConst();
	
	/**
	 * Returns <code>true</code> for a volatile method
	 */
	public boolean isVolatile();
	
	/**
	 * Whether the function type takes variable number of arguments.
	 * @since 5.2
	 */
	public boolean takesVarArgs();
	
	/**
	 * @deprecated function types don't relate to this pointers at all.
	 * @noreference This method is not intended to be referenced by clients and should be removed.
	 */
	@Deprecated
	public IPointerType getThisType();
}
