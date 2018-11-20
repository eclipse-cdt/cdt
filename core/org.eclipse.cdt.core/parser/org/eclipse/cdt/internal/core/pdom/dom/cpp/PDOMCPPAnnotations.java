/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation.
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
 *     Thomas Corbat (IFS)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;

class PDOMCPPAnnotations {
	private static final int VISIBILITY_OFFSET = 0;
	private static final int VISIBILITY_MASK = 0x03;
	private static final int EXTERN_OFFSET = 2;
	private static final int MUTABLE_OFFSET = 3;
	private static final int STATIC_OFFSET = 4;
	private static final int CONSTEXPR_OFFSET = 5;

	// "Inline" shares the same offset as "mutable" because
	// only fields can be mutable and only functions can be inline.
	private static final int INLINE_OFFSET = MUTABLE_OFFSET;
	// "extern C" shares the same offset as visibility because
	// only members have visibility and they cannot be extern C.
	private static final int EXTERN_C_OFFSET = VISIBILITY_OFFSET;

	// Function annotations start here.
	private static final int VARARGS_OFFSET = 6;
	private static final int PARAMETER_PACK_OFFSET = 7;
	private static final int DELETED_OFFSET = 8;
	private static final int NO_RETURN_OFFSET = 9;

	// Method annotations that don't fit on the first 16 bits of annotations.
	private static final int VIRTUAL_OFFSET = 0;
	private static final int DESTRUCTOR_OFFSET = 1;
	private static final int IMPLICIT_OFFSET = 2;
	private static final int EXPLICIT_OFFSET = 3;
	private static final int PURE_VIRTUAL_OFFSET = 4;
	private static final int OVERRIDE_OFFSET = 5;
	private static final int FINAL_OFFSET = 6;

	/**
	 * Encodes annotations applicable to C++ functions.
	 *
	 * @param function the function whose annotations will be encoded
	 * @return a bit vector of the annotations
	 */
	public static short encodeFunctionAnnotations(ICPPFunction function) {
		short annotation = encodeVisibility(function);

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
		if (function.isExternC())
			annotation |= 1 << EXTERN_C_OFFSET;
		if (function.isConstexpr())
			annotation |= 1 << CONSTEXPR_OFFSET;
		if (function.hasParameterPack())
			annotation |= 1 << PARAMETER_PACK_OFFSET;
		if (function.isDeleted())
			annotation |= 1 << DELETED_OFFSET;

		return annotation;
	}

	/**
	 * Encodes annotations applicable to C++ variables.
	 *
	 * @param variable the IBinding whose annotations will be encoded
	 * @return a bit vector of the annotations
	 */
	public static byte encodeVariableAnnotations(ICPPVariable variable) {
		byte annotation = encodeVisibility(variable);

		if (variable.isExtern())
			annotation |= 1 << EXTERN_OFFSET;
		if (variable.isStatic())
			annotation |= 1 << STATIC_OFFSET;
		if (variable.isExternC())
			annotation |= 1 << EXTERN_C_OFFSET;
		if (variable.isConstexpr())
			annotation |= 1 << CONSTEXPR_OFFSET;
		if (variable instanceof ICPPField && ((ICPPField) variable).isMutable())
			annotation |= 1 << MUTABLE_OFFSET;

		return annotation;
	}

	private static byte encodeVisibility(ICPPBinding binding) {
		byte annotation = 0;
		if (binding instanceof ICPPMember) {
			ICPPMember member = (ICPPMember) binding;
			annotation = (byte) ((member.getVisibility() & VISIBILITY_MASK) << VISIBILITY_OFFSET);
		}
		return annotation;
	}

	/**
	 * Encodes extra annotations applicable to C++ methods.
	 *
	 * @param binding the IBinding whose annotations will be encoded
	 * @return a bit vector of the annotation
	 */
	public static byte encodeExtraMethodAnnotations(IBinding binding) {
		byte annotation = 0;
		if (binding instanceof ICPPMethod) {
			ICPPMethod method = (ICPPMethod) binding;
			if (method.isVirtual())
				annotation |= 1 << VIRTUAL_OFFSET;
			if (method.isDestructor())
				annotation |= 1 << DESTRUCTOR_OFFSET;
			if (method.isImplicit())
				annotation |= 1 << IMPLICIT_OFFSET;
			if (method.isPureVirtual())
				annotation |= 1 << PURE_VIRTUAL_OFFSET;
			if (method.isExplicit())
				annotation |= 1 << EXPLICIT_OFFSET;
			if (method.isOverride())
				annotation |= 1 << OVERRIDE_OFFSET;
			if (method.isFinal())
				annotation |= 1 << FINAL_OFFSET;
		}
		return annotation;
	}

