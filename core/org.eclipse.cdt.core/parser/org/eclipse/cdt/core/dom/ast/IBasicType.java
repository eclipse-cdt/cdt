/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for basic types.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBasicType extends IType {
	/**
	 * @since 5.2
	 */
	enum Kind {
		eUnspecified, eVoid, eChar, eWChar, eInt, eFloat, eDouble, eBoolean, eChar16, eChar32,
		/** @since 5.4 */
		eNullPtr,
		/** @since 5.5 */
		eInt128,
		/** @since 5.5 */
		eFloat128,
		/** @since 5.10 */
		eDecimal32,
		/** @since 5.10  */
		eDecimal64,
		/** @since 5.10  */
		eDecimal128;
	}

	/** @since 5.2 */
	final int IS_LONG = 1;
	/** @since 5.2 */
	final int IS_SHORT = 1 << 1;
	/** @since 5.2 */
	final int IS_SIGNED = 1 << 2;
	/** @since 5.2 */
	final int IS_UNSIGNED = 1 << 3;
	/** @since 5.2 */
	final int IS_COMPLEX = 1 << 4;
	/** @since 5.2 */
	final int IS_IMAGINARY = 1 << 5;
	/** @since 5.2 */
	final int IS_LONG_LONG = 1 << 6;

	/**
	 * This returns the kind of basic type you are looking at. The type is
	 * then refined by qualifiers for signed/unsigned and short/long/long long.
	 * @since 5.2
	 */
	Kind getKind();

	/**
	 * This returns the combination of modifier bits for this type.
	 * @since 5.2
	 */
	int getModifiers();

	public boolean isSigned();

	public boolean isUnsigned();

	public boolean isShort();

	public boolean isLong();

	/**
	 * @since 5.2
	 */
	public boolean isLongLong();

	/**
	 * Is complex number? e.g. _Complex t;
	 * @return true if it is a complex number, false otherwise
	 * @since 5.2
	 */
	public boolean isComplex();

	/**
	 * Is imaginary number? e.g. _Imaginr
	 * @return true if it is an imaginary number, false otherwise
	 * @since 5.2
	 */
	public boolean isImaginary();

	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public int getType() throws DOMException;

	/**
	 * @deprecated Types don't have values.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IASTExpression getValue() throws DOMException;

	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_unspecified = IASTSimpleDeclSpecifier.t_unspecified;
	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_void = IASTSimpleDeclSpecifier.t_void;
	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_char = IASTSimpleDeclSpecifier.t_char;
	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_int = IASTSimpleDeclSpecifier.t_int;
	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_float = IASTSimpleDeclSpecifier.t_float;
	/**
	 * @deprecated Use the type-safe version getKind(), instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int t_double = IASTSimpleDeclSpecifier.t_double;
}
