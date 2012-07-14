/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Models bindings for macro names.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMacroBinding extends IBinding {
	/**
	 * Returns <code>true</code> if this is a function-style macro. 
	 * @since 5.0
	 */
	boolean isFunctionStyle();
	
	/**
	 * Returns <code>true</code> if this is a dynamic macro. 
	 * @since 5.0
	 */
	boolean isDynamic();
	
	/**
	 * Returns the parameter names or <code>null</code> if this is not a function style macro.
	 */
	char[][] getParameterList();
	
	/**
	 * Returns the expansion of this macro definition, or <code>null</code> if the definition is not
	 * available. For dynamic macros an exemplary image is returned.
	 * @since 5.0
	 */
	char[] getExpansion();
	
	/**
	 * Returns the parameter list where the name of the last parameter is changed if this is a variadic macro,
	 * or <code>null</code> if this is not a function style macro.
	 * The parameter '...' will be changed to '__VA_ARGS__'
	 * Parameters like 'a...' will be changed to 'a'.
	 * @since 5.0
	 */
	char[][] getParameterPlaceholderList();

	/**
	 * Returns the image of the expansion (also containing comments), or <code>null</code> if the definition 
	 * is not available. For dynamic macros an exemplary image is returned.
	 * @since 5.0
	 */
	char[] getExpansionImage();
}
