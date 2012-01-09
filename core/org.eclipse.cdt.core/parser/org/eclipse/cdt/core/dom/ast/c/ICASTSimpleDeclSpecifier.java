/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

/**
 * This interface represents a built-in type in C.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTSimpleDeclSpecifier extends IASTSimpleDeclSpecifier, ICASTDeclSpecifier {

	/**
	 * @since 5.1
	 */
	@Override
	public ICASTSimpleDeclSpecifier copy();
	
	/**
	 * @since 5.3
	 */
	@Override
	public ICASTSimpleDeclSpecifier copy(CopyStyle style);

	/**
	 * @deprecated Replaced by {@link IASTSimpleDeclSpecifier#t_bool}.
	 */
	@Deprecated
	public static final int t_Bool = t_bool;

	/**
	 * @deprecated All constants must be defined in {@link IASTSimpleDeclSpecifier}.
	 */
	@Deprecated
	public static final int t_last = t_Bool;
}
