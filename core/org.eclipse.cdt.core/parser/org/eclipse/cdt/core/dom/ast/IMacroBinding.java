/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Models bindings for macro names.
 *
 * <p> This interface is not intended to be implemented by clients. </p>
 */
public interface IMacroBinding extends IBinding {

	/**
	 * Returns the expansion of this macro definition.
	 * @since 5.0
	 */
	char[] getExpansion();
	
	/**
	 * Returns <code>true</code> if this is a function-style macro. 
	 * @since 5.0
	 */
	boolean isFunctionStyle();
}
