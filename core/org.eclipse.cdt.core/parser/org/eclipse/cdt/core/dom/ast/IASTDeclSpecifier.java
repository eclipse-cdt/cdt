/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the base interface that represents a declaration specifier sequence.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTDeclSpecifier extends IASTNode {
	/**
	 * No storage class specified.
	 */
	public static final int sc_unspecified = 0;
	public static final int sc_typedef = 1;
	public static final int sc_extern = 2;
	public static final int sc_static = 3;
	public static final int sc_auto = 4;
	public static final int sc_register = 5;
	/**
	 * @since 5.2
	 */
	public static final int sc_mutable = 6;

	/**
	 * Returns the storage class, which is one of the constants sc_...
	 */
	public int getStorageClass();

	// Type qualifier
	public boolean isConst();
	public boolean isVolatile();
	/**
	 * @since 5.2
	 */
	public boolean isRestrict();

	// Function specifier
	public boolean isInline();
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTDeclSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTDeclSpecifier copy(CopyStyle style);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setStorageClass(int storageClass);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setConst(boolean value);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setVolatile(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setRestrict(boolean value);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setInline(boolean value);
	
	/**
	 * @deprecated All constants must be defined in this interface.
	 */
	@Deprecated
	public static final int sc_last = sc_register;
}
