/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;

/**
 * A utility class for packing various annotations into bit fields.  This
 * includes storage class specifiers (auto, register, etc.), and CV qualifiers
 * (const, volatile).
 */
public class PDOMCAnnotation {

	// Storage class specifiers and function annotations
	public static final int AUTO_OFFSET = 0;
	public static final int EXTERN_OFFSET = 1;
	public static final int INLINE_OFFSET = 2;
	public static final int REGISTER_OFFSET = 3;
	public static final int STATIC_OFFSET = 4;
	public static final int VARARGS_OFFSET = 5;

	// CV qualifiers
	public static final int CONST_OFFSET = 0;
	public static final int VOLATILE_OFFSET = 1;
	
	/**
	 * Encodes storage class specifiers and other annotation from an IBinding
	 * as a bit vector.
	 * 
	 * @param binding the IBinding whose annotation will be encoded.
	 * @return a bit vector of the annotation.
	 */
	public static byte encodeAnnotation(IBinding binding) {
		byte modifiers = 0;
		if (binding instanceof IVariable) {
			IVariable variable = (IVariable) binding;
			modifiers |= (variable.isAuto() ? 1 : 0) << PDOMCAnnotation.AUTO_OFFSET;
			modifiers |= (variable.isExtern() ? 1 : 0) << PDOMCAnnotation.EXTERN_OFFSET;
			modifiers |= (variable.isRegister() ? 1 : 0) << PDOMCAnnotation.REGISTER_OFFSET;
			modifiers |= (variable.isStatic() ? 1 : 0) << PDOMCAnnotation.STATIC_OFFSET;
		}
		if (binding instanceof IFunction) {
			IFunction function = (IFunction) binding;
			modifiers |= (function.isAuto() ? 1 : 0) << PDOMCAnnotation.AUTO_OFFSET;
			modifiers |= (function.isExtern() ? 1 : 0) << PDOMCAnnotation.EXTERN_OFFSET;
			modifiers |= (function.isRegister() ? 1 : 0) << PDOMCAnnotation.REGISTER_OFFSET;
			modifiers |= (ASTInternal.isStatic(function, false) ? 1 : 0) << PDOMCAnnotation.STATIC_OFFSET;
			modifiers |= (function.isInline() ? 1 : 0) << PDOMCAnnotation.INLINE_OFFSET;
			modifiers |= (function.takesVarArgs() ? 1 : 0) << PDOMCAnnotation.VARARGS_OFFSET;
		}
		return modifiers;
	}
	
	/**
	 * Encodes CV qualifiers from a method declarator as a bit vector.
	 * @param type the function type
	 * @return a bit vector of the CV qualifiers.
	 */
	/*
	 * aftodo - will we put CV information in C pdom bindings or should we
	 * move this to PDOMCPPAnnotation?
	 */
	public static byte encodeCVQualifiers(ICPPFunctionType type) {
		byte modifiers = 0;
		modifiers |= (type.isConst() ? 1 : 0) << CONST_OFFSET;
		modifiers |= (type.isVolatile() ? 1 : 0) << VOLATILE_OFFSET;
		return modifiers;
	}
}
