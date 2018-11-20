/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;

/**
 * A utility class for packing various annotations into bit fields.  This includes
 * storage class specifiers (auto, register, etc.), and CV qualifiers (const, volatile).
 */
class PDOMCAnnotations {
	// Storage class specifiers and function annotations.
	private static final int EXTERN_OFFSET = 0;
	private static final int INLINE_OFFSET = 1;
	private static final int STATIC_OFFSET = 2;
	private static final int VARARGS_OFFSET = 3;
	private static final int NO_RETURN_OFFSET = 4;
	private static final int REGISTER_OFFSET = 5;
	private static final int AUTO_OFFSET = 6;

	/**
	 * Encodes annotations applicable to functions.
	 *
	 * @param function the function whose annotations will be encoded
	 * @return a bit vector of the annotations
	 */
	public static byte encodeFunctionAnnotations(IFunction function) {
		byte annotation = 0;
		if (function.isExtern())
			annotation |= 1 << EXTERN_OFFSET;
		if (ASTInternal.isStatic(function, false))
			annotation |= 1 << STATIC_OFFSET;
		if (function.isInline())
			annotation |= 1 << INLINE_OFFSET;
		if (function.takesVarArgs())
			annotation |= 1 << VARARGS_OFFSET;
		if (function.isNoReturn())
			annotation |= 1 << NO_RETURN_OFFSET;
		if (function.isRegister())
			annotation |= 1 << REGISTER_OFFSET;
		if (function.isAuto())
			annotation |= 1 << AUTO_OFFSET;

		return annotation;
	}

	/**
	 * Encodes annotations applicable to variables.
	 *
	 * @param variable the IBinding whose annotation will be encoded.
	 * @return a bit vector of the annotation.
	 */
	public static byte encodeVariableAnnotations(IVariable variable) {
		byte modifiers = 0;
		if (variable.isExtern())
			modifiers |= 1 << EXTERN_OFFSET;
		if (variable.isStatic())
			modifiers |= 1 << STATIC_OFFSET;
		if (variable.isRegister())
			modifiers |= 1 << REGISTER_OFFSET;
		if (variable.isAuto())
			modifiers |= 1 << AUTO_OFFSET;

		return modifiers;
	}

	/**
	 * Checks if the "extern" annotation is set.
	 */
	public static boolean isExtern(short annotation) {
		return (annotation & (1 << EXTERN_OFFSET)) != 0;
	}

	/**
	 * Checks if the "static" annotation is set.
	 */
	public static boolean isStatic(short annotation) {
		return (annotation & (1 << STATIC_OFFSET)) != 0;
	}

	/**
	 * Checks if the "inline" annotation is set.
	 */
	public static boolean isInline(short annotation) {
		return (annotation & (1 << INLINE_OFFSET)) != 0;
	}

	/**
	 * Checks if the "varargs" annotation is set.
	 */
	public static boolean isVarargsFunction(short annotation) {
		return (annotation & (1 << VARARGS_OFFSET)) != 0;
	}

	/**
	 * Checks if the "no return" annotation is set.
	 */
	public static boolean isNoReturnFunction(short annotation) {
		return (annotation & (1 << NO_RETURN_OFFSET)) != 0;
	}

	/**
	 * Checks if the "register" annotation is set.
	 */
	public static boolean isRegister(byte annotation) {
		return (annotation & (1 << REGISTER_OFFSET)) != 0;
	}

	/**
	 * Checks if the "auto" annotation is set.
	 */
	public static boolean isAuto(byte annotation) {
		return (annotation & (1 << AUTO_OFFSET)) != 0;
	}
}