	/**
	 * Unpacks visibility information from a bit vector of annotation.
	 *
	 * @param annotation the annotation containing visibility information.
	 * @return the visibility component of the annotation.
	 */
	public static int getVisibility(short annotation) {
		return (annotation >> VISIBILITY_OFFSET) & VISIBILITY_MASK;
	}

	/**
	 * Checks if the "extern" annotation is set.
	 */
	public static boolean isExtern(short annotation) {
		return (annotation & (1 << EXTERN_OFFSET)) != 0;
	}

	/**
	 * Checks if the "mutable" annotation is set.
	 */
	public static boolean isMutable(short annotation) {
		return (annotation & (1 << MUTABLE_OFFSET)) != 0;
	}

	/**
	 * Checks if the "static" annotation is set.
	 */
	public static boolean isStatic(short annotation) {
		return (annotation & (1 << STATIC_OFFSET)) != 0;
	}

	/**
	 * Checks if the "constexpr" annotation is set.
	 */
	public static boolean isConstexpr(short annotation) {
		return (annotation & (1 << CONSTEXPR_OFFSET)) != 0;
	}

	/**
	 * Checks if the "inline" annotation is set.
	 */
	public static boolean isInline(short annotation) {
		return (annotation & (1 << INLINE_OFFSET)) != 0;
	}

	/**
	 * Checks if the "extern C" annotation is set.
	 */
	public static boolean isExternC(short annotation) {
		return (annotation & (1 << EXTERN_C_OFFSET)) != 0;
	}

	/**
	 * Checks if the "varargs" annotation is set.
	 */
	public static boolean isVarargsFunction(short annotation) {
		return (annotation & (1 << VARARGS_OFFSET)) != 0;
	}

	/**
	 * Checks if the "has parameter pack" annotation is set.
	 */
	public static boolean hasParameterPack(short annotation) {
		return (annotation & (1 << PARAMETER_PACK_OFFSET)) != 0;
	}

	/**
	 * Checks if the "deleted" annotation is set.
	 */
	public static boolean isDeletedFunction(short annotation) {
		return (annotation & (1 << DELETED_OFFSET)) != 0;
	}

	/**
	 * Checks if the "no return" annotation is set.
	 */
	public static boolean isNoReturnFunction(short annotation) {
		return (annotation & (1 << NO_RETURN_OFFSET)) != 0;
	}

	/**
	 * Checks if the "virtual" annotation is set.
	 */
	public static boolean isVirtualMethod(byte annotation) {
		return (annotation & (1 << VIRTUAL_OFFSET)) != 0;
	}

	/**
	 * Checks if the "destructor" annotation is set.
	 */
	public static boolean isDestructor(byte annotation) {
		return (annotation & (1 << DESTRUCTOR_OFFSET)) != 0;
	}

	/**
	 * Checks if the "implicit" annotation is set.
	 */
	public static boolean isImplicitMethod(byte annotation) {
		return (annotation & (1 << IMPLICIT_OFFSET)) != 0;
	}

	/**
	 * Checks if the "explicit" annotation is set.
	 */
	public static boolean isExplicitMethod(byte annotation) {
		return (annotation & (1 << EXPLICIT_OFFSET)) != 0;
	}

	/**
	 * Checks if the "pure virtual " annotation is set.
	 */
	public static boolean isPureVirtualMethod(byte annotation) {
		return (annotation & (1 << PURE_VIRTUAL_OFFSET)) != 0;
	}

	/**
	 * Checks if the "override" annotation is set.
	 */
	public static boolean isOverrideMethod(byte annotation) {
		return (annotation & (1 << OVERRIDE_OFFSET)) != 0;
	}

	/**
	 * Checks if the "final" annotation is set.
	 */
	public static boolean isFinalMethod(byte annotation) {
		return (annotation & (1 << FINAL_OFFSET)) != 0;
	}

	/**
	 * Checks if the "explicit" annotation is set.
	 */
	public static byte clearImplicitMethodFlag(byte annotation) {
		return (byte) (annotation & ~(1 << IMPLICIT_OFFSET));
	}
}
